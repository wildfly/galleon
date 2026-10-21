/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.config.capability.dynamic.collection;

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
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.PmInstallFeaturePackTestBase;

/**
 *
 * @author Alexey Loubyansky
 */
public class MissingCapProviderTestCase extends PmInstallFeaturePackTestBase {

    private static final FPID FP_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(FP_GAV)
            .addFeatureSpec(FeatureSpec.builder("specA")
                    .providesCapability("cap.$a")
                    .addParam(FeatureParameterSpec.createId("a"))
                    .build())
            .addFeatureSpec(FeatureSpec.builder("specB")
                    .requiresCapability("cap.$col")
                    .addParam(FeatureParameterSpec.createId("b"))
                    .addParam(FeatureParameterSpec.builder("col").setType("[String]").setDefaultValue("[1,2 , 3 ]").build())
                    .build())
            .addConfig(ConfigModel.builder()
                    .setName("main")
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("a", "3"))
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("b", "b1"))
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("a", "1"))
                    .build());
    }

    @Override
    protected FeaturePackConfig featurePackConfig() {
        return FeaturePackConfig.forLocation(FP_GAV.getLocation());
    }

    @Override
    protected String[] pmErrors() {
        return new String[] {
                Errors.failedToBuildConfigSpec(null, "main"),
                "No provider found for capability cap.2 required by {org.jboss.pm.test:fp1@galleon1}specB:b=b1 as cap.$col"
        };
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return null;
    }
}
