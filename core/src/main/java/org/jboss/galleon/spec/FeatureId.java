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
package org.jboss.galleon.spec;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.StringUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeatureId {

    public static FeatureId fromString(String str) throws ProvisioningDescriptionException {

        final int length = str.length();
        if(length == 0) {
            formatException(str);
        }

        int nextIndex = 0;
        char c = str.charAt(nextIndex++);
        final StringBuilder buf = new StringBuilder(length);
        String specId = null;
        while(nextIndex < length) {
            if(c == ':') {
                if(buf.length() == 0) {
                    formatException(str);
                }
                specId = buf.toString();
                break;
            } else {
                buf.append(c);
            }
            c = str.charAt(nextIndex++);
        }

        if(specId == null) {
            formatException(str);
        }

        int endIndex = str.indexOf(',', nextIndex + 3);
        if(endIndex < 0) {
            final int equals = str.indexOf('=', nextIndex + 1);
            if(equals < 0 || equals == str.length() - 1) {
                formatException(str);
            }
            return FeatureId.create(specId.toString(), str.substring(nextIndex, equals), str.substring(equals + 1));
        }

        final Builder builder = FeatureId.builder(specId.toString());
        int lastComma = nextIndex - 1;
        while(endIndex > 0) {
            int equals = str.indexOf('=', lastComma + 2);
            if(equals < 0 || equals == str.length() - 1) {
                formatException(str);
            }
            builder.setParam(str.substring(lastComma + 1, equals),  str.substring(equals + 1, endIndex));
            lastComma = endIndex;
            endIndex = str.indexOf(',', endIndex + 1);
        }

        int equals = str.indexOf('=', lastComma + 2);
        if(equals < 0 || equals == str.length() - 1) {
            formatException(str);
        }
        builder.setParam(str.substring(lastComma + 1, equals),  str.substring(equals + 1));
        return builder.build();
    }

    private static void formatException(String s) throws ProvisioningDescriptionException {
        throw new ProvisioningDescriptionException('\'' + s + "' does not follow format spec_name:param_name=value(,param_name=value)*");
    }

    public static class Builder {

        private final String specId;
        private Map<String, String> params = Collections.emptyMap();

        private Builder(String specId) throws ProvisioningDescriptionException {
            this.specId = specId;
        }

        public Builder setParam(String name, String value) {
            params = CollectionUtils.put(params, name, value);
            return this;
        }

        public FeatureId build() throws ProvisioningDescriptionException {
            return new FeatureId(specId, params);
        }
    }

    public static Builder builder(String specId) throws ProvisioningDescriptionException {
        return new Builder(specId);
    }

    public static FeatureId create(String specId, String name, String value) throws ProvisioningDescriptionException {
        return new FeatureId(specId, Collections.singletonMap(name, value));
    }

    final SpecId specId;
    final Map<String, String> params;

    public FeatureId(String specName, Map<String, String> params) throws ProvisioningDescriptionException {
        if(params.isEmpty()) {
            throw new ProvisioningDescriptionException("ID paramaters are missing");
        }
        this.specId = SpecId.fromString(specName);
        this.params = CollectionUtils.unmodifiable(params);
    }

    public SpecId getSpec() {
        return specId;
    }

    public Collection<String> getParamNames() {
        return params.keySet();
    }

    public String getParam(String name) {
        return params.get(name);
    }

    public Map<String, String> getParams() {
        return params;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((params == null) ? 0 : params.hashCode());
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
        FeatureId other = (FeatureId) obj;
        if (params == null) {
            if (other.params != null)
                return false;
        } else if (!params.equals(other.params))
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
        buf.append(specId);
        if (!params.isEmpty()) {
            buf.append(':');
            StringUtils.append(buf, params.entrySet());
        }
        return buf.toString();
    }
}
