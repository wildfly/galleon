/*
 * Copyright ${license.git.copyrightYears} Red Hat, Inc. and/or its affiliates
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
import java.util.Set;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.util.CollectionUtils;

/**
 * @author Alexey Loubyansky
 *
 */
public abstract class ConfigCustomizationsBuilder<B extends ConfigCustomizationsBuilder<B>> {

    protected boolean inheritConfigs = true;
    protected boolean inheritModelOnlyConfigs = true;
    protected Set<String> includedModels = Collections.emptySet();
    protected Set<ConfigId> includedConfigs = Collections.emptySet();
    protected Map<String, Boolean> excludedModels = Collections.emptyMap();
    protected Set<ConfigId> excludedConfigs = Collections.emptySet();
    protected List<ConfigModel> definedConfigs = Collections.emptyList();
    protected boolean hasModelOnlyConfigs = false;

    @SuppressWarnings("unchecked")
    public B setInheritConfigs(boolean inherit) {
        this.inheritConfigs = inherit;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B setInheritModelOnlyConfigs(boolean inheritModelOnlyConfigs) {
        this.inheritModelOnlyConfigs = inheritModelOnlyConfigs;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B addConfig(ConfigModel config) throws ProvisioningDescriptionException {
        definedConfigs = CollectionUtils.add(definedConfigs, config);
        this.hasModelOnlyConfigs |= config.id.isModelOnly();
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B removeConfig(ConfigId id) throws ProvisioningDescriptionException {
        int index = getDefinedConfigIndex(id);
        definedConfigs = CollectionUtils.remove(definedConfigs, index);
        // reset flag
        hasModelOnlyConfigs = false;
        for (ConfigModel cm : definedConfigs) {
            hasModelOnlyConfigs |= cm.id.isModelOnly();
        }
        return (B) this;
    }

    public int getDefinedConfigIndex(ConfigId id) throws ProvisioningDescriptionException {
        int index = -1;
        for (int i = 0; i < definedConfigs.size(); i++) {
            ConfigModel cm = definedConfigs.get(i);
            if (cm.getId().equals(id)) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            throw new ProvisioningDescriptionException("Config " + id + " is not added");
        }
        return index;
    }

    @SuppressWarnings("unchecked")
    public B addConfig(int index, ConfigModel config) throws ProvisioningDescriptionException {
        definedConfigs = CollectionUtils.add(definedConfigs, index, config);
        this.hasModelOnlyConfigs |= config.id.isModelOnly();
        return (B) this;
    }

    public B excludeConfigModel(String model) throws ProvisioningDescriptionException {
        return excludeConfigModel(model, true);
    }

    @SuppressWarnings("unchecked")
    public B excludeConfigModel(String model, boolean namedConfigsOnly) throws ProvisioningDescriptionException {
        if(includedModels.contains(model)) {
            throw new ProvisioningDescriptionException("Model " + model + " has been included");
        }
        excludedModels = CollectionUtils.put(excludedModels, model, namedConfigsOnly);
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B includeConfigModel(String name) throws ProvisioningDescriptionException {
        if(excludedModels.containsKey(name)) {
            throw new ProvisioningDescriptionException("Model " + name + " has been excluded");
        }
        includedModels = CollectionUtils.add(includedModels, name);
        return (B) this;
    }

    public B includeDefaultConfig(String model, String name) throws ProvisioningDescriptionException {
        return includeDefaultConfig(new ConfigId(model, name));
    }

    @SuppressWarnings("unchecked")
    public B includeDefaultConfig(ConfigId configId) throws ProvisioningDescriptionException {
        if(includedConfigs.contains(configId)) {
            throw new ProvisioningDescriptionException("Config model with id " + configId + " has already been included into the configuration");
        }
        includedConfigs = CollectionUtils.add(includedConfigs, configId);
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B removeIncludedDefaultConfig(ConfigId configId) throws ProvisioningDescriptionException {
        if (!includedConfigs.contains(configId)) {
            throw new ProvisioningDescriptionException("Config model with id " + configId + " is not included into the configuration");
        }
        includedConfigs = CollectionUtils.remove(includedConfigs, configId);
        return (B) this;
    }

    public B excludeDefaultConfig(String model, String name) {
        return excludeDefaultConfig(new ConfigId(model, name));
    }

    @SuppressWarnings("unchecked")
    public B removeExcludedDefaultConfig(ConfigId configId) throws ProvisioningDescriptionException {
        if (!excludedConfigs.contains(configId)) {
            throw new ProvisioningDescriptionException("Config model with id " + configId + " is not excluded from the configuration");
        }
        excludedConfigs = CollectionUtils.remove(excludedConfigs, configId);
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B excludeDefaultConfig(ConfigId configId) {
        excludedConfigs = CollectionUtils.add(excludedConfigs, configId);
        return (B) this;
    }
}
