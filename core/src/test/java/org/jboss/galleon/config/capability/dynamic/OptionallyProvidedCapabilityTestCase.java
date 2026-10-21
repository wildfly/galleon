/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.config.capability.dynamic;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.test.PmInstallFeaturePackTestBase;
import org.junit.Assert;

/**
 *
 * @author Alexey Loubyansky
 */
public class OptionallyProvidedCapabilityTestCase extends PmInstallFeaturePackTestBase {

    private static final FPID FP_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(FP_GAV)
            .addFeatureSpec(FeatureSpec.builder("specA")
                    .providesCapability("cap.$c", true)
                    .addParam(FeatureParameterSpec.createId("a"))
                    .addParam(FeatureParameterSpec.create("c", true))
                    .build())
            .addFeatureSpec(FeatureSpec.builder("specB")
                    .requiresCapability("cap.$c")
                    .addParam(FeatureParameterSpec.createId("b"))
                    .addParam(FeatureParameterSpec.create("c"))
                    .build())
            .addConfig(ConfigModel.builder()
                    .setName("main")
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("b", "b1")
                            .setParam("c", "c1"))
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("a", "a1")
                            .setParam("c", "c1"))
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("b", "b2")
                            .setParam("c", "c2"))
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("a", "a2"))
                    .build());
    }

    @Override
    protected FeaturePackConfig featurePackConfig() {
        return FeaturePackConfig.forLocation(FP_GAV.getLocation());
    }

    @Override
    protected void pmSuccess() {
        Assert.fail("There is no cap.a provider");
    }

    @Override
    protected void pmFailure(Throwable e) {
        Assert.assertEquals(Errors.failedToBuildConfigSpec(null, "main"), e.getMessage());
        e = (ProvisioningException) e.getCause();
        Assert.assertNotNull(e);
        Assert.assertEquals(
                "No provider found for capability cap.c2 required by {org.jboss.pm.test:fp1@galleon1}specB:b=b2 as cap.$c",
                e.getMessage());
    }
}
