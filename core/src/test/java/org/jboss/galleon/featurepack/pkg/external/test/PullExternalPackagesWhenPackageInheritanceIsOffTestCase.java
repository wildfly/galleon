/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.featurepack.pkg.external.test;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.PmProvisionConfigTestBase;
import org.jboss.galleon.test.util.fs.state.DirState;

/**
 *
 * @author Alexey Loubyansky
 */
public class PullExternalPackagesWhenPackageInheritanceIsOffTestCase extends PmProvisionConfigTestBase {

    private static final FPID FP1_GAV = LegacyGalleon1Universe.newFPID("org.pm.test:fp1", "1", "1.0.0.Final");
    private static final FPID FP2_GAV = LegacyGalleon1Universe.newFPID("org.pm.test:fp2", "1", "1.0.0.Final");
    private static final FPID FP3_GAV = LegacyGalleon1Universe.newFPID("org.pm.test:fp3", "1", "1.0.0.Final");

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(FP1_GAV)
            .addDependency("fp2", FeaturePackConfig.builder(FP2_GAV.getLocation())
                    .build())
            .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "p1")
                .getFeaturePack()
            .newPackage("p2")
                .addDependency("fp2", "p2")
                .writeContent("fp1/p2.txt", "p2")
                .getFeaturePack()
            .getCreator()
        .newFeaturePack(FP2_GAV)
            .addDependency("fp3", FeaturePackConfig.builder(FP3_GAV.getLocation())
                    .build())
            .newPackage("p1", true)
                .writeContent("fp2/p1.txt", "p1")
                .getFeaturePack()
            .newPackage("p2")
                .addDependency("fp3", "p2")
                .writeContent("fp2/p2.txt", "p2")
                .getFeaturePack()
            .getCreator()
        .newFeaturePack(FP3_GAV)
             .newPackage("p1", true)
                 .writeContent("fp3/p1.txt", "p1")
                 .getFeaturePack()
             .newPackage("p2")
                 .writeContent("fp3/p2.txt", "p2")
                 .getFeaturePack();
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(FP1_GAV.getLocation())
                        .setInheritPackages(false)
                        .includePackage("p2")
                        .build())
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(FP3_GAV)
                        .addPackage("p2")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(FP2_GAV)
                        .addPackage("p2")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(FP1_GAV)
                        .addPackage("p2")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1/p2.txt", "p2")
                .addFile("fp2/p2.txt", "p2")
                .addFile("fp3/p2.txt", "p2")
                .build();
    }
}
