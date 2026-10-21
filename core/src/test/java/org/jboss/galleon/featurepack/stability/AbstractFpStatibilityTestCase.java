/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.featurepack.stability;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.test.PmProvisionConfigTestBase;

/**
 *
 * @author jfdenise
 */
public abstract class AbstractFpStatibilityTestCase extends PmProvisionConfigTestBase {

    static final FPID FP1_100_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");
    static final FPID FP2_100_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "1", "1.0.0.Final");

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
            .newFeaturePack(FP1_100_GAV).setConfigStability("default").setPackageStability("default")
                .newPackage("fp1_1default", true).setStability("default")
                    .getFeaturePack()
                .newPackage("fp1_2noStability", true)
                    .getFeaturePack()
                .newPackage("fp1_3experimental", true).setStability("experimental")
                    .getFeaturePack().getCreator()
            .newFeaturePack(FP2_100_GAV).setConfigStability("experimental").setPackageStability("experimental")
                .addDependency(FP1_100_GAV.getLocation())
                .newPackage("fp2_1default", true).setStability("default")
                    .getFeaturePack()
                .newPackage("fp2_2noStability", true)
                    .getFeaturePack()
                .newPackage("fp2_3community", true).setStability("community")
                    .getFeaturePack()
                .newPackage("fp2_4preview", true).setStability("preview")
                    .getFeaturePack()
                .newPackage("fp2_5experimental", true).setStability("experimental")
                    .getFeaturePack();
    }
}
