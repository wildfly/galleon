/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.featurepack.pkg.external.test;


import org.jboss.galleon.BaseErrors;
import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.test.FeaturePackRepoTestBase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Alexey Loubyansky
 */
public class InvalidExternalDependencyNameTestCase extends FeaturePackRepoTestBase {

    private static final FPID FP1_GAV = LegacyGalleon1Universe.newFPID("org.pm.test:fp1", "1", "1.0.0.Final");

    @Test
    public void setupRepo() throws Exception {
        try {
            initCreator()
                    .newFeaturePack(FP1_GAV)
                            .addDependency("fp2-dep",
                                    FeaturePackConfig.builder(LegacyGalleon1Universe.newFPID("org.pm.test:fp2", "1", "1.0.0.Final").getLocation()).build())
                            .newPackage("p1", true)
                                    .addDependency("fp2-depp", "p2")
                                    .writeContent("fp1/p1.txt", "p1")
                                    .getFeaturePack()
                            .getCreator()
                    .newFeaturePack(LegacyGalleon1Universe.newFPID("org.pm.test:fp2", "1", "1.0.0.Final"))
                            .newPackage("p1", true)
                                    .writeContent("fp2/p1.txt", "p1")
                                    .getFeaturePack()
                            .getCreator()
                    .install();
        } catch (ProvisioningDescriptionException e) {
            Assert.assertEquals(BaseErrors.unknownFeaturePackDependencyName(FP1_GAV, "p1", "fp2-depp"), e.getLocalizedMessage());
        }
    }
}
