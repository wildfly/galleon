/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.featurepack.uninstall.test;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.PmUninstallFeaturePackTestBase;
import org.jboss.galleon.test.util.fs.state.DirState;

/**
 *
 * @author Alexey Loubyansky
 */
public class UninstallFpWithDepsTestCase extends PmUninstallFeaturePackTestBase {

    private static final FPID FP1_100_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");
    private static final FPID FP2_100_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "1", "1.0.0.Final");
    private static final FPID FP3_100_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp3", "1", "1.0.0.Final");
    private static final FPID FP4_100_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp4", "1", "1.0.0.Final");

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
            .newFeaturePack(FP1_100_GAV)
                .addDependency(FP2_100_GAV.getLocation())
                .addDependency(FP4_100_GAV.getLocation())
                .newPackage("p1", true)
                    .writeContent("fp1/p1.txt", "fp1 1.0.0.Final p1")
                    .getFeaturePack()
                .getCreator()
            .newFeaturePack(FP2_100_GAV)
                .addDependency(FP3_100_GAV.getLocation())
                .newPackage("p1", true)
                    .writeContent("fp2/p1.txt", "fp2 1.0.0.Final p1")
                    .getFeaturePack()
                .getCreator()
            .newFeaturePack(FP3_100_GAV)
                .newPackage("p1", true)
                    .writeContent("fp3/p1.txt", "fp3 1.0.0.Final p1")
                    .getFeaturePack()
                .getCreator()
            .newFeaturePack(FP4_100_GAV)
                .newPackage("p1", true)
                    .writeContent("fp4/p1.txt", "fp4 1.0.0.Final p1")
                    .getFeaturePack();
    }

    @Override
    protected ProvisioningConfig initialState() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.forLocation(FP1_100_GAV.getLocation()))
                .build();
    }

    @Override
    protected FPID uninstallGav() throws ProvisioningDescriptionException {
        return FP1_100_GAV;
    }

    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder().build();
    }

    @Override
    protected ProvisionedState provisionedState() {
        return ProvisionedState.builder().build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder().build();
    }
}
