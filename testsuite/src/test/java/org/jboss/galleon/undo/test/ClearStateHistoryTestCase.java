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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jboss.galleon.Constants;
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
import org.jboss.galleon.util.PathsUtils;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author jdenise@redhat.com
 */
public class ClearStateHistoryTestCase extends SingleUniverseTestBase {

    private FeaturePackLocation fp100;
    private FeaturePackLocation fp101;

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
        assertTrue(pm.isUndoAvailable());
        pm.clearStateHistory();
        assertFalse(pm.isUndoAvailable());
        Path installedHistoryDir = PathsUtils.getStateHistoryDir(installHome);
        assertTrue(Files.exists(installedHistoryDir));
        File[] files = installedHistoryDir.toFile().listFiles();
        assertTrue(files.length == 1);
        assertTrue(files[0].getName().equals(Constants.HISTORY_LIST));
    }

    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(fp101)
                        .build()).build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(fp101.getFPID())
                        .addPackage("p1")
                        .build()).build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1/p1.txt", "fp101 p1")
                .build();
    }
}
