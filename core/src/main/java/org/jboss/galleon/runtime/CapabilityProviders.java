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
