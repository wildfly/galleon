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
package org.jboss.galleon.xml;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.runtime.ResolvedSpecId;
import org.jboss.galleon.state.ProvisionedFeature;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.StringUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class ProvisionedFeatureBuilder implements ProvisionedFeature {

    public static ProvisionedFeatureBuilder builder(ResolvedFeatureId id) {
        return new ProvisionedFeatureBuilder(id, id.getSpecId());
    }

    public static ProvisionedFeatureBuilder builder(ResolvedSpecId id) {
        return new ProvisionedFeatureBuilder(null, id);
    }

    private final ResolvedSpecId specId;

    private ResolvedFeatureId id;
    private ResolvedFeatureId.Builder idBuilder;

    private Map<String, String> configParams = Collections.emptyMap();

    private ProvisionedFeatureBuilder(ResolvedFeatureId id, ResolvedSpecId specId) {
        this.id = id;
        this.specId = specId;
        if(id != null) {
            Map<String, Object> idParams = id.getParams();
            if(idParams.size() > 1) {
                configParams = new HashMap<>(idParams.size());
                for(Map.Entry<String, Object> entry : idParams.entrySet()) {
                    configParams.put(entry.getKey(), (String) entry.getValue());
                }
            } else {
                final Map.Entry<String, Object> entry = idParams.entrySet().iterator().next();
                configParams = Collections.singletonMap(entry.getKey(), (String) entry.getValue());
            }
            idBuilder = null;
        } else {
            idBuilder = ResolvedFeatureId.builder(specId);
        }
    }

    /**
     * Sets the parameter's configuration value
     *
     * @param name  configuration parameter name
     * @param value  configuration parameter value
     * @return this builder
     */
    public ProvisionedFeatureBuilder setConfigParam(String name, String value) {
        configParams = CollectionUtils.put(configParams, name, value);
        return this;
    }

    /**
     * Sets the ID parameter's resolved value.
     * @param name  id parameter name
     * @param value  id parameter value
     * @return this builder
     */
    public ProvisionedFeatureBuilder setIdParam(String name, String value) {
        if(idBuilder == null) {
            throw new IllegalStateException("The ID builder has not been initialized");
        }
        idBuilder.setParam(name, value);
        setConfigParam(name, value);
        return this;
    }

    public ProvisionedFeature build() throws ProvisioningDescriptionException {
        if(idBuilder != null) {
            if(!idBuilder.isEmpty()) {
                id = idBuilder.build();
            }
            idBuilder = null;
        }
        configParams = Collections.unmodifiableMap(configParams);
        return this;
    }

    @Override
    public boolean hasId() {
        return id != null;
    }

    @Override
    public ResolvedFeatureId getId() {
        return id;
    }

    @Override
    public ResolvedSpecId getSpecId() {
        return specId;
    }

    @Override
    public boolean hasParams() {
        return !configParams.isEmpty();
    }

    @Override
    public Collection<String> getParamNames() {
        return configParams.keySet();
    }

    @Override
    public Object getResolvedParam(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getConfigParam(String name) throws ProvisioningException {
        return configParams.get(name);
    }

    public Map<String, String> getConfigParams() {
        return configParams;
    }

    @Override
    public Map<String, Object> getResolvedParams() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((configParams == null) ? 0 : configParams.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((specId == null) ? 0 : specId.hashCode());
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
        ProvisionedFeatureBuilder other = (ProvisionedFeatureBuilder) obj;
        if (configParams == null) {
            if (other.configParams != null)
                return false;
        } else if (!configParams.equals(other.configParams))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (specId == null) {
            if (other.specId != null)
                return false;
        } else if (!specId.equals(other.specId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append('[');
        if(id != null) {
            buf.append(id);
        } else {
            buf.append(specId);
        }
        if(!configParams.isEmpty()) {
            buf.append(" config-params:{");
            StringUtils.append(buf, configParams.entrySet());
            buf.append('}');
        }
        return buf.append(']').toString();
    }
}
