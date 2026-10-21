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
public class UndoUpdateWithMatchingUserRemovedFileTestCase extends UserChangesTestBase {

    private FeaturePackLocation prod100;
    private FeaturePackLocation prod101;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        prod100 = newFpl("prod1", "1", "1.0.0.Final");
        creator.newFeaturePack(prod100.getFPID())
            .newPackage("p1", true)
                .writeContent("prod1/p1.txt", "prod1 p1")
                .writeContent("prod1/p2.txt", "prod1 p2");

        prod101 = newFpl("prod1", "1", "1.0.1.Final");
        creator.newFeaturePack(prod101.getFPID())
            .newPackage("p1", true)
                .writeContent("prod1/p1.txt", "prod1 p1");

    }

    @Override
    protected void testPm(ProvisioningManager pm) throws ProvisioningException {
        pm.install(prod100);
        recursiveDelete("prod1/p2.txt");
        pm.install(prod101);
        pm.undo();
    }


    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(prod100).build())
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(prod100.getFPID())
                        .addPackage("p1")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("prod1/p1.txt", "prod1 p1")
                .build();
    }
}
