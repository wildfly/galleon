/*
 * Copyright 2016-2026 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.layout;

import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;

/**
 *
 * @author jdenise
 */
public abstract class FamilyLayoutOrderingTestBase extends LayoutOrderingTestBase {

    @Override
    protected void assertLayout(ProvisioningLayout<FeaturePackLayout> layout) throws Exception {
        for (FeaturePackLayout fl : layout.getOrderedFeaturePacks()) {
            // Do we have a maven mapping
            ProducerSpec maven = layout.getFPLToMavenMappings().get(fl.getFPID().getProducer());
            if (maven != null) {
                FeaturePackLayout fl2 = layout.getMavenProducers().get(maven);
                if (fl != fl2) {
                    throw new Exception("Invalid state, " + fl.getFPID().getProducer()
                            + " is not the same instance in ordered and maven producers for " + maven);
                }
                FeaturePackLayout fl3 = layout.getFeaturePacks().get(fl.getFPID().getProducer());
                if (fl3 != null) {
                    if (fl != fl3) {
                        throw new Exception("Invalid state, " + fl.getFPID().getProducer()
                                + " is not the same instance in ordered and feature-packsfor " + fl.getFPID().getProducer());
                    }
                }
            } else {
                // Must be in feature-packs
                FeaturePackLayout fl3 = layout.getFeaturePacks().get(fl.getFPID().getProducer());
                if (fl != fl3) {
                    throw new Exception("Invalid state, " + fl.getFPID().getProducer()
                            + " is not the same instance in ordered and feature-packsfor " + fl.getFPID().getProducer());
                }
            }
            super.assertLayout(layout);
        }
    }
}
