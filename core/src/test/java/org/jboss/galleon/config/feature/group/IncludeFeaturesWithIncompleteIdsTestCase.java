/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.config.feature.group;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeatureGroup;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.spec.FeatureId;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureReferenceSpec;
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
public class IncludeFeaturesWithIncompleteIdsTestCase extends PmInstallFeaturePackTestBase {

    private static final FPID FP_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(FP_GAV)
            .addFeatureSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("id"))
                    .build())
            .addFeatureSpec(FeatureSpec.builder("specB")
                    .addFeatureRef(FeatureReferenceSpec.builder("specA").mapParam("a", "id").build())
                    .addParam(FeatureParameterSpec.createId("id"))
                    .addParam(FeatureParameterSpec.createId("a"))
                    .build())
            .addFeatureSpec(FeatureSpec.builder("specC")
                    .addFeatureRef(FeatureReferenceSpec.builder("specA").mapParam("a", "id").build())
                    .addParam(FeatureParameterSpec.createId("id"))
                    .addParam(FeatureParameterSpec.createId("a"))
                    .build())
            .addFeatureGroup(FeatureGroup.builder("fg1")
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("id", "b1"))
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("id", "b2"))
                    .addFeature(
                            new FeatureConfig("specC")
                            .setParam("id", "c1"))
                    .addFeature(
                            new FeatureConfig("specC")
                            .setParam("id", "c2"))
                    .build())
            .addConfig(ConfigModel.builder()
                    .setName("main")
                    .addFeature(FeatureConfig.newConfig("specA")
                            .setParam("id", "a1")
                            .addFeatureGroup(FeatureGroup.builder("fg1")
                                    .setInheritFeatures(false)
                                    .includeFeature(FeatureId.create("specB", "id", "b1"))
                                    .includeFeature(FeatureId.create("specC", "id", "c1"))
                                    .build()))
                    .build());
    }

    @Override
    protected FeaturePackConfig featurePackConfig() {
        return FeaturePackConfig.forLocation(FP_GAV.getLocation());
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.forFPID(FP_GAV))
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setName("main")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specA", "id", "a1"))
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.builder(FP_GAV.getProducer(), "specB").setParam("id", "b1").setParam("a", "a1").build())
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.builder(FP_GAV.getProducer(), "specC").setParam("id", "c1").setParam("a", "a1").build())
                                .build())
                        .build())
                .build();
    }
}
