/*
 * Copyright 2016-2023 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.api.config;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.jboss.galleon.BaseErrors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.StringUtils;

/**
 * @author Alexey Loubyansky
 *
 */
public class GalleonConfigurationWithLayersBuilder implements GalleonConfigurationWithLayersBuilderItf {

    static class GalleonConfigurationImpl implements GalleonConfigurationWithLayers {

        final ConfigId id;
        final Map<String, String> props;
        final Map<String, ConfigId> configDeps;
        final boolean inheritLayers;
        final Set<String> includedLayers;
        final Set<String> excludedLayers;

        protected GalleonConfigurationImpl(GalleonConfigurationWithLayersBuilder builder) throws ProvisioningDescriptionException {
            this.id = new ConfigId(builder.model, builder.name);
            this.props = CollectionUtils.unmodifiable(builder.props);
            this.configDeps = CollectionUtils.unmodifiable(builder.configDeps);
            this.inheritLayers = builder.inheritLayers;
            this.includedLayers = CollectionUtils.unmodifiable(builder.includedLayers);
            this.excludedLayers = CollectionUtils.unmodifiable(builder.excludedLayers);
        }

        public ConfigId getId() {
            return id;
        }

        public String getModel() {
            return id.getModel();
        }

        public String getName() {
            return id.getName();
        }

        public boolean hasProperties() {
            return !props.isEmpty();
        }

        public Map<String, String> getProperties() {
            return props;
        }

        public boolean hasConfigDeps() {
            return !configDeps.isEmpty();
        }

        public Map<String, ConfigId> getConfigDeps() {
            return configDeps;
        }

        public boolean isInheritLayers() {
            return inheritLayers;
        }

        public boolean hasIncludedLayers() {
            return !includedLayers.isEmpty();
        }

        public Set<String> getIncludedLayers() {
            return includedLayers;
        }

        public boolean isLayerIncluded(String layerName) {
            return includedLayers.contains(layerName);
        }

        public boolean hasExcludedLayers() {
            return !excludedLayers.isEmpty();
        }

        public Set<String> getExcludedLayers() {
            return excludedLayers;
        }

