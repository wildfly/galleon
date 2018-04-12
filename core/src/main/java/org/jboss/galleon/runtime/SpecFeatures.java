/*
 * Copyright ${license.git.copyrightYears} Red Hat, Inc. and/or its affiliates
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
        branch.setSpecId(spec.id);
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
