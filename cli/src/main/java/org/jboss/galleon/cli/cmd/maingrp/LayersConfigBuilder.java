/*
 * Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.galleon.cli.cmd.maingrp;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.layout.FeaturePackLayout;
import org.jboss.galleon.layout.ProvisioningLayout;
import org.jboss.galleon.spec.ConfigLayerDependency;
import org.jboss.galleon.spec.ConfigLayerSpec;
import org.jboss.galleon.universe.FeaturePackLocation;

/**
 *
 * @author jdenise@redhat.com
 */
public class LayersConfigBuilder {

    public static final String EXCLUDE_PREFIX = "-";
    public static final String REMOVE_EXCLUDE_PREFIX = "+";

    private final String[] layers;
    private final String config;
    private final String model;
    private final FeaturePackLocation loc;
    private final ProvisioningManager mgr;
    private final Set<String> excludedLayers = new HashSet<>();
    private final Set<String> removeExcludedLayers = new HashSet<>();
    private final Set<String> includedLayers = new HashSet<>();
    private final Map<String, Set<String>> layersDeps;

    LayersConfigBuilder(ProvisioningManager mgr, PmSession session, String[] layers, String model,
            String config, FeaturePackLocation loc) throws ProvisioningException, IOException {
        this.mgr = mgr;
        this.layers = layers;
        this.loc = loc;

        Map<String, Map<String, Set<String>>> layersMap = getAllLayers(session, loc, true);

        this.model = getModel(model, layersMap);
        if (this.model == null) {
            throw new ProvisioningException(CliErrors.noLayersForModel(model));
        }

        layersDeps = layersMap.get(this.model);
        if (layersDeps == null) {
            throw new ProvisioningException(CliErrors.noLayersForModel(this.model));
        }
        Set<String> actualLayers = layersDeps.keySet();
        for (String l : layers) {
            String name = getLayerName(l);
            if (!actualLayers.contains(name)) {
                throw new ProvisioningException(CliErrors.unknownLayer(name));
            }
            if (l.startsWith(EXCLUDE_PREFIX)) {
                excludedLayers.add(name);
            } else {
                if (l.startsWith(REMOVE_EXCLUDE_PREFIX)) {
                    removeExcludedLayers.add(name);
                } else {
                    includedLayers.add(name);
                }
            }
        }
        this.config = config == null ? this.model + ".xml" : config;
    }

    private static String getLayerName(String name) {
        if (name.startsWith(EXCLUDE_PREFIX)) {
            name = name.substring(EXCLUDE_PREFIX.length());
        } else {
            if (name.startsWith(REMOVE_EXCLUDE_PREFIX)) {
                name = name.substring(REMOVE_EXCLUDE_PREFIX.length());
            }
        }
        return name;
    }

    private static String getModel(String model, Map<String, Map<String, Set<String>>> layersMap) throws ProvisioningException {
        if (model == null) {
            if (layersMap.isEmpty()) {
                throw new ProvisioningException(CliErrors.noLayers());
            }
            if (layersMap.size() != 1) {
                throw new ProvisioningException(CliErrors.tooMuchModels());
            }
            model = layersMap.keySet().iterator().next();
        }
        return model;
    }

    public static Set<String> getLayerNames(PmSession session, String model,
            FeaturePackLocation loc, Set<String> noDependencies) throws ProvisioningException, IOException {
        Set<String> names;
        Map<String, Map<String, Set<String>>> layersMap = getAllLayers(session, loc, true);
        model = getModel(model, layersMap);
        Map<String, Set<String>> layers = layersMap.get(model);
        if (layers != null) {
            names = new HashSet<>();
            // Compute all dependencies (transitive included).
            Set<String> allDependencies = new HashSet<>();
            for (String s : noDependencies) {
                getDependencies(s, allDependencies, layers);
            }
            for (Entry<String, Set<String>> entry : layers.entrySet()) {
                if (!allDependencies.contains(entry.getKey()) || noDependencies.contains(entry.getKey())) {
                    names.add(entry.getKey());
                }
            }
        } else {
            names = Collections.emptySet();
        }
        return names;
    }

    private static void getDependencies(String name, Set<String> set, Map<String, Set<String>> all) {
        Set<String> deps = all.get(name);
        if (deps != null) {
            set.addAll(deps);
            for (String n : deps) {
                getDependencies(n, set, all);
            }
        }
    }

    public static Map<String, Map<String, Set<String>>> getAllLayers(ProvisioningLayout<FeaturePackLayout> pLayout)
            throws ProvisioningException, IOException {
        return getAllLayers(pLayout, true);
    }

    private static Map<String, Map<String, Set<String>>> getAllLayers(PmSession session, FeaturePackLocation loc,
            boolean includeDependencies) throws ProvisioningException, IOException {
        ProvisioningConfig pConfig = ProvisioningConfig.builder().
                addFeaturePackDep(FeaturePackConfig.builder(loc).build()).build();
        try (ProvisioningLayout<FeaturePackLayout> layout = session.
                getLayoutFactory().newConfigLayout(pConfig)) {
            return getAllLayers(layout, includeDependencies);
        }
    }

