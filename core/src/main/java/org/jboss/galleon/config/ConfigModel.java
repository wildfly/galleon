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
import java.util.Iterator;
import java.util.Map;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.spec.FeatureId;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.StringUtils;

/**
 * @author Alexey Loubyansky
 *
 */
public class ConfigModel extends FeatureGroupSupport {

    public static final String BRANCH_PER_SPEC = "config.branch-per-spec";
    public static final String BRANCH_IS_BATCH = "config.branch-is-batch";
    public static final String ISOLATE_CIRCULAR_DEPS = "config.isolate-circular-deps";

    public static class Builder extends FeatureGroupBuilderSupport<Builder> {

        private String model;
        private Map<String, String> props = Collections.emptyMap();
        private Map<String, ConfigId> configDeps = Collections.emptyMap();

        protected Builder() {
            super();
        }

        protected Builder(String model, String name) {
            super(name);
            this.model = model;
        }

        public Builder setModel(String model) {
            this.model = model;
            return this;
        }

        @Override
        public Builder setProperty(String name, String value) {
            props = CollectionUtils.put(props, name, value);
            return this;
        }

        public Builder setConfigDep(String depName, ConfigId configId) {
            configDeps = CollectionUtils.put(configDeps, depName, configId);
            return this;
        }

        public ConfigModel build() throws ProvisioningDescriptionException {
            return new ConfigModel(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(String model, String name) {
        return new Builder(model, name);
    }

    final ConfigId id;
    final Map<String, String> props;
    final Map<String, ConfigId> configDeps;
    private final Builder builder;

    protected ConfigModel(Builder builder) throws ProvisioningDescriptionException {
        super(builder);
        this.id = new ConfigId(builder.model, builder.name);
        this.props = CollectionUtils.unmodifiable(builder.props);
        this.configDeps = CollectionUtils.unmodifiable(builder.configDeps);
        this.builder = builder;
    }

    public Builder getBuilder() {
        return builder;
    }

    public ConfigId getId() {
        return id;
    }

    public String getModel() {
        return id.getModel();
    }

    @Override
    public boolean hasProperties() {
        return !props.isEmpty();
    }

    @Override
    public Map<String, String> getProperties() {
        return props;
    }

    @Override
    public boolean isConfig() {
        return true;
    }

    public boolean hasConfigDeps() {
        return !configDeps.isEmpty();
    }

    public Map<String, ConfigId> getConfigDeps() {
        return configDeps;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((configDeps == null) ? 0 : configDeps.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((props == null) ? 0 : props.hashCode());
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
        ConfigModel other = (ConfigModel) obj;
        if (configDeps == null) {
            if (other.configDeps != null)
                return false;
        } else if (!configDeps.equals(other.configDeps))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (props == null) {
            if (other.props != null)
                return false;
        } else if (!props.equals(other.props))
            return false;
        return true;
    }


    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append("[model=").append(id.getModel()).append(" name=").append(id.getName());
        if(origin != null) {
            buf.append(" origin=").append(origin);
        }
        if(!props.isEmpty()) {
            buf.append(" props=");
            StringUtils.append(buf, props.entrySet());
        }
        if(!configDeps.isEmpty()) {
            buf.append(" config-deps=");
            StringUtils.append(buf, configDeps.entrySet());
        }
        if(!inheritFeatures) {
            buf.append(" inherit-features=false");
        }
        if(!includedSpecs.isEmpty()) {
            buf.append(" includedSpecs=");
            StringUtils.append(buf, includedSpecs);
        }
        if(!excludedSpecs.isEmpty()) {
            buf.append(" exlcudedSpecs=");
            StringUtils.append(buf, excludedSpecs);
        }
        if(!includedFeatures.isEmpty()) {
            buf.append(" includedFeatures=[");
            final Iterator<Map.Entry<FeatureId, FeatureConfig>> i = includedFeatures.entrySet().iterator();
            Map.Entry<FeatureId, FeatureConfig> entry = i.next();
            buf.append(entry.getKey());
            if(entry.getValue() != null) {
                buf.append("->").append(entry.getValue());
            }
            while(i.hasNext()) {
                entry = i.next();
                buf.append(';').append(entry.getKey());
                if(entry.getValue() != null) {
                    buf.append("->").append(entry.getValue());
                }
            }
            buf.append(']');
        }
        if(!excludedFeatures.isEmpty()) {
            buf.append(" exlcudedFeatures=");
            StringUtils.append(buf, excludedFeatures.keySet());
        }

        if(!items.isEmpty()) {
            buf.append(" items=");
            StringUtils.append(buf, items);
        }
        return buf.append(']').toString();
    }
}
