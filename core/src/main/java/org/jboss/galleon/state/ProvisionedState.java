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
package org.jboss.galleon.state;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.StringUtils;

/**
 * Represents provisioned installation.
 *
 * @author Alexey Loubyansky
 */
public class ProvisionedState implements FeaturePackSet<ProvisionedFeaturePack> {

    public static class Builder {
        private Map<FeaturePackLocation.ChannelSpec, ProvisionedFeaturePack> featurePacks = Collections.emptyMap();
        private List<ProvisionedConfig> configs = Collections.emptyList();

        private Builder() {
        }

        public Builder addFeaturePack(ProvisionedFeaturePack fp) {
            featurePacks = CollectionUtils.putLinked(featurePacks, fp.getFPID().getChannel(), fp);
            return this;
        }

        public Builder addConfig(ProvisionedConfig config) {
            configs = CollectionUtils.add(configs, config);
            return this;
        }

        public ProvisionedState build() {
            return new ProvisionedState(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final Map<FeaturePackLocation.ChannelSpec, ProvisionedFeaturePack> featurePacks;
    private final List<ProvisionedConfig> configs;

    ProvisionedState(Builder builder) {
        this.featurePacks = CollectionUtils.unmodifiable(builder.featurePacks);
        this.configs = CollectionUtils.unmodifiable(builder.configs);
    }

    @Override
    public boolean hasFeaturePacks() {
        return !featurePacks.isEmpty();
    }

    @Override
    public boolean hasFeaturePack(FeaturePackLocation.ChannelSpec channel) {
        return featurePacks.containsKey(channel);
    }

    @Override
    public Collection<ProvisionedFeaturePack> getFeaturePacks() {
        return featurePacks.values();
    }

    @Override
    public ProvisionedFeaturePack getFeaturePack(FeaturePackLocation.ChannelSpec channel) {
        return featurePacks.get(channel);
    }

    @Override
    public boolean hasConfigs() {
        return !configs.isEmpty();
    }

    @Override
    public List<ProvisionedConfig> getConfigs() {
        return configs;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((configs == null) ? 0 : configs.hashCode());
        result = prime * result + ((featurePacks == null) ? 0 : featurePacks.hashCode());
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
        ProvisionedState other = (ProvisionedState) obj;
        if (configs == null) {
            if (other.configs != null)
                return false;
        } else if (configs.size() != other.configs.size() || !configs.containsAll(other.configs))
            return false;
        if (featurePacks == null) {
            if (other.featurePacks != null)
                return false;
        } else if (!featurePacks.equals(other.featurePacks))
            return false;
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append("[state");
        if(!featurePacks.isEmpty()) {
            buf.append(" feature-packs=[");
            StringUtils.append(buf, featurePacks.values());
            buf.append(']');
        }
        if(!configs.isEmpty()) {
            buf.append(" configs=[");
            StringUtils.append(buf, configs);
            buf.append(']');
        }
        return buf.append(']').toString();
    }
}
