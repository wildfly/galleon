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

import java.util.Iterator;
import java.util.Map;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.spec.FeatureId;
import org.jboss.galleon.util.StringUtils;

/**
 * @author Alexey Loubyansky
 *
 */
public class FeatureGroup extends FeatureGroupSupport {

    public static class Builder extends FeatureGroupBuilderSupport<Builder> {

        protected Builder() {
            super();
        }

        protected Builder(String featureGroupName) {
            super(featureGroupName);
        }

        protected Builder(String featureGroupName, boolean inheritFeatures) {
            super(featureGroupName);
            this.inheritFeatures = inheritFeatures;
        }

        public FeatureGroup build() throws ProvisioningDescriptionException {
            return new FeatureGroup(this);
        }
    }

    public static Builder builder() {
        return new Builder(null, true);
    }

    public static Builder builder(boolean inheritFeatures) {
        return new Builder(null, inheritFeatures);
    }

    public static Builder builder(String featureGroupName) {
        return builder(featureGroupName, true);
    }

    public static Builder builder(String featureGroupName, boolean inheritFeatures) {
        return new Builder(featureGroupName, inheritFeatures);
    }

    public static FeatureGroup forGroup(String featureGroupName) {
        return new FeatureGroup(null, featureGroupName);
    }

    public static FeatureGroup forGroup(String origin, String featureGroupName) {
        return new FeatureGroup(origin, featureGroupName);
    }

    public FeatureGroup(String origin, String name) {
        super(origin, name);
    }

    protected FeatureGroup(FeatureGroupBuilderSupport<?> builder) throws ProvisioningDescriptionException {
        super(builder);
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append('[');
        if(name != null) {
            buf.append(name);
        }
        if(origin != null) {
            buf.append(" origin=").append(origin);
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
