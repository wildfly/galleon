/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.runtime;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeatureGroupSupport;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;

/**
 *
 * @author Alexey Loubyansky
 */
public class ResolvedFeatureGroupConfig {

    final ConfigModelStack configStack;
    final ProducerSpec producer;
    final FeatureGroupSupport fg;

    boolean inheritFeatures = true;
    Set<ResolvedSpecId> includedSpecs = Collections.emptySet();
    Map<ResolvedFeatureId, FeatureConfig> includedFeatures = Collections.emptyMap();
    Set<ResolvedSpecId> excludedSpecs = Collections.emptySet();
    Set<ResolvedFeatureId> excludedFeatures = Collections.emptySet();

    ResolvedFeatureGroupConfig(ConfigModelStack configStack, FeatureGroupSupport fg, ProducerSpec producer) {
        this.configStack = configStack;
        this.producer = producer;
        this.fg = fg;
    }

    boolean isSubsetOf(ResolvedFeatureGroupConfig other) {
        if(this.fg.getId() == null || other.fg.getId() == null) {
            throw new IllegalArgumentException("Can't compare group anonymous groups");
        }
        if(inheritFeatures) {
            if(other.inheritFeatures) {
                if(!excludedSpecs.containsAll(other.excludedSpecs)) {
                    return false;
                }
                if(!excludedFeatures.containsAll(other.excludedFeatures)) {
                    return false;
                }
                if(!includedFeatures.isEmpty()) {
                    if(other.includedFeatures.isEmpty()) {
                        return false;
                    }
                    for(Map.Entry<ResolvedFeatureId, FeatureConfig> entry : includedFeatures.entrySet()) {
                        final FeatureConfig otherFc = other.includedFeatures.get(entry.getKey());
                        if(otherFc == null) {
                            return false;
                        }
                        if(!otherFc.equals(entry.getValue())) {
                            return false;
                        }
                    }
                }
            } else {
                return false;
            }
        } else if(other.inheritFeatures) {
            if(!includedSpecs.isEmpty() && !other.excludedSpecs.isEmpty()) {
                for (ResolvedSpecId specId : includedSpecs) {
                    if (excludedSpecs.contains(specId)) {
                        return false;
                    }
                }
            }
            if(!includedFeatures.isEmpty() && !other.excludedFeatures.isEmpty()) {
                for (Map.Entry<ResolvedFeatureId, FeatureConfig> entry : includedFeatures.entrySet()) {
                    if (other.excludedFeatures.contains(entry.getKey())) {
                        return false;
                    }
                }
            }
        } else {
            if(!other.includedSpecs.containsAll(includedSpecs)) {
                return false;
            }
            if(!includedFeatures.isEmpty()) {
                if(other.includedFeatures.isEmpty()) {
                    return false;
                }
                for(Map.Entry<ResolvedFeatureId, FeatureConfig> entry : includedFeatures.entrySet()) {
                    final FeatureConfig otherFc = other.includedFeatures.get(entry.getKey());
                    if(otherFc == null) {
                        return false;
                    }
                    if(!otherFc.equals(entry.getValue())) {
                        return false;
                    }
                }
            }
            if(!excludedFeatures.containsAll(other.excludedFeatures)) {
                return false;
            }
        }
        return true;
    }
}
