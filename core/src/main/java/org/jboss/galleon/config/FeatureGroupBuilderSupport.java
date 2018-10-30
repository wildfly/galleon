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
package org.jboss.galleon.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.spec.FeatureId;
import org.jboss.galleon.spec.PackageDepsSpecBuilder;
import org.jboss.galleon.spec.SpecId;
import org.jboss.galleon.util.CollectionUtils;

import java.util.Set;

/**
 * @author Alexey Loubyansky
 *
 */
public abstract class FeatureGroupBuilderSupport<B extends FeatureGroupBuilderSupport<B>>
    extends PackageDepsSpecBuilder<B>
    implements ConfigItemContainerBuilder<B> {

    protected String origin;
    protected String name;

    // dependency customizations
    protected boolean inheritFeatures = true;
    protected Set<SpecId> includedSpecs = Collections.emptySet();
    protected Map<FeatureId, FeatureConfig> includedFeatures = Collections.emptyMap();
    protected Set<SpecId> excludedSpecs = Collections.emptySet();
    protected Map<FeatureId, String> excludedFeatures = Collections.emptyMap();
    protected Map<String, FeatureGroup.Builder> externalFgConfigs = Collections.emptyMap();

    // added items
    protected List<ConfigItem> items = Collections.emptyList();

    protected FeatureGroupBuilderSupport() {
    }

    protected FeatureGroupBuilderSupport(String name) {
        this.name = name;
    }

    protected FeatureGroupBuilderSupport(FeatureGroupSupport fg) {
        super(fg);
        this.name = fg.name;
        this.origin = fg.origin;
        this.inheritFeatures = fg.inheritFeatures;
        this.includedSpecs = CollectionUtils.clone(fg.includedSpecs);
        this.includedFeatures = CollectionUtils.clone(fg.includedFeatures);
        this.excludedSpecs = CollectionUtils.clone(fg.excludedSpecs);
        this.excludedFeatures = CollectionUtils.clone(fg.excludedFeatures);
        if(!fg.externalFgConfigs.isEmpty()) {
            for(Map.Entry<String, FeatureGroup> entry : fg.externalFgConfigs.entrySet()) {
                externalFgConfigs = CollectionUtils.putLinked(externalFgConfigs, entry.getKey(), FeatureGroup.builder(entry.getValue()));
            }
        }
        this.items = CollectionUtils.clone(fg.items);
    }

    @SuppressWarnings("unchecked")
    public B setOrigin(String origin) {
        this.origin = origin;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B setName(String name) {
        this.name = name;
        return (B) this;
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("unchecked")
    public B setInheritFeatures(boolean inheritFeatures) {
        this.inheritFeatures = inheritFeatures;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B includeSpec(String origin, String spec) throws ProvisioningDescriptionException {
        if(origin == null) {
            return includeSpec(spec);
        }
        getExternalFgConfig(origin).includeSpec(spec);
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B includeSpec(String spec) throws ProvisioningDescriptionException {
        final SpecId specId = SpecId.fromString(spec);
        if(excludedSpecs.contains(specId)) {
            throw new ProvisioningDescriptionException(specId + " spec has been explicitly excluded");
        }
        includedSpecs = CollectionUtils.addLinked(includedSpecs, specId);
        return (B) this;
    }

    public B includeFeature(FeatureId featureId) throws ProvisioningDescriptionException {
        return includeFeature(featureId, null);
    }

    public B includeFeature(String origin, FeatureId featureId) throws ProvisioningDescriptionException {
        return includeFeature(origin, featureId, null);
    }

    @SuppressWarnings("unchecked")
    private B includeFeature(String origin, FeatureId featureId, FeatureConfig feature) throws ProvisioningDescriptionException {
        if(origin == null) {
            return includeFeature(featureId, feature);
        }
        getExternalFgConfig(origin).includeFeature(featureId, feature);
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B includeFeature(FeatureId featureId, FeatureConfig feature) throws ProvisioningDescriptionException {
        if(feature != null && feature.getOrigin() != null) {
            final String origin = feature.getOrigin();
            feature.setOrigin(null);
            getExternalFgConfig(origin).includeFeature(featureId, feature);
            return (B) this;
        }
        if(excludedFeatures.containsKey(featureId)) {
            throw new ProvisioningDescriptionException(featureId + " has been explicitly excluded");
        }
        if(feature == null) {
            feature = new FeatureConfig(featureId.getSpec());
        } else if(feature.specId == null) {
            feature.specId = featureId.getSpec();
        }

        for (Map.Entry<String, String> idEntry : featureId.getParams().entrySet()) {
            final String prevValue = feature.putParam(idEntry.getKey(), idEntry.getValue());
            if (prevValue != null && !prevValue.equals(idEntry.getValue())) {
                throw new ProvisioningDescriptionException("Parameter " + idEntry.getKey() + " has value '"
                        + idEntry.getValue() + "' in feature ID and value '" + prevValue + "' in the feature body");
            }
        }
        includedFeatures = CollectionUtils.putLinked(includedFeatures, featureId, feature);
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B excludeSpec(String origin, String spec) throws ProvisioningDescriptionException {
        if(origin == null) {
            return excludeSpec(spec);
        }
        getExternalFgConfig(origin).excludeSpec(spec);
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B excludeSpec(String spec) throws ProvisioningDescriptionException {
        final SpecId specId = SpecId.fromString(spec);
        if(includedSpecs.contains(specId)) {
            throw new ProvisioningDescriptionException(specId + " spec has been inplicitly excluded");
        }
        excludedSpecs = CollectionUtils.add(excludedSpecs, specId);
        return (B) this;
    }

    public B excludeFeature(String origin, FeatureId featureId) throws ProvisioningDescriptionException {
        return excludeFeature(origin, featureId, null);
    }

    @SuppressWarnings("unchecked")
    public B excludeFeature(String origin, FeatureId featureId, String parentRef) throws ProvisioningDescriptionException {
        if(origin == null) {
            return excludeFeature(featureId, parentRef);
        }
        getExternalFgConfig(origin).excludeFeature(featureId, parentRef);
        return (B) this;
    }

    public B excludeFeature(FeatureId featureId) throws ProvisioningDescriptionException {
        return excludeFeature(featureId, null);
    }

    @SuppressWarnings("unchecked")
    public B excludeFeature(FeatureId featureId, String parentRef) throws ProvisioningDescriptionException {
        if(includedFeatures.containsKey(featureId)) {
            throw new ProvisioningDescriptionException(featureId + " has been explicitly included");
        }
        excludedFeatures = CollectionUtils.put(excludedFeatures, featureId, parentRef);
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B removeExcludedFeature(FeatureId featureId) throws ProvisioningDescriptionException {
        excludedFeatures = CollectionUtils.remove(excludedFeatures, featureId);
        return (B) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public B addConfigItem(ConfigItem item) {
        items = CollectionUtils.add(items, item);
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B removeFeature(FeatureId id) throws ProvisioningDescriptionException {
        int index = -1;
        // Although that is a list, we have a single ConfigModel for a given ConfigId
        for (int i = 0; i < items.size(); i++) {
            ConfigItem ci = items.get(i);
            if (ci instanceof FeatureConfig) {
                FeatureConfig conf = (FeatureConfig) ci;
                if (conf.getSpecId().equals(id.getSpec())) {
                    boolean eq = true;
                    for (Entry<String, String> entry : id.getParams().entrySet()) {
                        String val = conf.getParam(entry.getKey());
                        if (val == null || !val.equals(entry.getValue())) {
                            eq = false;
                            break;
                        }
                    }
                    if (eq) {
                        index = i;
                        break;
                    }
                }
            }
        }
        if (index == -1) {
            throw new ProvisioningDescriptionException("Feature " + id + " is not added");
        }
        items = CollectionUtils.remove(items, index);
        return (B) this;
    }


    private FeatureGroup.Builder getExternalFgConfig(String origin) {
        FeatureGroup.Builder fgBuilder = externalFgConfigs.get(origin);
        if(fgBuilder != null) {
            return fgBuilder;
        }
        fgBuilder = FeatureGroup.builder(inheritFeatures);
        externalFgConfigs = CollectionUtils.putLinked(externalFgConfigs, origin, fgBuilder);
        return fgBuilder;
    }
}
