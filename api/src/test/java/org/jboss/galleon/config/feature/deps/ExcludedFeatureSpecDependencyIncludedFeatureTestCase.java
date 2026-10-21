/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.config.feature.deps;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.api.config.GalleonFeaturePackConfig;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeatureGroup;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.spec.FeatureDependencySpec;
import org.jboss.galleon.spec.FeatureId;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.api.test.PmInstallFeaturePackTestBase;
import org.junit.Assert;

/**
 *
 * @author Alexey Loubyansky
 */
public class ExcludedFeatureSpecDependencyIncludedFeatureTestCase extends PmInstallFeaturePackTestBase {

    private static final FPID FP_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(FP_GAV)
            .addFeatureSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("id"))
                    .addParam(FeatureParameterSpec.create("a", true))
                    .build())
            .addFeatureSpec(FeatureSpec.builder("specB")
                    .addParam(FeatureParameterSpec.createId("id"))
                    .addParam(FeatureParameterSpec.create("b", true))
                    .addFeatureDep(FeatureDependencySpec.create(FeatureId.create("specA", "id", "a"), true))
                    .build())
            .addFeatureGroup(FeatureGroup.builder("fg1")
                    .addFeature(new FeatureConfig("specB")
                            .setParam("id", "b"))
                    .build())
            .addConfig(ConfigModel.builder()
                    .setName("main")
                    .addFeatureGroup(FeatureGroup.builder("fg1")
                            .excludeFeature(FeatureId.create("specA", "id", "a"))
                            .build())
                    .build());
    }

    @Override
    protected GalleonFeaturePackConfig featurePackConfig() {
        return GalleonFeaturePackConfig.forLocation(FP_GAV.getLocation());
    }

    @Override
    protected void pmSuccess() {
        Assert.fail("There should be an unsatisfied dependency");
    }

    @Override
    protected void pmFailure(Throwable e) {
        Assert.assertEquals(Errors.failedToBuildConfigSpec(null, "main"), e.getMessage());
        e = (ProvisioningException) e.getCause();
        Assert.assertNotNull(e);
        Assert.assertEquals("{org.jboss.pm.test:fp1@galleon1}specB:id=b has unresolved dependency on {org.jboss.pm.test:fp1@galleon1}specA:id=a", e.getMessage());
    }
}
