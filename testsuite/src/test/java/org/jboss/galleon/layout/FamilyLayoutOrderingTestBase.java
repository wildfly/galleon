/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
