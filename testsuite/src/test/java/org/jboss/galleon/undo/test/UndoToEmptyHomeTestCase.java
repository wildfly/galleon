/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.undo.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.util.fs.state.DirState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.SingleUniverseTestBase;

/**
 *
 * @author Alexey Loubyansky
 */
public class UndoToEmptyHomeTestCase extends SingleUniverseTestBase {

    private FeaturePackLocation fp100;
    private FeaturePackLocation fp101;
    private FeaturePackLocation fp102;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        fp100 = newFpl("prod1", "1", "1.0.0.Final");
        creator.newFeaturePack(fp100.getFPID())
            .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "fp100 p1");

        fp101 = newFpl("prod1", "1", "1.0.1.Final");
        creator.newFeaturePack(fp101.getFPID())
            .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "fp101 p1");

        fp102 = newFpl("prod1", "1", "1.0.2.Final");
        creator.newFeaturePack(fp102.getFPID())
            .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "fp102 p1");

    }

    @Override
    protected ProvisioningConfig initialState() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(fp100)
                .build();
    }

    @Override
    protected void testPm(ProvisioningManager pm) throws ProvisioningException {
        pm.install(fp101);
        pm.uninstall(fp101.getFPID());
        pm.install(fp102);

        assertTrue(pm.isUndoAvailable());
        assertEquals(ProvisioningConfig.builder()
                .addFeaturePackDep(fp102)
                .build(),
                pm.getProvisioningConfig());

        pm.undo();
        assertTrue(pm.isUndoAvailable());
        assertEquals(ProvisioningConfig.builder()
                .build(),
                pm.getProvisioningConfig());
        assertTrue(pm.isUndoAvailable());
    }

    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder().build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder().build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .build();
    }
}
