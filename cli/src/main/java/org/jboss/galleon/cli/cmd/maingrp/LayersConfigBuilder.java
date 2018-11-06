/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
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

    private final String[] layers;
    private final String config;
    private final String model;
    private final FeaturePackLocation loc;

    LayersConfigBuilder(PmSession session, String[] layers, String model,
            String config, FeaturePackLocation loc) throws ProvisioningException, IOException {
        this.layers = layers;
        this.loc = loc;

        Map<String, Map<String, Set<String>>> layersMap = getAllLayers(session, loc, false);

        this.model = getModel(model, layersMap);
        if (this.model == null) {
            throw new ProvisioningException(CliErrors.noLayersForModel(model));
        }

        Map<String, Set<String>> map = layersMap.get(this.model);
        if (map == null) {
            throw new ProvisioningException(CliErrors.noLayersForModel(this.model));
        }
        Set<String> actualLayers = map.keySet();
        for (String l : layers) {
            if (!actualLayers.contains(l)) {
                throw new ProvisioningException(CliErrors.unknownLayer(l));
            }
        }
        this.config = config == null ? this.model + ".xml" : config;
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
                names.put(layer.getName(), dependencies);
            }
        }
        return layersMap;
    }

    ProvisioningConfig build() throws ProvisioningException, IOException {
        final ProvisioningConfig.Builder builder = ProvisioningConfig.builder();
        ConfigModel.Builder configBuilder = ConfigModel.builder(model, config);
        for (String layer : layers) {
            configBuilder.includeLayer(layer);
        }
        builder.addConfig(configBuilder.build());
        builder.addFeaturePackDep(FeaturePackConfig.builder(loc).setInheritConfigs(false).
                setInheritPackages(false).build());
        return builder.build();
    }
}
