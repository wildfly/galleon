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
package org.jboss.galleon.spec;

import java.util.Collections;
import java.util.Map;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.StringUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeatureReferenceSpec {

    public static class Builder {

        private String origin;
        private final String featureSpec;
        private String name;
        private boolean nillable;
        private boolean include;
        private Map<String, String> mappedParams = Collections.emptyMap();

        private Builder(String spec) {
            this.featureSpec = spec;
            this.name = featureSpec;
        }

        public Builder setOrigin(String origin) {
            this.origin = origin;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setNillable(boolean nillable) {
            this.nillable = nillable;
            return this;
        }

        public Builder setInclude(boolean include) {
            this.include = include;
            return this;
        }

        public Builder mapParam(String localName, String targetName) {
            mappedParams = CollectionUtils.put(mappedParams, localName, targetName);
            return this;
        }

        public FeatureReferenceSpec build() throws ProvisioningDescriptionException {
            return new FeatureReferenceSpec(origin, name, featureSpec, nillable, include, mappedParams);
        }
    }

    public static Builder builder(String feature) {
        return new Builder(feature);
    }

    public static FeatureReferenceSpec create(String str) throws ProvisioningDescriptionException {
        return create(str, str, false);
    }

    public static FeatureReferenceSpec create(String str, boolean nillable) throws ProvisioningDescriptionException {
        return create(str, str, nillable);
    }

    public static FeatureReferenceSpec create(String name, String feature, boolean nillable) throws ProvisioningDescriptionException {
        return new FeatureReferenceSpec(null, name, feature, nillable, false, Collections.emptyMap());
    }

    final String origin;
    final String name;
    final SpecId feature;
    final boolean nillable;
    final boolean include;
    final Map<String, String> mappedParams;

    private FeatureReferenceSpec(String origin, String name, String featureSpec, boolean nillable, boolean include, Map<String, String> paramMapping) throws ProvisioningDescriptionException {
        this.origin = origin;
        this.name = name;
        this.feature = SpecId.fromString(featureSpec);
        this.nillable = nillable;
        this.include = include;
        this.mappedParams = CollectionUtils.unmodifiable(paramMapping);
    }

    public String getOrigin() {
        return origin;
    }

    public String getName() {
        return name;
    }

    public SpecId getFeature() {
        return feature;
    }

    public boolean isNillable() {
        return nillable;
    }

    public boolean isInclude() {
        return include;
    }

    public boolean hasMappedParams() {
        return !mappedParams.isEmpty();
    }

    public int getParamsMapped() {
        return mappedParams.size();
    }

    public Map<String,String> getMappedParams() {
        return mappedParams;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((origin == null) ? 0 : origin.hashCode());
        result = prime * result + ((feature == null) ? 0 : feature.hashCode());
        result = prime * result + (include ? 1231 : 1237);
        result = prime * result + ((mappedParams == null) ? 0 : mappedParams.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (nillable ? 1231 : 1237);
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
        FeatureReferenceSpec other = (FeatureReferenceSpec) obj;
        if (origin == null) {
            if (other.origin != null)
                return false;
        } else if (!origin.equals(other.origin))
            return false;
        if (feature == null) {
            if (other.feature != null)
                return false;
        } else if (!feature.equals(other.feature))
            return false;
        if (include != other.include)
            return false;
        if (mappedParams == null) {
            if (other.mappedParams != null)
                return false;
        } else if (!mappedParams.equals(other.mappedParams))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (nillable != other.nillable)
            return false;
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append('[').append(name);
        if(origin != null) {
            buf.append(" origin=").append(origin);
        }
        buf.append(" feature=").append(feature);
        if(nillable) {
            buf.append(" nillable");
        }
        if(include) {
            buf.append(" auto-includes ");
        }
        if(!mappedParams.isEmpty()) {
            buf.append(' ');
            StringUtils.append(buf, mappedParams.entrySet());
        }
        return buf.append(']').toString();
    }
}
