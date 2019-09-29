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
package org.jboss.galleon.config;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.StringUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class ConfigCustomizations {

    protected final Boolean inheritConfigs;
    protected final boolean inheritModelOnlyConfigs;
    protected final Set<String> includedModels;
    protected final Map<String, Boolean> excludedModels;
    protected final Set<ConfigId> includedConfigs;
    protected final Set<ConfigId> excludedConfigs;
    protected final Map<ConfigId, ConfigModel> definedConfigs;
    protected final boolean hasModelOnlyConfigs;

    protected ConfigCustomizations(ConfigCustomizationsBuilder<?> builder) {
        this.inheritConfigs = builder.inheritConfigs;
        this.inheritModelOnlyConfigs = builder.inheritModelOnlyConfigs;
        this.includedModels = CollectionUtils.unmodifiable(builder.includedModels);
        this.excludedModels = CollectionUtils.unmodifiable(builder.excludedModels);
        this.includedConfigs = CollectionUtils.unmodifiable(builder.includedConfigs);
        this.excludedConfigs = CollectionUtils.unmodifiable(builder.excludedConfigs);
        this.definedConfigs = CollectionUtils.unmodifiable(builder.definedConfigs);
        this.hasModelOnlyConfigs = builder.hasModelOnlyConfigs;
    }

    public boolean isInheritConfigs(boolean defaultValue) {
        return inheritConfigs == null ? defaultValue : inheritConfigs.booleanValue();
    }

    public Boolean getInheritConfigs() {
        return inheritConfigs;
    }

    public boolean isInheritModelOnlyConfigs() {
        return inheritModelOnlyConfigs;
    }

    public boolean hasFullModelsIncluded() {
        return !includedModels.isEmpty();
    }

    public Set<String> getFullModelsIncluded() {
        return includedModels;
    }

    public boolean isConfigModelIncluded(ConfigId configId) {
        return includedModels.contains(configId.getModel());
    }

    public boolean hasFullModelsExcluded() {
        return !excludedModels.isEmpty();
    }

    public Map<String, Boolean> getFullModelsExcluded() {
        return excludedModels;
    }

    public boolean isConfigModelExcluded(ConfigId configId) {
        final Boolean namedOnly = excludedModels.get(configId.getModel());
        if(namedOnly == null) {
            return false;
        }
        return namedOnly ? configId.getName() != null : true;
    }

    public boolean hasExcludedConfigs() {
        return !excludedConfigs.isEmpty();
    }

    public boolean isConfigExcluded(ConfigId configId) {
        return excludedConfigs.contains(configId);
    }

    public Set<ConfigId> getExcludedConfigs() {
        return excludedConfigs;
    }

    public boolean hasIncludedConfigs() {
        return !includedConfigs.isEmpty();
    }

    public boolean isConfigIncluded(ConfigId id) {
        return includedConfigs.contains(id);
    }

    public Set<ConfigId> getIncludedConfigs() {
        return includedConfigs;
    }

    public boolean hasDefinedConfigs() {
        return !definedConfigs.isEmpty();
    }

    public Collection<ConfigModel> getDefinedConfigs() {
        return definedConfigs.values();
    }

    public boolean hasDefinedConfig(ConfigId configId) {
        return definedConfigs.containsKey(configId);
    }

    public ConfigModel getDefinedConfig(ConfigId configId) {
        return definedConfigs.get(configId);
    }

    public boolean hasModelOnlyConfigs() {
        return hasModelOnlyConfigs;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((definedConfigs == null) ? 0 : definedConfigs.hashCode());
        result = prime * result + ((excludedConfigs == null) ? 0 : excludedConfigs.hashCode());
        result = prime * result + ((excludedModels == null) ? 0 : excludedModels.hashCode());
        result = prime * result + ((includedConfigs == null) ? 0 : includedConfigs.hashCode());
        result = prime * result + ((includedModels == null) ? 0 : includedModels.hashCode());
        result = prime * result + ((inheritConfigs == null) ? 0 : inheritConfigs.hashCode());
        result = prime * result + (inheritModelOnlyConfigs ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ConfigCustomizations other = (ConfigCustomizations) obj;
        if (definedConfigs == null) {
            if (other.definedConfigs != null)
                return false;
        } else if (!definedConfigs.equals(other.definedConfigs))
            return false;
        if (excludedConfigs == null) {
            if (other.excludedConfigs != null)
                return false;
        } else if (!excludedConfigs.equals(other.excludedConfigs))
            return false;
        if (excludedModels == null) {
            if (other.excludedModels != null)
                return false;
        } else if (!excludedModels.equals(other.excludedModels))
            return false;
        if (includedConfigs == null) {
            if (other.includedConfigs != null)
                return false;
        } else if (!includedConfigs.equals(other.includedConfigs))
            return false;
        if (includedModels == null) {
            if (other.includedModels != null)
                return false;
        } else if (!includedModels.equals(other.includedModels))
            return false;
        if (inheritConfigs == null) {
            if (other.inheritConfigs != null)
                return false;
        } else if (!inheritConfigs.equals(other.inheritConfigs))
            return false;
        if (inheritModelOnlyConfigs != other.inheritModelOnlyConfigs)
            return false;
        return true;
    }

    protected void append(StringBuilder builder) {
        if(inheritConfigs != null) {
            builder.append(" inheritConfigs=").append(inheritConfigs);
        }
        if(!inheritModelOnlyConfigs) {
            builder.append(" inheritModelOnlyConfigs=false");
        }
        if(!this.excludedModels.isEmpty()) {
            builder.append(" excludedModels ");
            StringUtils.append(builder, excludedModels.entrySet());
        }
        if(!this.includedModels.isEmpty()) {
            builder.append(" includedModels ");
            StringUtils.append(builder, includedModels);
        }
        if(!excludedConfigs.isEmpty()) {
            builder.append(" excludedConfigs ");
            StringUtils.append(builder, excludedConfigs);
        }
        if(!includedConfigs.isEmpty()) {
            builder.append(" includedConfigs ");
            StringUtils.append(builder, includedConfigs);
        }
        if(!definedConfigs.isEmpty()) {
            builder.append(" definedConfigs ");
            StringUtils.append(builder, definedConfigs.values());
        }
    }
}