    private static Map<String, Map<String, Set<String>>> getAllLayers(ProvisioningLayout<FeaturePackLayout> pLayout,
            boolean includeDependencies)
            throws ProvisioningException, IOException {
        Map<String, Map<String, Set<String>>> layersMap = new HashMap<>();
        for (FeaturePackLayout fp : pLayout.getOrderedFeaturePacks()) {
            for (ConfigId layer : fp.loadLayers()) {
                String model = layer.getModel();
                Map<String, Set<String>> names = layersMap.get(model);
                if (names == null) {
                    names = new HashMap<>();
                    layersMap.put(model, names);
                }
                Set<String> dependencies = new TreeSet<>();
                if (includeDependencies) {
                    ConfigLayerSpec spec = fp.loadConfigLayerSpec(model, layer.getName());
                    for (ConfigLayerDependency dep : spec.getLayerDeps()) {
                        dependencies.add(dep.getName());
                    }
                }
                // Case where a layer is redefined in multiple FP. Add all deps.
                Set<String> existingDependencies = names.get(layer.getName());
                if(existingDependencies != null) {
                    existingDependencies.addAll(dependencies);
                    dependencies = existingDependencies;
                }
                names.put(layer.getName(), dependencies);
            }
        }
        return layersMap;
    }

    ProvisioningConfig build() throws ProvisioningException, IOException {
        // Reuse existing configuration builder.
        ProvisioningConfig existing = mgr.getProvisioningConfig();
        ProvisioningConfig.Builder builder = null;
        FeaturePackConfig.Builder fpBuilder = null;
        ConfigModel.Builder configBuilder = null;
        if (existing != null) {
            builder = ProvisioningConfig.builder(existing);
            ConfigId id = new ConfigId(model, config);
            if (existing.hasDefinedConfig(id)) {
                ConfigModel cmodel = existing.getDefinedConfig(id);
                configBuilder = ConfigModel.builder(cmodel);
                handleLayers(configBuilder, cmodel);
                builder.removeConfig(id);
            }
            if (builder.hasFeaturePackDep(loc.getProducer())) {
                FeaturePackConfig fp = existing.getFeaturePackDep(loc.getProducer());
                fpBuilder = FeaturePackConfig.builder(fp);
                builder.removeFeaturePackDep(fp.getLocation());
            }
        }
        if (builder == null) {
            builder = ProvisioningConfig.builder();
        }
        if (configBuilder == null) {
            configBuilder = ConfigModel.builder(model, config);
            handleLayers(configBuilder, null);
        }
        if (fpBuilder == null) {
            fpBuilder = FeaturePackConfig.builder(loc).setInheritConfigs(false).
                    setInheritPackages(false);
        }
        builder.addConfig(configBuilder.build());
        builder.addFeaturePackDep(fpBuilder.build());
        return builder.build();
    }

    /**
     * Layers are included if not already included, excluded if not already
     * excluded. An already included layer can't be excluded. If an already
     * excluded layer is included, it is removed from excluded layers and
     * included.
     */
    private void handleLayers(ConfigModel.Builder configBuilder, ConfigModel cmodel) throws ProvisioningException {
        if (!removeExcludedLayers.isEmpty()) {
            if (cmodel == null) {
                throw new ProvisioningException(CliErrors.noExcludedLayers());
            }
            for (String l : removeExcludedLayers) {
                if (!cmodel.getExcludedLayers().contains(l)) {
                    throw new ProvisioningException(CliErrors.notExcludedLayer(l));
                } else {
                    configBuilder.removeExcludedLayer(l);
                }
            }
        }
        if (!excludedLayers.isEmpty()) {
            // Check that the dependencies exist in the set of provisioned layers.
            // Optional/vs non optional will be handled at provisioning time.
            Set<String> allDependencies = new HashSet<>();
            for (String l : includedLayers) {
                getDependencies(l, allDependencies, layersDeps);
            }
            // We could have already installed layers, so retrieve their dependencies
            if (cmodel != null) {
                for (String l : cmodel.getIncludedLayers()) {
                    getDependencies(l, allDependencies, layersDeps);
                }
            }
            for (String excludedLayer : excludedLayers) {
                if (!allDependencies.contains(excludedLayer)) {
                    throw new ProvisioningException(CliErrors.notDependencyLayer(excludedLayer));
                }
                if (cmodel == null) {
                    configBuilder.excludeLayer(excludedLayer);
                } else {
                    if (cmodel.getIncludedLayers().contains(excludedLayer)) {
                        throw new ProvisioningException(CliErrors.cantExcludeLayer(excludedLayer));
                    }
                    if (!cmodel.getExcludedLayers().contains(excludedLayer)) {
                        configBuilder.excludeLayer(excludedLayer);
                    }
                }
            }
        }
        for (String layer : includedLayers) {
            if (cmodel == null) {
                configBuilder.includeLayer(layer);
            } else {
                if (cmodel.getExcludedLayers().contains(layer)) {
                    configBuilder.removeExcludedLayer(layer);
                }
                if (!cmodel.getIncludedLayers().contains(layer)) {
                    configBuilder.includeLayer(layer);
                }
            }
        }
    }
}
