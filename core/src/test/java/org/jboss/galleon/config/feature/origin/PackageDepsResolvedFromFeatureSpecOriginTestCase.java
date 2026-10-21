/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.config.feature.origin;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.PmInstallFeaturePackTestBase;
import org.jboss.galleon.test.util.fs.state.DirState;
import org.jboss.galleon.xml.ProvisionedConfigBuilder;
import org.jboss.galleon.xml.ProvisionedFeatureBuilder;

/**
 *
 * @author Alexey Loubyansky
 */
public class PackageDepsResolvedFromFeatureSpecOriginTestCase extends PmInstallFeaturePackTestBase {

    private static final FPID FP1_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");
    private static final FPID FP2_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "1", "1.0.0.Final");
    private static final FPID FP3_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp3", "1", "1.0.0.Final");

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(FP1_GAV)
            .addFeatureSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("id"))
                    .addParam(FeatureParameterSpec.create("p1", "fp1"))
                    .addPackageDep("p1")
                    .build())
            .newPackage("p1")
                    .writeContent("fp1/p1.txt", "fp1 p1")
                    .getFeaturePack()
            .getCreator()
        .newFeaturePack(FP2_GAV)
            .addDependency(FP1_GAV.getLocation())
            .getCreator()
        .newFeaturePack(FP3_GAV)
            .addDependency(FP2_GAV.getLocation())
            .addConfig(ConfigModel.builder()
                    .setName("main")
                    .addFeature(new FeatureConfig("specA")
                            .setParam("id", "1"))
                    .build());
    }

    @Override
    protected FeaturePackConfig featurePackConfig() {
        return FeaturePackConfig.forLocation(FP3_GAV.getLocation());
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(FP1_GAV)
                        .addPackage("p1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.forFPID(FP2_GAV))
                .addFeaturePack(ProvisionedFeaturePack.forFPID(FP3_GAV))
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setName("main")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP1_GAV.getProducer(), "specA", "id", "1"))
                                .setConfigParam("p1", "fp1")
                                .build())
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1/p1.txt", "fp1 p1")
                .build();
    }
}
