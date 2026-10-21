/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.featurepack.layout.test;



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
public class UnknownDefaultPackageTestCase  {

    @Test
    public void testMain() throws Exception {
        final FPID fp1Gav = LegacyGalleon1Universe.newFPID("org.pm.test:fp-install", "1", "1.0.0.Beta1");
        try {
            FeaturePackDescription
                    .builder(FeaturePackSpec.builder(fp1Gav)
                            .addDefaultPackage("default"))
                    .addPackage(PackageSpec.forName("a"))
                    .build();
            Assert.fail("Non-existing default package");
        } catch(ProvisioningDescriptionException e) {
            Assert.assertEquals(Errors.unknownPackage(fp1Gav, "default"), e.getMessage());
        }
    }
}
