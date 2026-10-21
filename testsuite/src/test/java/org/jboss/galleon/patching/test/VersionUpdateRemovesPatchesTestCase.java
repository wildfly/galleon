/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.patching.test;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.util.fs.state.DirState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.ProvisionFromUniverseTestBase;

/**
 *
 * @author Alexey Loubyansky
 */
public class VersionUpdateRemovesPatchesTestCase extends ProvisionFromUniverseTestBase {

    private FeaturePackLocation fp1;
    private FeaturePackLocation fp1Patch1;
    private FeaturePackLocation fp1Patch2;
    private FeaturePackLocation fp1_101;
    private FeaturePackLocation fp2;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1");
        universe.createProducer("prod2");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        fp1 = newFpl("prod1", "1", "1.0.0.Final");
        creator.newFeaturePack(fp1.getFPID())
            .newPackage("p1", true)
                .addDependency("p2")
                .addDependency("p3", true)
                .writeContent("fp1/p1.txt", "fp1 p1")
                .getFeaturePack()
            .newPackage("p2")
                .writeContent("fp1/p2.txt", "fp1 p2")
                .getFeaturePack()
            .newPackage("p3")
                .writeContent("fp1/p3.txt", "fp1 p3")
                .getFeaturePack()
            .newPackage("p4")
                .writeContent("fp1/p4.txt", "fp1 p4")
                .getFeaturePack()
            .newPackage("p5")
                .writeContent("fp1/p5.txt", "fp1 p5");

        fp1Patch1 = newFpl("prod1", "1", "1.0.0.Patch1.Final");
        creator.newFeaturePack(fp1Patch1.getFPID())
            .setPatchFor(fp1.getFPID())
            .newPackage("p1", true)
                .addDependency("p2")
                .addDependency("p3")
                .addDependency("p4", true)
                .getFeaturePack()
            .newPackage("p2")
                .writeContent("fp1/p2.txt", "fp1 p2 patch1")
                .getFeaturePack()
            .newPackage("p3")
                .writeContent("fp1/p3.txt", "fp1 p3 patch1");

        fp1Patch2 = newFpl("prod1", "1", "1.0.0.Patch2.Final");
        creator.newFeaturePack(fp1Patch2.getFPID())
            .setPatchFor(fp1.getFPID())
            .newPackage("p3")
                .writeContent("fp1/p3.txt", "fp1 p3 patch2");

        fp1_101 = newFpl("prod1", "1", "1.0.1.Final");
        creator.newFeaturePack(fp1_101.getFPID())
            .newPackage("p1", true)
                .addDependency("p2")
                .addDependency("p3", true)
                .addDependency("p5", true)
                .writeContent("fp1/p1.txt", "fp1 p1 101")
                .getFeaturePack()
            .newPackage("p2")
                .writeContent("fp1/p2.txt", "fp1 p2 101")
                .getFeaturePack()
            .newPackage("p3")
                .writeContent("fp1/p3.txt", "fp1 p3 101")
                .getFeaturePack()
            .newPackage("p4")
                .writeContent("fp1/p4.txt", "fp1 p4 101")
                .getFeaturePack()
            .newPackage("p5")
                .writeContent("fp1/p5.txt", "fp1 p5 101");

        fp2 = newFpl("prod2", "1", "1.0.0.Final");
        creator.newFeaturePack(fp2.getFPID())
            .addDependency(FeaturePackConfig.builder(fp1)
                    .addPatch(fp1Patch1.getFPID())
                    .addPatch(fp1Patch2.getFPID())
                    .build());
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        final ProvisioningConfig config = ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(fp2)
                        .build())
                .addTransitiveDep(fp1_101)
                .build();
        return config;
    }


    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(fp1_101.getFPID())
                        .addPackage("p2")
                        .addPackage("p3")
                        .addPackage("p5")
                        .addPackage("p1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(fp2.getFPID())
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1/p1.txt", "fp1 p1 101")
                .addFile("fp1/p2.txt", "fp1 p2 101")
                .addFile("fp1/p3.txt", "fp1 p3 101")
                .addFile("fp1/p5.txt", "fp1 p5 101")
                .build();
    }
}
