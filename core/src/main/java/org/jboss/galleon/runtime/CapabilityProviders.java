/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.runtime;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jboss.galleon.util.CollectionUtils;


/**
 * @author Alexey Loubyansky
 *
 */
class CapabilityProviders {

    // specs providing the capability
    List<SpecFeatures> specs = Collections.emptyList();
    // features providing the capability of specs that don't provide the capability
    List<ResolvedFeature> features = Collections.emptyList();

    private ConfigFeatureBranch firstProvided; // this is just a short cut
    Set<ConfigFeatureBranch> branches = Collections.emptySet();

    private List<ResolvedFeature> branchDependees = Collections.emptyList();

    void addBranchDependee(ResolvedFeature feature) {
        branchDependees = CollectionUtils.add(branchDependees, feature);
    }

    void add(SpecFeatures specFeatures) {
        specs = CollectionUtils.add(specs, specFeatures);
        specFeatures.spec.addCapabilityProviders(this);
    }

    void add(ResolvedFeature feature) {
        features = CollectionUtils.add(features, feature);
        feature.addCapabilityProviders(this);
    }

    void provided(ConfigFeatureBranch branch) {
        if(firstProvided != null) {
            branches = CollectionUtils.add(branches, branch);
            return;
        }
        firstProvided = branch;
        branches = Collections.singleton(branch);
        if (!branchDependees.isEmpty()) {
            for (ResolvedFeature branchDependee : branchDependees) {
                branchDependee.addBranchDep(branch, false);
            }
            branchDependees = Collections.emptyList();
        }
    }

    boolean isProvided() {
        return !branches.isEmpty();
    }
}
