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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.spec.FeatureId;
import org.jboss.galleon.spec.PackageDepsSpec;
import org.jboss.galleon.spec.SpecId;
import org.jboss.galleon.util.CollectionUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class FeatureGroupSupport extends PackageDepsSpec implements ConfigItem, ConfigItemContainer {

    final String origin;
    final String name;

    // customizations of the dependencies
    final boolean inheritFeatures;
    final Set<SpecId> includedSpecs;
    final Map<FeatureId, FeatureConfig> includedFeatures;
    final Set<SpecId> excludedSpecs;
    final Map<FeatureId, String> excludedFeatures; // featureId and optional parent-ref
    final Map<String, FeatureGroup> externalFgConfigs;

    // added items
    protected final List<ConfigItem> items;

    protected FeatureGroupSupport(String origin, String name) {
        super();
        this.origin = origin;
        this.name = name;
        this.inheritFeatures = true;
        this.includedSpecs = Collections.emptySet();
        this.includedFeatures = Collections.emptyMap();
        this.excludedSpecs = Collections.emptySet();
        this.excludedFeatures = Collections.emptyMap();
        this.externalFgConfigs = Collections.emptyMap();
        this.items = Collections.emptyList();
    }

    protected FeatureGroupSupport(FeatureGroupBuilderSupport<?> builder) throws ProvisioningDescriptionException {
        super(builder);
        this.origin = builder.origin;
        this.name = builder.name;
        this.inheritFeatures = builder.inheritFeatures;
        this.includedSpecs = CollectionUtils.unmodifiable(builder.includedSpecs);
        this.excludedSpecs = CollectionUtils.unmodifiable(builder.excludedSpecs);
        this.includedFeatures = CollectionUtils.unmodifiable(builder.includedFeatures);
        this.excludedFeatures = CollectionUtils.unmodifiable(builder.excludedFeatures);

        if(builder.externalFgConfigs.isEmpty()) {
            this.externalFgConfigs = Collections.emptyMap();
        } else if(builder.externalFgConfigs.size() == 1) {
            final Map.Entry<String, FeatureGroup.Builder> entry = builder.externalFgConfigs.entrySet().iterator().next();
            this.externalFgConfigs = Collections.singletonMap(entry.getKey(), entry.getValue().build());
        } else {
            final Map<String, FeatureGroup> tmp = new LinkedHashMap<>(builder.externalFgConfigs.size());
            for(Map.Entry<String, FeatureGroup.Builder> entry : builder.externalFgConfigs.entrySet()) {
                tmp.put(entry.getKey(), entry.getValue().build());
            }
            this.externalFgConfigs = Collections.unmodifiableMap(tmp);
        }

        this.items = CollectionUtils.unmodifiable(builder.items);
    }

    public Object getId() {
        return name;
    }

    public boolean isConfig() {
        return false;
    }

    public boolean isLayer() {
        return false;
    }

    @Override
    public String getOrigin() {
        return origin;
    }

    @Override
    public boolean isGroup() {
        return true;
    }

    public String getName() {
        return name;
    }

    public boolean isInheritFeatures() {
        return inheritFeatures;
    }

    public boolean hasExcludedSpecs() {
        return !excludedSpecs.isEmpty();
    }

    public Set<SpecId> getExcludedSpecs() {
        return excludedSpecs;
    }

    public boolean hasIncludedSpecs() {
        return !includedSpecs.isEmpty();
    }

    public Set<SpecId> getIncludedSpecs() {
        return includedSpecs;
    }

    public boolean hasExcludedFeatures() {
        return !excludedFeatures.isEmpty();
    }

    public Map<FeatureId, String> getExcludedFeatures() {
        return excludedFeatures;
    }

    public boolean hasIncludedFeatures() {
        return !includedFeatures.isEmpty();
    }

    public Map<FeatureId, FeatureConfig> getIncludedFeatures() {
        return includedFeatures;
    }

    public boolean hasExternalFeatureGroups() {
        return !externalFgConfigs.isEmpty();
    }

    public Map<String, FeatureGroup> getExternalFeatureGroups() {
        return externalFgConfigs;
    }

    @Override
    public boolean hasItems() {
        return !items.isEmpty();
    }

    @Override
    public List<ConfigItem> getItems() {
        return items;
    }

    @Override
    public boolean isResetFeaturePackOrigin() {
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((excludedFeatures == null) ? 0 : excludedFeatures.hashCode());
        result = prime * result + ((excludedSpecs == null) ? 0 : excludedSpecs.hashCode());
        result = prime * result + ((externalFgConfigs == null) ? 0 : externalFgConfigs.hashCode());
        result = prime * result + ((origin == null) ? 0 : origin.hashCode());
        result = prime * result + ((includedFeatures == null) ? 0 : includedFeatures.hashCode());
        result = prime * result + ((includedSpecs == null) ? 0 : includedSpecs.hashCode());
        result = prime * result + (inheritFeatures ? 1231 : 1237);
        result = prime * result + ((items == null) ? 0 : items.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        FeatureGroupSupport other = (FeatureGroupSupport) obj;
        if (excludedFeatures == null) {
            if (other.excludedFeatures != null)
                return false;
        } else if (!excludedFeatures.equals(other.excludedFeatures))
            return false;
        if (excludedSpecs == null) {
            if (other.excludedSpecs != null)
                return false;
        } else if (!excludedSpecs.equals(other.excludedSpecs))
            return false;
        if (externalFgConfigs == null) {
            if (other.externalFgConfigs != null)
                return false;
        } else if (!externalFgConfigs.equals(other.externalFgConfigs))
            return false;
        if (origin == null) {
            if (other.origin != null)
                return false;
        } else if (!origin.equals(other.origin))
            return false;
        if (includedFeatures == null) {
            if (other.includedFeatures != null)
                return false;
        } else if (!includedFeatures.equals(other.includedFeatures))
            return false;
        if (includedSpecs == null) {
            if (other.includedSpecs != null)
                return false;
        } else if (!includedSpecs.equals(other.includedSpecs))
            return false;
        if (inheritFeatures != other.inheritFeatures)
            return false;
        if (items == null) {
            if (other.items != null)
                return false;
        } else if (!items.equals(other.items))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
}
