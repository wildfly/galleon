/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.featurepack.layout.test;

import org.jboss.galleon.BaseErrors;
import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.layout.FeaturePackDescription;
import org.jboss.galleon.spec.FeaturePackSpec;
import org.jboss.galleon.spec.PackageSpec;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Alexey Loubyansky
 */
public class InvalidExternalDependencyNameTestCase {

    private static final FPID fp1Gav = LegacyGalleon1Universe.newFPID("g", "a1", "v");
    private static final FPID fp2Gav = LegacyGalleon1Universe.newFPID("g", "a2", "v");

    @Test
    public void testRequiredDependency() throws Exception {

        final FeaturePackDescription.Builder builder = FeaturePackDescription.builder(FeaturePackSpec.builder(fp1Gav)
                        .addFeaturePackDep(FeaturePackConfig.forLocation(fp2Gav.getLocation()))
                        .addDefaultPackage("p1"))
                        .addPackage(PackageSpec.builder("p1")
                                .addPackageDep("fp2dep", "p2")
                                .build());

        try {
            builder.build();
            Assert.fail("Cannot build feature-pack description with inconsistent package dependencies.");
        } catch (ProvisioningDescriptionException e) {
            Assert.assertEquals(BaseErrors.unknownFeaturePackDependencyName(fp1Gav, "p1", "fp2dep"), e.getMessage());
        }
    }

    @Test
    public void testOptionalDependency() throws Exception {

        final FeaturePackDescription.Builder builder = FeaturePackDescription.builder(FeaturePackSpec.builder(fp1Gav)
                .addFeaturePackDep(FeaturePackConfig.forLocation(fp2Gav.getLocation()))
                .addDefaultPackage("p1"))
                .addPackage(PackageSpec.builder("p1")
                        .addPackageDep("fp2dep", "p2", true)
                        .build());

        try {
            builder.build();
            Assert.fail("Cannot build feature-pack description with inconsistent package dependencies.");
        } catch (ProvisioningDescriptionException e) {
            Assert.assertEquals(BaseErrors.unknownFeaturePackDependencyName(fp1Gav, "p1", "fp2dep"), e.getMessage());
        }
    }
}
