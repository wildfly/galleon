/*
 * Copyright 2016-2020 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.galleon.Constants;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningOption;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureGroupSupport;
import org.jboss.galleon.spec.ConfigLayerDependency;
import org.jboss.galleon.spec.FeatureDependencySpec;
import org.jboss.galleon.util.CollectionUtils;

/**
 * @author Alexey Loubyansky
 *
 */
class ConfigModelStack {

    private class ConfigScope {

        final ConfigModel config;
        private final boolean pushedFgScope;
        private List<ResolvedFeatureGroupConfig> groupStack = new ArrayList<>();

        ConfigScope(ConfigModel config) throws ProvisioningException {
            this.config = config;
            if(config != null) {
                pushedFgScope = push(config);
                if(pushedFgScope) {
                    newFgScope();
                }
            } else {
                pushedFgScope = false;
            }
        }

        void complete() throws ProvisioningException {
            if(pushedFgScope) {
                mergeFgScope();
            }
            for (int i = groupStack.size() - 1; i >= 0; --i) {
                rt.processIncludedFeatures(groupStack.get(i));
            }
        }

        boolean push(FeatureGroupSupport fg) throws ProvisioningException {
            final ResolvedFeatureGroupConfig resolvedFg = rt.resolveFeatureGroupConfig(ConfigModelStack.this, fg);
            if (!fg.isConfig() && !ConfigModelStack.this.isRelevant(resolvedFg)) {
                return false;
            }
            groupStack.add(resolvedFg);
            return true;
        }

        void pop() throws ProvisioningException {
            if(groupStack.isEmpty()) {
                throw new IllegalStateException("Feature group stack is empty");
            }
            final ResolvedFeatureGroupConfig last = groupStack.remove(groupStack.size() - 1);
            rt.processIncludedFeatures(last);
        }

        boolean isLayerFilteredOut(String layerName) {
            if(config == null) {
                return false;
            }
            return config.isInheritLayers() ? config.isLayerExcluded(layerName) : !config.isLayerIncluded(layerName);
        }

        boolean isLayerExcluded(String layerName) {
            return config == null ? false : config.isLayerExcluded(layerName);
        }

        boolean isFilteredOut(ResolvedSpecId specId, final ResolvedFeatureId id) {
            boolean included = false;
            for(int i = groupStack.size() - 1; i >= 0; --i) {
                final ResolvedFeatureGroupConfig fgConfig = groupStack.get(i);
                if (fgConfig.inheritFeatures) {
                    if (id != null && fgConfig.excludedFeatures.contains(id)) {
                        return true;
                    }
                    if (fgConfig.excludedSpecs.contains(specId)) {
                        if (id != null && fgConfig.includedFeatures.containsKey(id)) {
                            included = true;
                            continue;
                        }
                        return true;
                    }
                } else {
                    if (id != null && fgConfig.includedFeatures.containsKey(id)) {
                        included = true;
                        continue;
                    }
                    if (!fgConfig.includedSpecs.contains(specId)) {
                        return true;
                    }
                    if (id != null && fgConfig.excludedFeatures.contains(id)) {
                        return true;
                    }
                    included = true;
                }
            }
            if(included) {
                return false;
            }
            return config == null ? false : !config.isInheritFeatures();
        }

        private boolean isRelevant(ResolvedFeatureGroupConfig resolvedFg) {
            if(resolvedFg.fg.getId() == null) {
                return true;
            }
            for(int i = groupStack.size() - 1; i >= 0; --i) {
                final ResolvedFeatureGroupConfig stacked = groupStack.get(i);
                if (stacked.fg.getId() == null
                        || stacked.producer == null || resolvedFg.producer == null
                        || !stacked.producer.equals(resolvedFg.producer)
                        || !stacked.fg.getId().equals(resolvedFg.fg.getId())) {
                    continue;
                }
                return !resolvedFg.isSubsetOf(stacked);
            }
            return true;
        }

    }

    final ConfigId id;
    final ProvisioningRuntimeBuilder rt;

    Map<String, String> props = Collections.emptyMap();
    Map<String, ConfigId> configDeps = Collections.emptyMap();

    Map<ResolvedSpecId, SpecFeatures> specFeatures = new LinkedHashMap<>();
    private List<Map<ResolvedFeatureId, ResolvedFeature>> fgFeatures = new ArrayList<>();
    private int lastFg = -1;
    Map<ResolvedFeatureId, ResolvedFeature> features;
    private int featureIncludeCount = 0;

