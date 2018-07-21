/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.galleon.undo.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
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
public class DecreaseStateHistoryLimitTestCase extends SingleUniverseTestBase {

    private FeaturePackLocation fp100;
    private FeaturePackLocation fp101;
    private FeaturePackLocation fp102;
    private FeaturePackLocation fp103;
    private FeaturePackLocation fp104;

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

        fp103 = newFpl("prod1", "1", "1.0.3.Final");
        creator.newFeaturePack(fp103.getFPID())
            .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "fp103 p1");

        fp104 = newFpl("prod1", "1", "1.0.4.Final");
        creator.newFeaturePack(fp104.getFPID())
            .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "fp104 p1");

        creator.install();
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
        pm.install(fp102);
        pm.install(fp103);
        pm.install(fp104);

        pm.setStateHistoryLimit(2);
        assertTrue(pm.isUndoAvailable());
        pm.undo();
        assertTrue(pm.isUndoAvailable());
        pm.undo();
        assertFalse(pm.isUndoAvailable());
    }

    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(fp102)
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(fp102.getFPID())
                        .addPackage("p1")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1/p1.txt", "fp102 p1")
                .build();
    }
}
