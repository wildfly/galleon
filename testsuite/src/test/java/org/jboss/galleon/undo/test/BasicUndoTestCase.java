/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.undo.test;

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
import org.jboss.galleon.universe.SingleUniverseTestBase;

/**
 *
 * @author Alexey Loubyansky
 */
public class BasicUndoTestCase extends SingleUniverseTestBase {

    private FeaturePackLocation fp1;
    private FeaturePackLocation fp2;
    private FeaturePackLocation fp3;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1");
        universe.createProducer("prod2");
        universe.createProducer("prod3");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        fp1 = newFpl("prod1", "1", "1.0.0.Final");
        creator.newFeaturePack(fp1.getFPID())
            .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "fp1 p1");

        fp2 = newFpl("prod2", "1", "1.0.0.Final");
        creator.newFeaturePack(fp2.getFPID())
            .newPackage("p1", true)
                .writeContent("fp2/p1.txt", "fp2 p1");

        fp3 = newFpl("prod3", "1", "1.0.0.Final");
        creator.newFeaturePack(fp3.getFPID())
            .newPackage("p1", true)
                .writeContent("fp3/p1.txt", "fp3 p1");

    }

    @Override
    protected ProvisioningConfig initialState() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(fp1)
                .build();
    }

    @Override
    protected void testPm(ProvisioningManager pm) throws ProvisioningException {
        pm.install(fp2);
        pm.install(fp3);
        pm.undo();
    }

    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(fp1)
                        .build())
                .addFeaturePackDep(FeaturePackConfig.builder(fp2)
                        .build())
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(fp1.getFPID())
                        .addPackage("p1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(fp2.getFPID())
                        .addPackage("p1")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1/p1.txt", "fp1 p1")
                .addFile("fp2/p1.txt", "fp2 p1")
                .build();
    }
}