    private List<ConfigScope> configs = new ArrayList<>();
    private ConfigScope lastConfig;
    private ConfigScope lastProcessedScope;

    // features in the order they should be processed by the provisioning handlers
    private List<ResolvedFeature> orderedFeatures = null;

    private Set<ConfigId> addedLayers;
    private List<ConfigId> includedLayers;
    private Map<String, ConfigLayerDependency> layerDeps;

    // layers that were actually excluded from the top config model
    private final Set<String> topConfigExcludedLayers = new HashSet<>(0);
    private final boolean checkNotExcludedLayers;

    ConfigModelStack(ConfigId configId, ProvisioningRuntimeBuilder rt) throws ProvisioningException {
        this.id = configId;
        this.rt = rt;
        lastConfig = new ConfigScope(null);
        configs.add(lastConfig);
        newFgScope();
        this.checkNotExcludedLayers = rt.layout.getOptionValue(ProvisioningOption.IGNORE_NOT_EXCLUDED_LAYERS).equals(Constants.FALSE);
    }

    int size() {
        return configs.size();
    }

    boolean addLayer(ConfigId layerId) {
        return (addedLayers == null ? addedLayers = new LinkedHashSet<>() : addedLayers).add(layerId);
    }

    void addLayerDep(ConfigLayerDependency layerDep) {
        if(layerDeps == null) {
            layerDeps = Collections.singletonMap(layerDep.getName(), layerDep);
        } else {
            final ConfigLayerDependency existing = layerDeps.get(layerDep.getName());
            if(existing == null || existing.isOptional() && !layerDep.isOptional()) {
                layerDeps = CollectionUtils.putLinked(layerDeps, layerDep.getName(), layerDep);
            }
        }
    }

    boolean hasLayerDeps() {
        return layerDeps != null;
    }

    Collection<ConfigLayerDependency> getLayerDeps() {
        return layerDeps == null ? Collections.emptyList() : layerDeps.values();
    }

    void includedLayer(ConfigId layerId) {
        if(includedLayers == null) {
            includedLayers = new ArrayList<>(addedLayers.size());
        }
        includedLayers.add(layerId);
    }

    List<ConfigId> getIncludedLayers() {
        return includedLayers == null ? Collections.emptyList() : includedLayers;
    }

    boolean hasProperties() {
        return !props.isEmpty();
    }

    Map<String, String> getProperties() {
        return props;
    }

    void overwriteProps(Map<String, String> props) {
        if(props.isEmpty()) {
            return;
        }
        if(this.props.isEmpty()) {
            this.props = new HashMap<>(props.size());
        }
        this.props.putAll(props);
    }

    boolean hasConfigDeps() {
        return !configDeps.isEmpty();
    }

    Map<String, ConfigId> getConfigDeps() {
        return configDeps;
    }

    void overwriteConfigDeps(Map<String, ConfigId> configDeps) {
        if(configDeps.isEmpty()) {
            return;
        }
        if(this.configDeps.isEmpty()) {
            this.configDeps = new HashMap<>(configDeps.size());
        }
        this.configDeps.putAll(configDeps);
    }

    void pushConfig(ConfigModel model) throws ProvisioningException {
        lastConfig = new ConfigScope(model);
        configs.add(lastConfig);
    }

    ConfigModel popConfig() throws ProvisioningException {
        lastProcessedScope = lastConfig;
        final int poppedIndex = configs.size() - 1;
        if(checkNotExcludedLayers && poppedIndex == 1 && lastConfig.config.hasExcludedLayers()) {
            if(lastConfig.config.getExcludedLayers().size() != topConfigExcludedLayers.size()) {
                List<String> notExcluded = new ArrayList<>();
                for(String layer : lastConfig.config.getExcludedLayers()) {
                    if(!topConfigExcludedLayers.contains(layer)) {
                        notExcluded.add(layer);
                    }
                }
                throw new ProvisioningException(Errors.excludedLayersWouldNotOtherwiseBeInstalled(lastConfig.config.getId(), notExcluded));
            }
            topConfigExcludedLayers.clear();
        }
        configs.remove(poppedIndex);
        lastConfig = configs.get(poppedIndex - 1);
        lastProcessedScope.complete();
        return lastProcessedScope.config;
    }

    ConfigModel peekAtConfig() {
        return lastConfig.config;
    }

    boolean pushGroup(FeatureGroupSupport fg) throws ProvisioningException {
        if(!lastConfig.push(fg)) {
            return false;
        }
        newFgScope();
        return true;
    }

