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
package org.jboss.galleon.diff;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.galleon.Constants;
import org.jboss.galleon.MessageWriter;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeatureGroup;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.layout.ProvisioningLayout;
import org.jboss.galleon.plugin.StateDiffPlugin;
import org.jboss.galleon.plugin.ProvisionedConfigHandler;
import org.jboss.galleon.runtime.FeaturePackRuntimeBuilder;
import org.jboss.galleon.runtime.ProvisioningRuntime;
import org.jboss.galleon.runtime.ProvisioningRuntimeBuilder;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.spec.FeatureId;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.spec.SpecId;
import org.jboss.galleon.state.ProvisionedConfig;
import org.jboss.galleon.state.ProvisionedFeature;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.util.CollectionUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class ProvisioningDiffProvider {

    public static ProvisioningDiffProvider newInstance(ProvisioningLayout<FeaturePackRuntimeBuilder> layout, ProvisionedState provisionedState, FsDiff diff, MessageWriter log) {
        ProvisioningDiffProvider diffProvider = new ProvisioningDiffProvider();
        diffProvider.layout = layout;
        diffProvider.provisionedConfig = layout.getConfig();
        diffProvider.provisionedState = provisionedState;
        diffProvider.fsDiff = diff;
        diffProvider.log = log;
        return diffProvider;
    }

    private ProvisioningLayout<FeaturePackRuntimeBuilder> layout;
    private ProvisioningConfig provisionedConfig;
    private ProvisionedState provisionedState;
    private FsDiff fsDiff;
    private MessageWriter log;

    private Map<FPID, FeaturePackConfig.Builder> updatedDirectFps = Collections.emptyMap();
    private Map<FPID, FeaturePackConfig.Builder> updatedTransitiveFps = Collections.emptyMap();
    private Map<FPID, FeaturePackConfig.Builder> addedTransitiveFps = Collections.emptyMap();
    private Map<ConfigId, ConfigModel> updatedConfigs = new LinkedHashMap<>(1);
    private Map<ConfigId, ConfigModel> addedConfigs = Collections.emptyMap();
    private Set<ConfigId> removedConfigs = Collections.emptySet();
    private Map<ConfigId, ProvisionedFeatureDiffCallback> configPlugins = Collections.emptyMap();
    private FeatureDiff featureDiff;

    private ProvisioningConfig mergedConfig;

    private ProvisioningDiffProvider() {
    }

    public MessageWriter getMessageWriter() {
        return log;
    }

    public ProvisioningLayout<FeaturePackRuntimeBuilder> getProvisioningLayout() {
        return layout;
    }

    public ProvisioningConfig getOriginalConfig() {
        return provisionedConfig;
    }

    public ProvisionedState getProvisionedState() {
        return provisionedState;
    }

    public FsDiff getFsDiff() {
        return fsDiff;
    }

    public void excludePackage(StateDiffPlugin plugin, FPID fpid, String name, String... relativePaths) throws ProvisioningException {
        getFpcBuilder(fpid).excludePackage(name);
        suppressPaths(relativePaths);
    }

    public void includePackage(StateDiffPlugin plugin, FPID fpid, String name, String... relativePaths) throws ProvisioningException {
        getFpcBuilder(fpid).includePackage(name);
        suppressPaths(relativePaths);
    }

    public void updateConfig(ProvisionedConfig updatedConfig, String... relativePaths) throws ProvisioningException {
        updateConfig(ProvisionedFeatureDiffCallback.DEFAULT, updatedConfig, relativePaths);
    }

    public void updateConfig(ProvisionedFeatureDiffCallback featureDiffCallback, ProvisionedConfig updatedConfig, String... relativePaths) throws ProvisioningException {

        final ConfigId configId = new ConfigId(updatedConfig.getModel(), updatedConfig.getName());
        configPlugins = CollectionUtils.put(configPlugins, configId, featureDiffCallback);
        if(featureDiff == null) {
            featureDiff = new FeatureDiff(log);
        }
        featureDiff.init(getRequiredProvisionedConfig(provisionedState.getConfigs(), updatedConfig.getModel(), updatedConfig.getName()));
        featureDiff.diff(featureDiffCallback, updatedConfig);
        final ConfigModel mergedConfig = featureDiff.getMergedConfig(layout);
        if (mergedConfig == null) {
            log.verbose("%s has not changed", updatedConfig.getName());
        } else {
            updatedConfigs.put(configId, mergedConfig);
        }
        featureDiff.reset();

        suppressPaths(relativePaths);
    }

    public void addConfig(ProvisionedConfig config, String... relativePaths) throws ProvisioningException {
        addConfig(ProvisionedFeatureDiffCallback.DEFAULT, config, relativePaths);
    }

    public void addConfig(ProvisionedFeatureDiffCallback featureDiffCallback, ProvisionedConfig config, String... relativePaths) throws ProvisioningException {

        if(featureDiff == null) {
            featureDiff = new FeatureDiff(log);
        }
        featureDiff.reset();
        featureDiff.model = config.getModel();
        featureDiff.name = config.getName();
        featureDiff.diff(featureDiffCallback, config);
        final ConfigModel mergedConfig = featureDiff.getMergedConfig(layout);
        if (mergedConfig == null) {
            log.verbose("%s is meaningless", config.getName());
        } else {
            addedConfigs = CollectionUtils.putLinked(addedConfigs, new ConfigId(config.getModel(), config.getName()), mergedConfig);
        }
        featureDiff.reset();

        suppressPaths(relativePaths);
    }

    public void removeConfig(ConfigId configId, String... relativePaths) throws ProvisioningException {
        removedConfigs = CollectionUtils.add(removedConfigs, configId);
        suppressPaths(relativePaths);
    }

    public boolean hasConfigChanges() {
        return !updatedDirectFps.isEmpty() ||
                !updatedTransitiveFps.isEmpty() ||
                !addedTransitiveFps.isEmpty() ||
                !updatedConfigs.isEmpty() ||
                !addedConfigs.isEmpty() ||
                !removedConfigs.isEmpty();
    }

    public ProvisioningConfig getMergedConfig() throws ProvisioningException {
        if (mergedConfig != null) {
            return mergedConfig;
        }
        if (!hasConfigChanges()) {
            mergedConfig = provisionedConfig;
            return provisionedConfig;
        }
        final ProvisioningConfig.Builder configBuilder = initProvisioningConfig();
        final Collection<ConfigModel> definedConfigs = provisionedConfig.getDefinedConfigs();
        if (!definedConfigs.isEmpty()) {
            for (ConfigModel originalConfig : definedConfigs) {
                final ConfigModel updatedConfig = updatedConfigs.remove(originalConfig.getId());
                if (updatedConfig != null) {
                    configBuilder.addConfig(updatedConfig);
                    continue;
                }
                if (removedConfigs.contains(originalConfig.getId())) {
                    continue;
                }
                configBuilder.addConfig(originalConfig);
            }
        }
        if(!updatedConfigs.isEmpty()) {
            for (ConfigModel updatedConfig : updatedConfigs.values()) {
                configBuilder.addConfig(updatedConfig);
            }
        }

        if (!addedConfigs.isEmpty()) {
            for (ConfigModel addedConfig : addedConfigs.values()) {
                configBuilder.addConfig(addedConfig);
            }
        }

        if(!removedConfigs.isEmpty()) {
            List<ProvisionedConfig> baseConfigs = null;
            for(ConfigId configId : removedConfigs) {
                if(provisionedConfig.hasDefinedConfig(configId)) {
                    if (baseConfigs == null) {
                        final ProvisioningConfig.Builder baseBuilder = initProvisioningConfig();
                        for (ProvisionedConfig config : provisionedState.getConfigs()) {
                            final ConfigId provisionedId = new ConfigId(config.getModel(), config.getName());
                            if (!provisionedConfig.hasDefinedConfig(provisionedId)) {
                                baseBuilder.excludeDefaultConfig(provisionedId);
                            }
                        }
                        try (ProvisioningRuntime baseRt = ProvisioningRuntimeBuilder.newInstance(log)
                                .initLayout(layout.getFactory(), baseBuilder.build()).build()) {
                            baseConfigs = baseRt.getConfigs();
                        }
                    }
                    if(getProvisionedConfig(baseConfigs, configId.getModel(), configId.getName()) != null) {
                        if(provisionedConfig.isInheritConfigs()) {
                            if(!provisionedConfig.isConfigModelExcluded(configId)) {
                                configBuilder.excludeDefaultConfig(configId);
                            }
                        } else if(provisionedConfig.isConfigModelIncluded(configId)) {
                            configBuilder.excludeDefaultConfig(configId);
                        }
                    }
                } else if(provisionedConfig.isInheritConfigs()) {
                    if(!provisionedConfig.isConfigModelExcluded(configId)) {
                        configBuilder.excludeDefaultConfig(configId);
                    }
                } else if(provisionedConfig.isConfigModelIncluded(configId)) {
                    configBuilder.excludeDefaultConfig(configId);
                }
            }
        }
        mergedConfig = configBuilder.build();
        return mergedConfig;
    }

    private ProvisioningConfig.Builder initProvisioningConfig() throws ProvisioningDescriptionException {
        final ProvisioningConfig.Builder configBuilder = ProvisioningConfig.builder();
        configBuilder.initUniverses(provisionedConfig);
        if (provisionedConfig.hasOptions()) {
            configBuilder.addOptions(provisionedConfig.getOptions());
        }
        for (FeaturePackConfig fp : provisionedConfig.getFeaturePackDeps()) {
            final FeaturePackConfig.Builder fpcBuilder = updatedDirectFps.get(fp.getLocation().getFPID());
            if (fpcBuilder == null) {
                configBuilder.addFeaturePackDep(provisionedConfig.originOf(fp.getLocation().getProducer()), fp);
            } else {
                configBuilder.addFeaturePackDep(provisionedConfig.originOf(fp.getLocation().getProducer()), fpcBuilder.build());
            }
        }
        for (FeaturePackConfig fp : provisionedConfig.getTransitiveDeps()) {
            final FeaturePackConfig.Builder fpcBuilder = updatedTransitiveFps.get(fp.getLocation().getFPID());
            if (fpcBuilder == null) {
                configBuilder.addFeaturePackDep(provisionedConfig.originOf(fp.getLocation().getProducer()), fp);
            } else {
                configBuilder.addFeaturePackDep(provisionedConfig.originOf(fp.getLocation().getProducer()), fpcBuilder.build());
            }
        }
        for (FeaturePackConfig.Builder fpcBuilder : addedTransitiveFps.values()) {
            configBuilder.addFeaturePackDep(fpcBuilder.build());
        }

        configBuilder.setInheritConfigs(provisionedConfig.isInheritConfigs());
        configBuilder.setInheritModelOnlyConfigs(provisionedConfig.isInheritModelOnlyConfigs());
        if(provisionedConfig.hasFullModelsExcluded()) {
            for(Map.Entry<String, Boolean> entry : provisionedConfig.getFullModelsExcluded().entrySet()) {
                configBuilder.excludeConfigModel(entry.getKey(), entry.getValue());
            }
        }
        if(provisionedConfig.hasFullModelsIncluded()) {
            for(String model : provisionedConfig.getFullModelsIncluded()) {
                configBuilder.includeConfigModel(model);
            }
        }
        return configBuilder;
    }

    private void suppressPaths(String... relativePaths) throws ProvisioningException {
        for (String relativePath : relativePaths) {
            fsDiff.suppress(relativePath);
        }
    }

    private FeaturePackConfig.Builder getFpcBuilder(FPID fpid) {
        FeaturePackConfig.Builder fpcBuilder = updatedDirectFps.get(fpid);
        if(fpcBuilder != null) {
            return fpcBuilder;
        }
        fpcBuilder = updatedTransitiveFps.get(fpid);
        if(fpcBuilder != null) {
            return fpcBuilder;
        }
        fpcBuilder = addedTransitiveFps.get(fpid);
        if(fpcBuilder != null) {
            return fpcBuilder;
        }

        FeaturePackConfig fpc = provisionedConfig.getFeaturePackDep(fpid.getProducer());
        if(fpc != null) {
            fpcBuilder = FeaturePackConfig.builder(fpc);
            updatedDirectFps = CollectionUtils.put(updatedDirectFps, fpid, fpcBuilder);
            return fpcBuilder;
        }

        fpc = provisionedConfig.getTransitiveDep(fpid.getProducer());
        if(fpc != null) {
            fpcBuilder = FeaturePackConfig.builder(fpc);
            updatedTransitiveFps = CollectionUtils.put(updatedTransitiveFps, fpid, fpcBuilder);
            return fpcBuilder;
        }

        fpcBuilder = FeaturePackConfig.transitiveBuilder(fpid.getLocation());
        addedTransitiveFps = CollectionUtils.putLinked(addedTransitiveFps, fpid, fpcBuilder);
        return fpcBuilder;
    }

    private static class FeatureDiff implements ProvisionedConfigHandler {
        private MessageWriter log;
        private String model;
        private String name;
        Map<ResolvedFeatureId, ProvisionedFeature> original = new HashMap<>();
        Map<ResolvedFeatureId, ProvisionedFeature> added = Collections.emptyMap();
        Map<ResolvedFeatureId, ProvisionedFeature[]> modified = Collections.emptyMap();
        List<ProvisionedFeature> originalWoIds = Collections.emptyList();
        List<ProvisionedFeature> addedWoIds = Collections.emptyList();
        private boolean init;
        private ProvisionedFeatureDiffCallback featureCallback;
        private ProvisionedConfig provisionedConfig;

        FeatureDiff(MessageWriter log) {
            this.log = log;
        }

        public void reset() {
            original.clear();
            added = Collections.emptyMap();
            modified = Collections.emptyMap();
            originalWoIds = Collections.emptyList();
            addedWoIds = Collections.emptyList();
            featureCallback = null;
            model = null;
            name = null;
            provisionedConfig = null;
        }

        public void init(ProvisionedConfig config) throws ProvisioningException {
            reset();
            model = config.getModel();
            name = config.getName();
            init = true;
            config.handle(this);
            init = false;
        }

        public void diff(ProvisionedFeatureDiffCallback featureDiffCallback, ProvisionedConfig config) throws ProvisioningException {
            this.featureCallback = featureDiffCallback;
            this.provisionedConfig = config;
            config.handle(this);
            if(!original.isEmpty()) {
                final Iterator<Map.Entry<ResolvedFeatureId, ProvisionedFeature>> i = original.entrySet().iterator();
                while(i.hasNext()) {
                    if(!featureCallback.removed(i.next().getValue())) {
                        i.remove();
                    }
                }
            }
            if(!originalWoIds.isEmpty()) {
                if(originalWoIds.size() == 1) {
                    if(!featureCallback.removed(originalWoIds.get(0))) {
                        originalWoIds = Collections.emptyList();
                    }
                } else {
                    final Iterator<ProvisionedFeature> i = originalWoIds.iterator();
                    while(i.hasNext()) {
                        if(!featureCallback.removed(i.next())) {
                            i.remove();
                        }
                    }
                }
            }
        }

        public ConfigModel getMergedConfig(ProvisioningLayout<FeaturePackRuntimeBuilder> layout) throws ProvisioningException {
            if(isEmpty()) {
                return null;
            }

            final ProvisioningConfig provisionedConfig = layout.getConfig();
            final ConfigModel definedConfig = provisionedConfig.getDefinedConfig(new ConfigId(model, name));
            final ConfigModel.Builder configBuilder = ConfigModel.builder(model, name);
            if(definedConfig != null) {
                final ProvisionedFeatureDiffCallback originalCallback = featureCallback;
                final ProvisionedConfig originalProvisioned = this.provisionedConfig;
                init(getDefaultProvisionedConfig(layout, definedConfig));
                diff(originalCallback, originalProvisioned);
                configBuilder.setInheritFeatures(definedConfig.isInheritFeatures());
                configBuilder.setInheritLayers(definedConfig.isInheritLayers());
                if(definedConfig.hasIncludedLayers()) {
                    for(String layer : definedConfig.getIncludedLayers()) {
                        configBuilder.includeLayer(layer);
                    }
                }
                if(definedConfig.hasExcludedLayers()) {
                    for(String layer : definedConfig.getExcludedLayers()) {
                        configBuilder.excludeLayer(layer);
                    }
                }
                if(definedConfig.hasIncludedSpecs()) {
                    for(SpecId specId : definedConfig.getIncludedSpecs()) {
                        configBuilder.includeSpec(specId.getName());
                    }
                }
                if(definedConfig.hasExcludedSpecs()) {
                    for(SpecId specId : definedConfig.getExcludedSpecs()) {
                        configBuilder.excludeSpec(specId.getName());
                    }
                }
                if(definedConfig.hasExternalFeatureGroups()) {
                    for(Map.Entry<String, FeatureGroup> entry : definedConfig.getExternalFeatureGroups().entrySet()) {
                        final FeatureGroup fg = entry.getValue();
                        if(fg.hasIncludedSpecs()) {
                            for(SpecId specId : fg.getIncludedSpecs()) {
                                configBuilder.includeSpec(entry.getKey(), specId.getName());
                            }
                        }
                        if(fg.hasExcludedSpecs()) {
                            for(SpecId specId : fg.getExcludedSpecs()) {
                                configBuilder.excludeSpec(entry.getKey(), specId.getName());
                            }
                        }
                    }
                }
            }

            if(isEmpty()) {
                return configBuilder.build();
            }

            if(!original.isEmpty()) {
                for(ProvisionedFeature feature : original.values()) {
                    // TODO this could check for excluded references with include=true
                    final String origin = provisionedConfig.originOf(feature.getSpecId().getProducer());
                    if(isExcluded(origin, feature, definedConfig)) {
                        continue;
                    }
                    configBuilder.excludeFeature(origin, getFeatureId(feature));
                }
            }
            if(!modified.isEmpty()) {
                for(ProvisionedFeature[] feature : modified.values()) {
                    final ProvisionedFeature original = feature[0];
                    final ProvisionedFeature actual = feature[1];
                    final FeatureSpec fSpec = layout.getFeaturePack(actual.getSpecId().getProducer()).getFeatureSpec(actual.getSpecId().getName()).getSpec();
                    final FeatureConfig config = new FeatureConfig(fSpec.getName());
                    config.setOrigin(provisionedConfig.originOf(actual.getSpecId().getProducer()));

                    final Set<String> actualParams = new HashSet<>(actual.getParamNames());
                    for(String paramName : original.getParamNames()) {
                        final FeatureParameterSpec pSpec = fSpec.getParam(paramName);
                        if(!actualParams.remove(paramName)) {
                            if(pSpec.isNillable() && pSpec.hasDefaultValue() && !Constants.GLN_UNDEFINED.equals(pSpec.getDefaultValue())) {
                                config.unsetParam(paramName);
                            }
                            continue;
                        }
                        final String actualValue = actual.getConfigParam(paramName);
                        final String originalValue = original.getConfigParam(paramName);
                        if(actualValue != null) {
                            if(!pSpec.isFeatureId() && !actualValue.equals(originalValue) && !actualValue.equals(pSpec.getDefaultValue())) {
                                config.setParam(paramName, actualValue);
                            }
                            continue;
                        }
                        if(pSpec.hasDefaultValue() && !Constants.GLN_UNDEFINED.equals(pSpec.getDefaultValue())) {
                            config.unsetParam(paramName);
                        }
                    }
                    if(!actualParams.isEmpty()) {
                        for(String paramName : actualParams) {
                            config.setParam(paramName, actual.getConfigParam(paramName));
                        }
                    }

                    configBuilder.includeFeature(getFeatureId(actual), config);
                }
            }

            if(!added.isEmpty()) {
                for(ProvisionedFeature feature : added.values()) {
                    final FeatureConfig config = new FeatureConfig(feature.getSpecId().getName());
                    for(String name : feature.getParamNames()) {
                        config.setParam(name, feature.getConfigParam(name));
                    }
                    configBuilder.addConfigItem(config);
                }
            }
            return configBuilder.build();
        }

        private static boolean isExcluded(String origin, ProvisionedFeature feature, ConfigModel definedConfig) throws ProvisioningDescriptionException {
            if(definedConfig == null) {
                return false;
            }
            if(definedConfig.isInheritFeatures()) {
                Set<SpecId> specs = Collections.emptySet();
                if (origin == null) {
                    specs = definedConfig.getExcludedSpecs();
                } else {
                    final FeatureGroup featureGroup = definedConfig.getExternalFeatureGroups().get(origin);
                    if (featureGroup != null) {
                        specs = featureGroup.getExcludedSpecs();
                    }
                }
                if (specs.contains(SpecId.fromString(feature.getSpecId().getName()))) {
                    return true;
                }
                return false;
            }
            Set<SpecId> specs = Collections.emptySet();
            if (origin == null) {
                specs = definedConfig.getIncludedSpecs();
            } else {
                final FeatureGroup featureGroup = definedConfig.getExternalFeatureGroups().get(origin);
                if (featureGroup != null) {
                    specs = featureGroup.getIncludedSpecs();
                }
            }
            if (specs.contains(SpecId.fromString(feature.getSpecId().getName()))) {
                return false;
            }
            return true;
        }

        boolean isEmpty() {
            return original.isEmpty()
                    && added.isEmpty()
                    && modified.isEmpty()
                    && originalWoIds.isEmpty()
                    && addedWoIds.isEmpty();
        }

        @Override
        public void nextFeature(ProvisionedFeature feature) throws ProvisioningException {
            if (init) {
                if (feature.getId() == null) {
                    originalWoIds = CollectionUtils.add(originalWoIds, feature);
                    return;
                }
                original.put(feature.getId(), feature);
                return;
            }
            if (feature.getId() == null) {
                int i = 0;
                boolean matchesOriginal = false;
                while (i < originalWoIds.size() && !matchesOriginal) {
                    final ProvisionedFeature original = originalWoIds.get(i++);
                    if (!original.getSpecId().equals(feature.getSpecId())) {
                        continue;
                    }
                    matchesOriginal = featureCallback.matches(original, feature);
                }
                if (matchesOriginal) {
                    originalWoIds = CollectionUtils.remove(originalWoIds, --i);
                } else if(featureCallback.added(feature)) {
                    addedWoIds = CollectionUtils.add(addedWoIds, feature);
                }
                return;
            }
            final ProvisionedFeature originalFeature = original.remove(feature.getId());
            if (originalFeature == null) {
                if(featureCallback.added(feature)) {
                    added = CollectionUtils.putLinked(added, feature.getId(), feature);
                }
                return;
            }
            if(!featureCallback.matches(originalFeature, feature)) {
                modified = CollectionUtils.putLinked(modified, feature.getId(), new ProvisionedFeature[] { originalFeature, feature });
            }
        }

        private ProvisionedConfig getDefaultProvisionedConfig(ProvisioningLayout<?> layout, final ConfigModel definedConfig)
                throws ProvisioningException {
            final ProvisioningConfig originalConfig = layout.getConfig();
            final ProvisioningConfig.Builder baseBuilder = ProvisioningConfig.builder().initUniverses(originalConfig)
                    .addOptions(originalConfig.getOptions());
            if (originalConfig.hasTransitiveDeps()) {
                for (FeaturePackConfig fp : originalConfig.getTransitiveDeps()) {
                    baseBuilder.addFeaturePackDep(originalConfig.originOf(fp.getLocation().getProducer()), fp);
                }
            }
            for (FeaturePackConfig fp : originalConfig.getFeaturePackDeps()) {
                baseBuilder.addFeaturePackDep(originalConfig.originOf(fp.getLocation().getProducer()), fp);
            }
            baseBuilder.setInheritConfigs(false);
            baseBuilder.includeDefaultConfig(model, name);
            baseBuilder.addConfig(getBaseConfig(definedConfig));
            ProvisioningConfig baseC = baseBuilder.build();
            try(ProvisioningRuntime baseRt = ProvisioningRuntimeBuilder.newInstance(log).initLayout(layout.getFactory(), baseC).build()) {
                return getRequiredProvisionedConfig(baseRt.getConfigs(), model, name);
            }
        }
    }

    private static ConfigModel getBaseConfig(final ConfigModel definedConfig) throws ProvisioningDescriptionException {
        final ConfigModel.Builder baseConfig = ConfigModel.builder(definedConfig.getModel(), definedConfig.getName());
        //baseConfig.setInheritFeatures(definedConfig.isInheritFeatures());
        baseConfig.setInheritLayers(definedConfig.isInheritLayers());
        if(definedConfig.hasIncludedLayers()) {
            for(String layer : definedConfig.getIncludedLayers()) {
                baseConfig.includeLayer(layer);
            }
        }
        if(definedConfig.hasExcludedLayers()) {
            for(String layer : definedConfig.getExcludedLayers()) {
                baseConfig.excludeLayer(layer);
            }
        }
        /* excluding things may break the config
        if(definedConfig.hasExcludedSpecs()) {
            for(SpecId specId : definedConfig.getExcludedSpecs()) {
                baseConfig.excludeSpec(specId.getName());
            }
        }
        if(definedConfig.hasIncludedSpecs()) {
            for(SpecId specId : definedConfig.getIncludedSpecs()) {
                baseConfig.includeSpec(specId.getName());
            }
        }
        */
        return baseConfig.build();
    }

    private static ProvisionedConfig getRequiredProvisionedConfig(List<ProvisionedConfig> list, String model, String name) throws ProvisioningException {
        final ProvisionedConfig config = getProvisionedConfig(list, model, name);
        if(config == null) {
            throw new ProvisioningException("Failed to locate provisioned config " + model + "/" + name);
        }
        return config;
    }

    private static ProvisionedConfig getProvisionedConfig(List<ProvisionedConfig> list, String model, String name) throws ProvisioningException {
        for (ProvisionedConfig config : list) {
            if ((config.getModel() == null && model == null
                    || config.getModel().equals(model))
                    && (config.getName() == null && name == null
                            || config.getName().equals(name))) {
                return config;
            }
        }
        return null;
    }

    private static FeatureId getFeatureId(final ProvisionedFeature feature)
            throws ProvisioningDescriptionException, ProvisioningException {
        final FeatureId.Builder id = FeatureId.builder(feature.getSpecId().getName());
        final ResolvedFeatureId resolvedId = feature.getId();
        for(String name : resolvedId.getParams().keySet()) {
            id.setParam(name, feature.getConfigParam(name));
        }
        return id.build();
    }
}
