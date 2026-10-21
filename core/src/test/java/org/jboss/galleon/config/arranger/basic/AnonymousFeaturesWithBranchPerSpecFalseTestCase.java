/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.config.arranger.basic;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.runtime.ResolvedSpecId;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.PmInstallFeaturePackTestBase;
import org.jboss.galleon.xml.ProvisionedConfigBuilder;
import org.jboss.galleon.xml.ProvisionedFeatureBuilder;

/**
 *
 * @author Alexey Loubyansky
 */
public class AnonymousFeaturesWithBranchPerSpecFalseTestCase extends PmInstallFeaturePackTestBase {

    private static final FPID FP1_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(FP1_GAV)

            .addFeatureSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("a"))
                    .build())
            .addFeatureSpec(FeatureSpec.builder("specB")
                    .addParam(FeatureParameterSpec.create("b"))
                    .build())
            .addConfig(ConfigModel.builder()
                    .setName("main")
                    .setProperty(ConfigModel.BRANCH_PER_SPEC, "false")
                    .addFeature(new FeatureConfig("specA").setParam("a", "1"))
                    .addFeature(new FeatureConfig("specB").setParam("b", "1"))

                    .addFeature(new FeatureConfig("specB").setParam("b", "2"))
                    .addFeature(new FeatureConfig("specA").setParam("a", "2"))

                    .addFeature(new FeatureConfig("specA").setParam("a", "3"))
                    .addFeature(new FeatureConfig("specB").setParam("b", "3"))

                    .build());
    }

    @Override
    protected FeaturePackConfig featurePackConfig() {
        return FeaturePackConfig.forLocation(FP1_GAV.getLocation());
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(FP1_GAV)
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setName("main")
                        .setProperty(ConfigModel.BRANCH_PER_SPEC, "false")

                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP1_GAV.getProducer(), "specA", "a", "1")).build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP1_GAV.getProducer(), "specA", "a", "2")).build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP1_GAV.getProducer(), "specA", "a", "3")).build())

                        .addFeature(ProvisionedFeatureBuilder.builder(new ResolvedSpecId(FP1_GAV.getProducer(), "specB")).setConfigParam("b", "1").build())
                        .addFeature(ProvisionedFeatureBuilder.builder(new ResolvedSpecId(FP1_GAV.getProducer(), "specB")).setConfigParam("b", "2").build())
                        .addFeature(ProvisionedFeatureBuilder.builder(new ResolvedSpecId(FP1_GAV.getProducer(), "specB")).setConfigParam("b", "3").build())

                        .build())
                .build();
    }
}