    FeatureGroupSupport peekAtGroup() {
        final ResolvedFeatureGroupConfig resolvedGroup = lastConfig.groupStack.get(lastConfig.groupStack.size() - 1);
        return resolvedGroup.fg;
    }

    void popGroup() throws ProvisioningException {
        mergeFgScope();
        lastConfig.pop();
    }

    private void newFgScope() {
        ++lastFg;
        if (fgFeatures.size() == lastFg) {
            features = new LinkedHashMap<>();
            fgFeatures.add(features);
        } else {
            features = fgFeatures.get(lastFg);
        }
    }

    private void mergeFgScope() throws ProvisioningException {
        if(lastFg <= 0) {
            return;
        }
        final Map<ResolvedFeatureId, ResolvedFeature> endedGroup = fgFeatures.get(lastFg--);
        final Map<ResolvedFeatureId, ResolvedFeature> parentGroup = fgFeatures.get(lastFg);
        for (Map.Entry<ResolvedFeatureId, ResolvedFeature> entry : endedGroup.entrySet()) {
            final ResolvedFeature parentFeature = parentGroup.get(entry.getKey());
            if (parentFeature == null) {
                parentGroup.put(entry.getKey(), entry.getValue());
                if (lastFg == 0) {
                    addToSpecFeatures(entry.getValue());
                }
            } else {
                parentFeature.merge(entry.getValue(), true);
            }
        }
        endedGroup.clear();
        features = parentGroup;
    }

    boolean includes(ResolvedFeatureId id) {
        return features.containsKey(id);
    }

    void addFeature(ResolvedFeature feature) throws ProvisioningDescriptionException {
        if(feature.id == null) {
            addToSpecFeatures(feature);
            return;
        }
        features.put(feature.id, feature);
        if (lastFg == 0) {
            addToSpecFeatures(feature);
        }
    }

    ResolvedFeature includeFeature(ResolvedFeatureId id, ResolvedFeatureSpec spec,
            Map<String, Object> resolvedParams, Map<ResolvedFeatureId, FeatureDependencySpec> resolvedDeps,
            Set<String> unsetParams, Set<String> resetParams)
            throws ProvisioningException {
        ResolvedFeature feature = null;
        if(id != null) {
            feature = features.get(id);
            if(feature != null) {
                feature.merge(resolvedDeps, resolvedParams, true);
            }
        }
        if(feature == null) {
            feature = new ResolvedFeature(id, spec, resolvedParams, resolvedDeps, ++featureIncludeCount);
            addFeature(feature);
        }
        if(!unsetParams.isEmpty()) {
            feature.unsetAllParams(unsetParams, true);
        }
        if (!resetParams.isEmpty()) {
            feature.resetAllParams(resetParams);
        }
        return feature;
    }

