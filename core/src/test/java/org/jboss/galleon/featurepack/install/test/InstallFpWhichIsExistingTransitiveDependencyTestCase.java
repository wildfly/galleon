/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.featurepack.install.test;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.PmInstallFeaturePackTestBase;
import org.jboss.galleon.test.util.fs.state.DirState;

/**
 *
 * @author Alexey Loubyansky
 */
public class InstallFpWhichIsExistingTransitiveDependencyTestCase extends PmInstallFeaturePackTestBase {

    private static final FPID FP1_100_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");
    private static final FPID FP1_200_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "2.0.0.Final");
    private static final FPID FP2_100_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "1", "1.0.0.Final");

    @Override
    protected void doBefore() throws Exception {
        super.doBefore();
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
            .newFeaturePack(FP1_100_GAV)
                .newPackage("p1", true)
                    .writeContent("fp1/p1.txt", "fp1 1.0.0.Final p1")
                    .getFeaturePack()
                .getCreator()
            .newFeaturePack(FP1_200_GAV)
                .newPackage("p1", true)
                    .writeContent("fp1/p1.txt", "fp1 2.0.0.Final p1")
                    .getFeaturePack()
                .getCreator()
            .newFeaturePack(FP2_100_GAV)
                .addDependency(FP1_100_GAV.getLocation())
                .newPackage("p1", true)
                    .writeContent("fp2/p1.txt", "fp2 1.0.0.Final p1")
                    .getFeaturePack();
    }

    @Override
    protected ProvisioningConfig initialState() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.forLocation(FP2_100_GAV.getLocation()))
                .build();
    }

    @Override
    protected FeaturePackConfig featurePackConfig() throws ProvisioningDescriptionException {
        return FeaturePackConfig.forLocation(FP1_200_GAV.getLocation());
    }

    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.forLocation(FP1_200_GAV.getLocation()))
                .addFeaturePackDep(FeaturePackConfig.forLocation(FP2_100_GAV.getLocation()))
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(FP1_200_GAV)
                        .addPackage("p1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(FP2_100_GAV)
                        .addPackage("p1")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir(){
        return newDirBuilder()
                .addFile("fp1/p1.txt", "fp1 2.0.0.Final p1")
                .addFile("fp2/p1.txt", "fp2 1.0.0.Final p1")
                .build();
    }
}
