/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.state;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.StringUtils;

/**
 * Represents provisioned installation.
 *
 * @author Alexey Loubyansky
 */
public class ProvisionedState implements FeaturePackSet<ProvisionedFeaturePack> {

    public static class Builder {
        private Map<ProducerSpec, ProvisionedFeaturePack> featurePacks = Collections.emptyMap();
        private List<ProvisionedConfig> configs = Collections.emptyList();

        private Builder() {
        }

        public Builder addFeaturePack(ProvisionedFeaturePack fp) {
            featurePacks = CollectionUtils.putLinked(featurePacks, fp.getFPID().getProducer(), fp);
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

    private final Map<ProducerSpec, ProvisionedFeaturePack> featurePacks;
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
    public boolean hasFeaturePack(ProducerSpec producer) {
        return featurePacks.containsKey(producer);
    }

    @Override
    public Collection<ProvisionedFeaturePack> getFeaturePacks() {
        return featurePacks.values();
    }

    @Override
    public ProvisionedFeaturePack getFeaturePack(ProducerSpec producer) {
        return featurePacks.get(producer);
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
        } else if (featurePacks.size() != other.featurePacks.size())
            return false;
        final Iterator<Map.Entry<ProducerSpec, ProvisionedFeaturePack>> i = featurePacks.entrySet().iterator();
        final Iterator<Map.Entry<ProducerSpec, ProvisionedFeaturePack>> otherI = other.featurePacks.entrySet().iterator();
        while(i.hasNext()) {
            if(!i.next().equals(otherI.next())) {
                return false;
            }
        }
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
