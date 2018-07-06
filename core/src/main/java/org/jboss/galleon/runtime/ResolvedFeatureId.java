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
package org.jboss.galleon.runtime;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.Constants;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;
import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.StringUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class ResolvedFeatureId {

    public static class Builder {
        private final ResolvedSpecId specId;
        private Map<String, Object> params = Collections.emptyMap();

        private Builder(ProducerSpec producer, String spec) {
            this.specId = new ResolvedSpecId(producer, spec);
        }

        private Builder(ResolvedSpecId specId) {
            this.specId = specId;
        }

        public Builder setParam(String name, Object value) {
            params = CollectionUtils.put(params, name, value);
            return this;
        }

        public boolean isEmpty() {
            return params.isEmpty();
        }

        public ResolvedFeatureId build() throws ProvisioningDescriptionException {
            if(params.isEmpty()) {
                throw new ProvisioningDescriptionException("Failed to create an instance of ResolvedFeatureId for " + specId + ": params have not been initialized");
            }
            return new ResolvedFeatureId(specId, params);
        }
    }

    public static Builder builder(ResolvedSpecId specId) {
        return new Builder(specId);
    }

    public static Builder builder(ProducerSpec producer, String spec) {
        return new Builder(producer, spec);
    }

    public static ResolvedFeatureId fromString(String str) throws ProvisioningDescriptionException {
        final int length = str.length();
        if(length == 0) {
            formatException(str);
        }

        if(str.charAt(0) != '{') {
            formatException(str);
        }

        int i = str.indexOf('}', 1);
        if(i < 0) {
            formatException(str);
        }

        final int colon = str.indexOf(':', i + 1);
        if(colon - i < 2 ) {
            formatException(str);
        }
        ResolvedSpecId specId = null;
        try {
            specId = new ResolvedSpecId(FeaturePackLocation.fromString(str.substring(1, i)).getProducer(), str.substring(i + 1, colon));
        } catch (IllegalArgumentException e) {
            throw new ProvisioningDescriptionException("Failed to parse the channel part of feature id '" + str + "'", e);
        }

        int endIndex = str.indexOf(',', colon + 3);
        if(endIndex < 0) {
            final int equals = str.indexOf('=', colon + 1);
            if(equals < 0 || equals == str.length() - 1) {
                formatException(str);
            }
            return new ResolvedFeatureId(specId, Collections.singletonMap(str.substring(colon + 1, equals), str.substring(equals + 1)));
        }

        final Map<String, Object> params = new HashMap<>();
        int lastComma = colon;
        while(endIndex > 0) {
            int equals = str.indexOf('=', lastComma + 1);
            if(equals < 0 || equals == str.length() - 1) {
                formatException(str);
            }
            params.put(str.substring(lastComma + 1, equals),  str.substring(equals + 1, endIndex));
            lastComma = endIndex;
            endIndex = str.indexOf(',', endIndex + 1);
        }

        int equals = str.indexOf('=', lastComma + 2);
        if(equals < 0 || equals == str.length() - 1) {
            formatException(str);
        }
        params.put(str.substring(lastComma + 1, equals),  str.substring(equals + 1));
        return new ResolvedFeatureId(specId, params);
    }

    public static ResolvedFeatureId fromGalleon1String(String str) throws ProvisioningDescriptionException {
        final int length = str.length();
        if(length == 0) {
            formatException(str);
        }

        int nextIndex = 0;
        char c = str.charAt(nextIndex++);
        final StringBuilder buf = new StringBuilder(length);
        ResolvedSpecId specId = null;
        String groupId = null;
        String artifactId = null;
        String version = null;
        while(nextIndex < length) {
            if(c == '#') {
                if(artifactId == null || version != null || buf.length() == 0) {
                    formatException(str);
                }
                version = buf.toString();
                buf.setLength(0);
            } else if(c == ':') {
                if(buf.length() == 0) {
                    formatException(str);
                }
                if(groupId == null) {
                    groupId = buf.toString();
                } else if(artifactId == null) {
                    artifactId = buf.toString();
                } else if(version == null) {
                    formatException(str);
                } else {
                    specId = new ResolvedSpecId(LegacyGalleon1Universe.toFpl(ArtifactCoords.newGav(groupId, artifactId, version)).getProducer(), buf.toString());
                    break;
                }
                buf.setLength(0);
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
            return new ResolvedFeatureId(specId, Collections.singletonMap(str.substring(nextIndex, equals), str.substring(equals + 1)));
        }

        final Map<String, Object> params = new HashMap<>(2);
        int lastComma = nextIndex - 1;
        while(endIndex > 0) {
            int equals = str.indexOf('=', lastComma + 2);
            if(equals < 0 || equals == str.length() - 1) {
                formatException(str);
            }
            params.put(str.substring(lastComma + 1, equals),  str.substring(equals + 1, endIndex));
            lastComma = endIndex;
            endIndex = str.indexOf(',', endIndex + 1);
        }

        int equals = str.indexOf('=', lastComma + 2);
        if(equals < 0 || equals == str.length() - 1) {
            formatException(str);
        }
        params.put(str.substring(lastComma + 1, equals),  str.substring(equals + 1));
        return new ResolvedFeatureId(specId, params);
    }

    private static void formatException(String str) throws ProvisioningDescriptionException {
        throw new ProvisioningDescriptionException("'" + str + "' does not follow format {producer[@universe]:channel}spec_name:param_name=value(,param_name=value)*");
    }

    public static ResolvedFeatureId create(ProducerSpec producer, String spec, String param, String value) {
        return new ResolvedFeatureId(new ResolvedSpecId(producer, spec), Collections.singletonMap(param, value));
    }

    public static ResolvedFeatureId create(ResolvedSpecId specId, String param, String value) {
        return new ResolvedFeatureId(specId, Collections.singletonMap(param, value));
    }

    final ResolvedSpecId specId;
    final Map<String, Object> params;
    final Boolean child;
    private final int hash;

    ResolvedFeatureId(ResolvedSpecId specId, Map<String, Object> params) {
        this(specId, params, null);
    }

    ResolvedFeatureId(ResolvedSpecId specId, Map<String, Object> params, Boolean child) {
        this.specId = specId;
        Map<String, Object> filtered = Collections.emptyMap(); // TODO
        for(Map.Entry<String, Object> entry : params.entrySet()) {
            if(!Constants.GLN_UNDEFINED.equals(entry.getValue())) {
                filtered = CollectionUtils.put(filtered, entry.getKey(), entry.getValue());
            }
        }
        if(filtered.isEmpty()) {
            filtered = params;
        }
        this.params = CollectionUtils.unmodifiable(filtered);
        this.child = child;

        final int prime = 31;
        int hash = 1;
        hash = prime * hash + this.params.hashCode();
        hash = prime * hash + this.specId.hashCode();
        this.hash = hash;
    }

    public ResolvedSpecId getSpecId() {
        return specId;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    boolean isChildRef() {
        return child != null && child;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ResolvedFeatureId other = (ResolvedFeatureId) obj;
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
