/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.config.feature.group;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeatureGroup;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.runtime.ResolvedSpecId;
import org.jboss.galleon.spec.FeatureId;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.test.PmInstallFeaturePackTestBase;
import org.junit.Assert;

/**
 *
 * @author Alexey Loubyansky
 */
public class IncludeFeatureNotBeloningToFeatureGroupTestCase extends PmInstallFeaturePackTestBase {

    private static final FPID FP_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(FP_GAV)
            .addFeatureSpec(FeatureSpec.builder("specP")
                    .addParam(FeatureParameterSpec.createId("parent"))
                    .build())
            .addFeatureGroup(FeatureGroup.builder("group1")
                    .addFeature(
                            new FeatureConfig("specP")
                            .setParam("parent", "p1"))
                    .build())
            .addConfig(ConfigModel.builder()
                    .setName("main")
                    .addFeatureGroup(FeatureGroup.builder("group1")
                            .includeFeature(FeatureId.create("specP", "parent", "p2"))
                            .build())
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
    protected void pmFailure(Throwable e) {
        Assert.assertEquals(Errors.failedToResolveConfigSpec(null, "main"), e.getLocalizedMessage());
        Assert.assertNotNull(e.getCause());
        Throwable t = e.getCause();
        Assert.assertEquals(Errors.failedToProcess(FP_GAV, "group1"), t.getLocalizedMessage());
        Assert.assertNotNull(t.getCause());
        t = t.getCause();
        Assert.assertEquals(Errors.featureNotInScope(ResolvedFeatureId.create(new ResolvedSpecId(FP_GAV.getProducer(), "specP"), "parent", "p2"), "group1", FP_GAV), t.getLocalizedMessage());
    }
}