        public boolean isLayerExcluded(String layerName) {
            return excludedLayers.contains(layerName);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((configDeps == null) ? 0 : configDeps.hashCode());
            result = prime * result + ((excludedLayers == null) ? 0 : excludedLayers.hashCode());
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            result = prime * result + ((includedLayers == null) ? 0 : includedLayers.hashCode());
            result = prime * result + (inheritLayers ? 1231 : 1237);
            result = prime * result + ((props == null) ? 0 : props.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            GalleonConfigurationImpl other = (GalleonConfigurationImpl) obj;
            if (configDeps == null) {
                if (other.configDeps != null) {
                    return false;
                }
            } else if (!configDeps.equals(other.configDeps)) {
                return false;
            }
            if (excludedLayers == null) {
                if (other.excludedLayers != null) {
                    return false;
                }
            } else if (!excludedLayers.equals(other.excludedLayers)) {
                return false;
            }
            if (id == null) {
                if (other.id != null) {
                    return false;
                }
            } else if (!id.equals(other.id)) {
                return false;
            }
            if (includedLayers == null) {
                if (other.includedLayers != null) {
                    return false;
                }
            } else if (!includedLayers.equals(other.includedLayers)) {
                return false;
            }
            if (inheritLayers != other.inheritLayers) {
                return false;
            }
            if (props == null) {
                if (other.props != null) {
                    return false;
                }
            } else if (!props.equals(other.props)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            final StringBuilder buf = new StringBuilder();
            buf.append("[model=").append(id.getModel()).append(" name=").append(id.getName());
            if (!props.isEmpty()) {
                buf.append(" props=");
                StringUtils.append(buf, props.entrySet());
            }
            if (!configDeps.isEmpty()) {
                buf.append(" config-deps=");
                StringUtils.append(buf, configDeps.entrySet());
            }
            if (!inheritLayers) {
                buf.append(" inherit-layers=false");
            }
            if (!includedLayers.isEmpty()) {
                buf.append(" included-layers=");
                StringUtils.append(buf, includedLayers);
            }
            if (!excludedLayers.isEmpty()) {
                buf.append(" excluded-layers=");
                StringUtils.append(buf, excludedLayers);
            }
            return buf.append(']').toString();
        }
    }
    private String model;
    private String name;
    private Map<String, String> props = Collections.emptyMap();
    private Map<String, ConfigId> configDeps = Collections.emptyMap();
    private boolean inheritLayers = true;
    private Set<String> includedLayers = Collections.emptySet();
    private Set<String> excludedLayers = Collections.emptySet();

    protected GalleonConfigurationWithLayersBuilder() {
        super();
    }

    protected GalleonConfigurationWithLayersBuilder(String model, String name) {
        this.name = name;
        this.model = model;
    }

    protected GalleonConfigurationWithLayersBuilder(GalleonConfigurationWithLayers config) {
        this.model = config.getModel();
        this.props = CollectionUtils.clone(config.getProperties());
        this.configDeps = CollectionUtils.clone(config.getConfigDeps());
        this.inheritLayers = config.isInheritLayers();
        this.includedLayers = CollectionUtils.clone(config.getIncludedLayers());
        this.excludedLayers = CollectionUtils.clone(config.getExcludedLayers());
    }

    public String getName() {
        return name;
    }

    public GalleonConfigurationWithLayersBuilder setModel(String model) {
        this.model = model;
        return this;
    }

    public GalleonConfigurationWithLayersBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public GalleonConfigurationWithLayersBuilder setProperty(String name, String value) {
        props = CollectionUtils.put(props, name, value);
        return this;
    }

    public GalleonConfigurationWithLayersBuilder setConfigDep(String depName, ConfigId configId) {
        configDeps = CollectionUtils.putLinked(configDeps, depName, configId);
        return this;
    }

    public GalleonConfigurationWithLayersBuilder setInheritLayers(boolean inheritLayers) {
        this.inheritLayers = inheritLayers;
        return this;
    }

    public GalleonConfigurationWithLayersBuilder includeLayer(String layerName) throws ProvisioningDescriptionException {
        if (excludedLayers.contains(layerName)) {
            throw new ProvisioningDescriptionException(BaseErrors.configLayerCanEitherBeIncludedOrExcluded(model, getName(), layerName));
        }
        includedLayers = CollectionUtils.addLinked(includedLayers, layerName);
        return this;
    }

    public GalleonConfigurationWithLayersBuilder removeIncludedLayer(String layer) {
        includedLayers = CollectionUtils.remove(includedLayers, layer);
        return this;
    }

    public GalleonConfigurationWithLayersBuilder removeExcludedLayer(String layer) {
        excludedLayers = CollectionUtils.remove(excludedLayers, layer);
        return this;
    }

    public GalleonConfigurationWithLayersBuilder excludeLayer(String layerName) throws ProvisioningDescriptionException {
        if (includedLayers.contains(layerName)) {
            throw new ProvisioningDescriptionException(BaseErrors.configLayerCanEitherBeIncludedOrExcluded(model, getName(), layerName));
        }
        excludedLayers = CollectionUtils.addLinked(excludedLayers, layerName);
        return this;
    }

    public GalleonConfigurationWithLayers build() throws ProvisioningDescriptionException {
        return new GalleonConfigurationImpl(this);
    }

    public static GalleonConfigurationWithLayersBuilder builder() {
        return new GalleonConfigurationWithLayersBuilder();
    }

    public static GalleonConfigurationWithLayersBuilder builder(String model, String name) {
        return new GalleonConfigurationWithLayersBuilder(model, name);
    }

    public static GalleonConfigurationWithLayersBuilder builder(GalleonConfigurationWithLayers config) {
        return new GalleonConfigurationWithLayersBuilder(config);
    }
}
