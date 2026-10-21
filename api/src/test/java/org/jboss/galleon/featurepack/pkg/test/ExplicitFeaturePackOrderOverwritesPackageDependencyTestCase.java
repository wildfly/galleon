/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.featurepack.pkg.test;

import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.api.config.GalleonProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.api.test.PmProvisionConfigTestBase;
import org.jboss.galleon.api.test.util.fs.state.DirState;

/**
 *
 * @author Alexey Loubyansky
 */
public class ExplicitFeaturePackOrderOverwritesPackageDependencyTestCase extends PmProvisionConfigTestBase {

    private static final FPID FP1 = LegacyGalleon1Universe.newFPID("org.pm.test:fp1", "1", "1.0.0.Final");
    private static final FPID FP2 = LegacyGalleon1Universe.newFPID("org.pm.test:fp2", "1", "1.0.0.Final");

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(FP1)
            .addDependency(FP2.getLocation())
            .newPackage("a", true)
                .writeContent("file.txt", "fp1")
                .getFeaturePack()
            .newPackage("b", true)
                .writeContent("fp1.txt", "fp1")
                .getFeaturePack()
            .getCreator()
        .newFeaturePack(FP2)
            .newPackage("a", true)
                .writeContent("file.txt", "fp2")
                .getFeaturePack()
            .newPackage("b", true)
                .writeContent("fp2.txt", "fp2")
                .getFeaturePack();
    }

    @Override
    protected GalleonProvisioningConfig provisioningConfig() throws ProvisioningDescriptionException {
        return GalleonProvisioningConfig.builder()
                .addFeaturePackDep(FP1.getLocation())
                .addFeaturePackDep(FP2.getLocation())
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(FP1)
                        .addPackage("a")
                        .addPackage("b")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(FP2)
                        .addPackage("a")
                        .addPackage("b")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("file.txt", "fp2")
                .addFile("fp2.txt", "fp2")
                .addFile("fp1.txt", "fp1")
                .build();
    }
}
