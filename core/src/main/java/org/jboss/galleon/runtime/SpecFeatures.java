/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.runtime;

import java.util.ArrayList;
import java.util.List;

import org.jboss.galleon.ProvisioningException;

/**
 *
 * @author Alexey Loubyansky
 */
class SpecFeatures {

    private static final byte FREE = 0;
    private static final byte PROCESSING = 1;

    final ResolvedFeatureSpec spec;
    private final List<ResolvedFeature> features = new ArrayList<>();
    private ConfigFeatureBranch branch;
    private byte state = FREE;

    SpecFeatures(ResolvedFeatureSpec spec) {
        this.spec = spec;
    }

    boolean isFree() {
        return state == FREE;
    }

    void schedule() {
        state = PROCESSING;
    }

    void free() {
        state = FREE;
    }

    void setBranch(ConfigFeatureBranch branch) throws ProvisioningException {
        if(this.branch != null) {
            throw new ProvisioningException("The branch has already been set for " + spec.id);
        }
        this.branch = branch;
        branch.setSpecBranch();
    }

    public ConfigFeatureBranch getBranch() {
        return branch;
    }

    boolean isBranchSet() {
        return branch != null;
    }

    void add(ResolvedFeature feature) {
        features.add(feature);
        feature.setSpecFeatures(this);
    }

    List<ResolvedFeature> getFeatures() {
        return features;
    }
}
