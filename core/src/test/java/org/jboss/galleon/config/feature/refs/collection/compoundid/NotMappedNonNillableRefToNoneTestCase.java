/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.config.feature.refs.collection.compoundid;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureReferenceSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.PmInstallFeaturePackTestBase;
import org.junit.Assert;

/**
 *
 * @author Alexey Loubyansky
 */
public class NotMappedNonNillableRefToNoneTestCase extends PmInstallFeaturePackTestBase {

    private static final FPID FP_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(FP_GAV)
            .addFeatureSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("id"))
                    .addParam(FeatureParameterSpec.createId("a"))
                    .build())
            .addFeatureSpec(FeatureSpec.builder("specB")
                    .addParam(FeatureParameterSpec.createId("id"))
                    .addParam(FeatureParameterSpec.createId("b"))
                    .addParam(FeatureParameterSpec.builder("a").setType("List<String>").build())
                    .addFeatureRef(FeatureReferenceSpec.create("specA"))
                    .build())
            .addConfig(ConfigModel.builder()
                    .setName("main")
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("id", "1")
                            .setParam("b", "b1")
                            .setParam("a", "[]"))
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("id", "1")
                            .setParam("a", "a1"))
                    .build());
    }

    @Override
    protected FeaturePackConfig featurePackConfig() {
        return FeaturePackConfig.forLocation(FP_GAV.getLocation());
    }

    @Override
    protected void pmSuccess() {
        Assert.fail();
    }

    @Override
    protected void pmFailure(Throwable t) {
        Assert.assertEquals(Errors.failedToBuildConfigSpec(null, "main"), t.getLocalizedMessage());
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return null;
    }
}
