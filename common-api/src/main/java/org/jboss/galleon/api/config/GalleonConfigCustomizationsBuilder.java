/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.api.config;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.util.CollectionUtils;

/**
 * @author Alexey Loubyansky
 *
 */
public abstract class GalleonConfigCustomizationsBuilder<B extends GalleonConfigCustomizationsBuilder<B>> {

    protected Boolean inheritConfigs;
    protected boolean inheritModelOnlyConfigs = true;
    protected Set<String> includedModels = Collections.emptySet();
    protected Set<ConfigId> includedConfigs = Collections.emptySet();
    protected Map<String, Boolean> excludedModels = Collections.emptyMap();
    protected Set<ConfigId> excludedConfigs = Collections.emptySet();
    protected Map<ConfigId, GalleonConfigurationWithLayers> definedConfigs = Collections.emptyMap();
    protected boolean hasModelOnlyConfigs;

    @SuppressWarnings("unchecked")
    public B initConfigs(GalleonConfigCustomizations clone) {
        inheritConfigs = clone.inheritConfigs;
        inheritModelOnlyConfigs = clone.inheritModelOnlyConfigs;
        includedModels = CollectionUtils.clone(clone.includedModels);
        includedConfigs = CollectionUtils.clone(clone.includedConfigs);
        excludedModels = CollectionUtils.clone(clone.excludedModels);
        excludedConfigs = CollectionUtils.clone(clone.excludedConfigs);
        definedConfigs = CollectionUtils.clone(clone.definedConfigs);
        hasModelOnlyConfigs = clone.hasModelOnlyConfigs;
        return (B) this;
    }

    protected void resetConfigs() {
        inheritConfigs = null;
        inheritModelOnlyConfigs = true;
        includedModels = Collections.emptySet();
        includedConfigs = Collections.emptySet();
        excludedModels = Collections.emptyMap();
        excludedConfigs = Collections.emptySet();
        definedConfigs = Collections.emptyMap();
        hasModelOnlyConfigs = false;
    }

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
    public B addConfig(GalleonConfigurationWithLayers config) throws ProvisioningDescriptionException {
        final ConfigId id = new ConfigId(config.getModel(), config.getName());
        definedConfigs = CollectionUtils.putLinked(definedConfigs, id, config);
        this.hasModelOnlyConfigs |= id.isModelOnly();
        return (B) this;
    }

    public boolean hasDefinedConfigs() {
        return !definedConfigs.isEmpty();
    }

    public Collection<GalleonConfigurationWithLayers> getDefinedConfigs() {
        return definedConfigs.values();
    }

    @SuppressWarnings("unchecked")
    public B removeConfig(ConfigId id) throws ProvisioningDescriptionException {
        definedConfigs = CollectionUtils.remove(definedConfigs, id);
        // reset flag
        hasModelOnlyConfigs = false;
        for (GalleonConfigurationWithLayers cm : definedConfigs.values()) {
            hasModelOnlyConfigs |= GalleonConfigurationWithLayers.toId(cm).isModelOnly();
        }
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B removeAllConfigs() {
        definedConfigs = Collections.emptyMap();
        hasModelOnlyConfigs = false;
        return (B) this;
    }

    public B excludeConfigModel(String model) throws ProvisioningDescriptionException {
        return excludeConfigModel(model, true);
    }

    @SuppressWarnings("unchecked")
    public B excludeConfigModel(String model, boolean namedConfigsOnly) throws ProvisioningDescriptionException {
        if (includedModels.contains(model)) {
            throw new ProvisioningDescriptionException("Model " + model + " has been included");
        }
        excludedModels = CollectionUtils.put(excludedModels, model, namedConfigsOnly);
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B includeConfigModel(String name) throws ProvisioningDescriptionException {
        if (excludedModels.containsKey(name)) {
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
        if (includedConfigs.contains(configId)) {
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

    public boolean isDefaultConfigIncluded(ConfigId configId) {
        return includedConfigs.contains(configId);
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

    public boolean isDefaultConfigExcluded(ConfigId configId) {
        return excludedConfigs.contains(configId);
    }

}
