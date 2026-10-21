/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.featurepack.layout.test;

import java.util.Collections;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.layout.FeaturePackDescription;
import org.jboss.galleon.spec.FeaturePackSpec;
import org.jboss.galleon.spec.PackageSpec;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Alexey Loubyansky
 */
public class UnsatisfiedLocalPackageDependenciesTestCase {

    private static final FPID fpGav = LegacyGalleon1Universe.newFPID("g", "a", "v");

    @Test
    public void testRequiredDependency() throws Exception {

        final FeaturePackDescription.Builder builder = FeaturePackDescription
                .builder(FeaturePackSpec.builder(fpGav)
                        .addDefaultPackage("p1"))
                        .addPackage(PackageSpec.builder("p1")
                                .addPackageDep("p2", true)
                                .build())
                        .addPackage(PackageSpec.builder("p2")
                                .addPackageDep("p3", true)
                                .build())
                        .addPackage(PackageSpec.builder("p3")
                                .addPackageDep("p4")
                                .build());

        try {
            builder.build();
            Assert.fail("Cannot build feature-pack description with inconsistent package dependencies.");
        } catch (ProvisioningDescriptionException e) {
            Assert.assertEquals(Errors.unsatisfiedPackageDependencies(fpGav, "p3", Collections.singleton("p4")), e.getMessage());
        }
    }

    @Test
    public void testOptionalDependency() throws Exception {

        final FeaturePackDescription.Builder builder = FeaturePackDescription
                .builder(FeaturePackSpec.builder(fpGav)
                        .addDefaultPackage("p1"))
                        .addPackage(PackageSpec.builder("p1")
                                .addPackageDep("p2", true)
                                .build())
                        .addPackage(PackageSpec.builder("p2")
                                .addPackageDep("p3", true)
                                .build())
                        .addPackage(PackageSpec.builder("p3")
                                .addPackageDep("p4", true)
                                .build());

        try {
            builder.build();
            Assert.fail("Cannot build feature-pack description with inconsistent package dependencies.");
        } catch (ProvisioningDescriptionException e) {
            Assert.assertEquals(Errors.unsatisfiedPackageDependencies(fpGav, "p3", Collections.singleton("p4")), e.getMessage());
        }
    }
}