    boolean isFilteredOut(ResolvedSpecId specId, final ResolvedFeatureId id) {
        if(lastConfig.isFilteredOut(specId, id)) {
            return true;
        }
        if(configs.size() > 1) {
            for (int i = configs.size() - 2; i >= 0; --i) {
                if (configs.get(i).isFilteredOut(specId, id)) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean isLayerFilteredOut(String layerName) {
        final int configsTotal = configs.size();
        if(configsTotal == 1) {
            return false;
        }
        if(configsTotal == 2) {
            return isFilteredOutFromTopConfig(layerName, lastConfig.config);
        }
        if(lastConfig.isLayerFilteredOut(layerName)) {
            return true;
        }
        for (int i = configsTotal - 2; i > 1; --i) {
            if (configs.get(i).isLayerFilteredOut(layerName)) {
                return true;
            }
        }
        return isFilteredOutFromTopConfig(layerName, configs.get(1).config);
    }

    private boolean isFilteredOutFromTopConfig(String layerName, final ConfigModel config) {
        if(config.isInheritLayers()) {
            if(config.isLayerExcluded(layerName)) {
                if(checkNotExcludedLayers) {
                    topConfigExcludedLayers.add(layerName);
                }
                return true;
            }
        } else if(!config.isLayerIncluded(layerName)) {
            return true;
        }
        return false;
    }

    boolean isLayerExcluded(String layerName) {
        final int configsTotal = configs.size();
        if(configsTotal == 1) {
            return false;
        }
        if(lastConfig.isLayerExcluded(layerName)) {
            if(checkNotExcludedLayers && configsTotal == 2) {
                topConfigExcludedLayers.add(layerName);
            }
            return true;
        }
        for (int i = configsTotal - 2; i > 0; --i) {
            if (configs.get(i).isLayerExcluded(layerName)) {
                if (checkNotExcludedLayers && i == 1) {
                    topConfigExcludedLayers.add(layerName);
                }
                return true;
            }
        }
        return false;
    }

    void merge(ConfigModelStack other) throws ProvisioningException {
        if(!other.props.isEmpty()) {
            if(props.isEmpty()) {
                props = other.props;
            } else {
                for(Map.Entry<String, String> prop : other.props.entrySet()) {
                    if(!props.containsKey(prop.getKey())) {
                        props.put(prop.getKey(), prop.getValue());
                    }
                }
            }
        }
        if(!other.configDeps.isEmpty()) {
            if(configDeps.isEmpty()) {
                configDeps = other.configDeps;
            } else {
                for(Map.Entry<String, ConfigId> configDep : other.configDeps.entrySet()) {
                    if(!configDeps.containsKey(configDep.getKey())) {
                        configDeps.put(configDep.getKey(), configDep.getValue());
                    }
                }
            }
        }
        if(other.includedLayers != null && !other.includedLayers.isEmpty()) {
            for(ConfigId layerId : other.includedLayers) {
                if(addLayer(layerId)) {
                    includedLayer(layerId);
                }
            }
        }

        if(other.specFeatures.isEmpty()) {
            return;
        }
        for (Map.Entry<ResolvedSpecId, SpecFeatures> entry : other.specFeatures.entrySet()) {
            final SpecFeatures otherSpecFeatures = entry.getValue();
            SpecFeatures specFeatures = null;
            for (ResolvedFeature feature : otherSpecFeatures.getFeatures()) {
                if(lastProcessedScope.isFilteredOut(feature.getSpecId(), feature.id)) {
                    continue;
                }
                if(feature.id == null) {
                    if(specFeatures == null) {
                        specFeatures = getSpecFeatures(otherSpecFeatures.spec);
                    }
                    specFeatures.add(feature);
                    continue;
                }
                final ResolvedFeature localFeature = features.get(feature.id);
                if(localFeature == null) {
                    feature = feature.copy(++featureIncludeCount);
                    features.put(feature.id, feature);
                    if(specFeatures == null) {
                        specFeatures = getSpecFeatures(otherSpecFeatures.spec);
                    }
                    specFeatures.add(feature);
                } else {
                    localFeature.merge(feature, false);
                }
            }
        }
    }

    List<ResolvedFeature> orderFeatures(boolean arrange) throws ProvisioningException {
        if(orderedFeatures != null) {
            return orderedFeatures;
        }
        if (features.isEmpty()) {
            orderedFeatures = Collections.emptyList();
        } else if(arrange) {
            final String arranger = System.getProperty(Constants.PROP_CONFIG_ARRANGER);
            if(arranger == null) {
                orderedFeatures = new DefaultBranchedConfigArranger(this).orderFeatures();
            } else if(Constants.CONFIG_ARRANGER_SPEC_ONLY.equals(arranger)) {
                orderedFeatures = new SpecOnlyConfigArranger().orderFeatures(this);
            } else {
                throw new ProvisioningException("Unsupported config arranger " + arranger);
            }
        } else {
            orderedFeatures = new ArrayList<>(features.size());
            for(ResolvedFeature feature : features.values()) {
                orderedFeatures.add(feature);
            }
            // make sure features w/o IDs are also ordered
            for(Map.Entry<ResolvedSpecId, SpecFeatures> entry : specFeatures.entrySet()) {
                if(entry.getValue().spec.getSpec().hasId()) {
                    continue;
                }
                for (ResolvedFeature feature : entry.getValue().getFeatures()) {
                    orderedFeatures.add(feature);
                }
            }
        }
        return orderedFeatures;
    }

    private void addToSpecFeatures(final ResolvedFeature feature) {
        getSpecFeatures(feature.spec).add(feature);
    }

    SpecFeatures getSpecFeatures(final ResolvedFeatureSpec spec) {
        SpecFeatures sf = specFeatures.get(spec.id);
        if(sf == null) {
            sf = new SpecFeatures(spec);
            specFeatures.put(spec.id, sf);
        }
        return sf;
    }

    private boolean isRelevant(ResolvedFeatureGroupConfig resolvedFg) {
        if(resolvedFg.fg.getId() == null) {
            return true;
        }
        if(!lastConfig.isRelevant(resolvedFg)) {
            return false;
        }
        if(configs.size() > 1) {
            for (int i = configs.size() - 2; i >= 0; --i) {
                if (!configs.get(i).isRelevant(resolvedFg)) {
                    return false;
                }
            }
        }
        return true;
    }
}
