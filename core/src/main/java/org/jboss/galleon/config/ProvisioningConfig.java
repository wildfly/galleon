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
import java.util.Map;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.StringUtils;

/**
 * The configuration of the installation to be provisioned.
 *
 * @author Alexey Loubyansky
 */
public class ProvisioningConfig extends FeaturePackDepsConfig {

    public static class Builder extends FeaturePackDepsConfigBuilder<Builder> {

        private Map<String, String> pluginOptions = Collections.emptyMap();

        private Builder() {
        }

        private Builder(ProvisioningConfig original) throws ProvisioningDescriptionException {
            if(original == null) {
                return;
            }
            if(original.hasPluginOptions()) {
                addPluginOptions(original.getPluginOptions());
            }
            for (FeaturePackConfig fp : original.getFeaturePackDeps()) {
                addFeaturePackDep(original.originOf(fp.getLocation().getProducer()), fp);
            }
            if (original.hasTransitiveDeps()) {
                for (FeaturePackConfig fp : original.getTransitiveDeps()) {
                    addFeaturePackDep(original.originOf(fp.getLocation().getProducer()), fp);
                }
            }
            initUniverses(original);
            initConfigs(original);
        }

        public Builder addPluginOption(String name, String value) {
            pluginOptions = CollectionUtils.put(pluginOptions, name, value);
            return this;
        }

        public Builder removePluginOption(String name) {
            pluginOptions = CollectionUtils.remove(pluginOptions, name);
            return this;
        }

        public Builder addPluginOptions(Map<String, String> options) {
            pluginOptions = CollectionUtils.putAll(pluginOptions, options);
            return this;
        }

        public ProvisioningConfig build() throws ProvisioningDescriptionException {
            return new ProvisioningConfig(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Allows to build a provisioning configuration starting from the passed in
     * initial configuration.
     *
     * @param provisioningConfig  initial state of the configuration to be built
     * @return  this builder instance
     * @throws ProvisioningDescriptionException  in case the config couldn't be built
     */
    public static Builder builder(ProvisioningConfig provisioningConfig) throws ProvisioningDescriptionException {
        return new Builder(provisioningConfig);
    }

    private final Map<String, String> pluginOptions;
    private final Builder builder;

    private ProvisioningConfig(Builder builder) throws ProvisioningDescriptionException {
        super(builder);
        this.pluginOptions = CollectionUtils.unmodifiable(builder.pluginOptions);
        this.builder = builder;
    }

    public Builder getBuilder() {
        return builder;
    }

    public boolean hasPluginOptions() {
        return !pluginOptions.isEmpty();
    }

    public Map<String, String> getPluginOptions() {
        return pluginOptions;
    }

    public boolean hasPluginOption(String name) {
        return pluginOptions.containsKey(name);
    }

    public String getPluginOption(String name) {
        return pluginOptions.get(name);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((pluginOptions == null) ? 0 : pluginOptions.hashCode());
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
        ProvisioningConfig other = (ProvisioningConfig) obj;
        if (pluginOptions == null) {
            if (other.pluginOptions != null)
                return false;
        } else if (!pluginOptions.equals(other.pluginOptions))
            return false;
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder().append('[');
        if(defaultUniverse != null) {
            buf.append("default-universe=").append(defaultUniverse);
        }
        if(!universeSpecs.isEmpty()) {
            if(defaultUniverse != null) {
                buf.append(' ');
            }
            buf.append("universes=[");
            StringUtils.append(buf, universeSpecs.entrySet());
            buf.append("] ");
        }
        if(!pluginOptions.isEmpty()) {
            buf.append("plugin-options=");
            StringUtils.append(buf, pluginOptions.entrySet());
        }
        if(!transitiveDeps.isEmpty()) {
            buf.append("transitive=");
            StringUtils.append(buf, transitiveDeps.values());
            buf.append(' ');
        }
        StringUtils.append(buf, fpDeps.values());
        append(buf);
        return buf.append(']').toString();
    }
}
