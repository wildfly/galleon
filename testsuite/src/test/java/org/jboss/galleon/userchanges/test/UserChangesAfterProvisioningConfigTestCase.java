/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.userchanges.test;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.util.fs.state.DirState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;

/**
 *
 * @author Alexey Loubyansky
 */
public class UserChangesAfterProvisioningConfigTestCase extends UserChangesTestBase {

    private FeaturePackLocation prod100;
    private FeaturePackLocation prod101;
    private FeaturePackLocation prod200;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1");
        universe.createProducer("prod2");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        prod100 = newFpl("prod1", "1", "1.0.0.Final");
        creator.newFeaturePack(prod100.getFPID())
            .newPackage("p1", true)
                .addDependency("p2")
                .addDependency("p3")
                .writeContent("prod1/p1.txt", "prod1 p1")
                .getFeaturePack()
            .newPackage("p2")
                .writeContent("prod1/p2.txt", "prod1 p2")
                .getFeaturePack()
            .newPackage("p3")
                .writeContent("prod1/p3.txt", "prod1 p3");

        prod101 = newFpl("prod1", "1", "1.0.1.Final");
        creator.newFeaturePack(prod101.getFPID())
            .newPackage("p1", true)
                .addDependency("p2")
                .addDependency("p3")
                .writeContent("prod1/p1.txt", "prod101 p1")
                .getFeaturePack()
            .newPackage("p2")
                .writeContent("prod1/p2.txt", "prod101 p2")
                .getFeaturePack()
            .newPackage("p3")
                .writeContent("prod1/p3.txt", "prod101 p3");

        prod200 = newFpl("prod2", "1", "1.0.0.Final");
        creator.newFeaturePack(prod200.getFPID())
            .newPackage("p1", true)
                .writeContent("prod2/p1.txt", "prod2 p1");

    }

    @Override
    protected void testPm(ProvisioningManager pm) throws ProvisioningException {
        pm.install(prod100);
        writeContent("new.txt", "user");
        writeContent("prod1/p2.txt", "user");
        recursiveDelete("prod1/p3.txt");
        mkdirs("prod1/user");

        pm.provision(ProvisioningConfig.builder()
                .addFeaturePackDep(prod101)
                .addFeaturePackDep(prod200)
                .build());
    }


    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(prod101)
                        .build())
                .addFeaturePackDep(FeaturePackConfig.builder(prod200)
                        .build())
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(prod101.getFPID())
                        .addPackage("p1")
                        .addPackage("p2")
                        .addPackage("p3")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(prod200.getFPID())
                        .addPackage("p1")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("prod1/p1.txt", "prod101 p1")
                .addFile("prod1/p2.txt", "user")
                .addFile("prod1/p2.txt.glnew", "prod101 p2")
                .addFile("prod2/p1.txt", "prod2 p1")
                .addFile("new.txt", "user")
                .addDir("prod1/user")
                .build();
    }
}
