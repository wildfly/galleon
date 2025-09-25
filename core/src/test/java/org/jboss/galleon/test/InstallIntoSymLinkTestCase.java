/*
 * Copyright 2016-2024 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.test;

import java.nio.file.Files;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedPackage;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.util.fs.state.DirState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.junit.Before;

public class InstallIntoSymLinkTestCase extends PmTestBase {

    private static final FeaturePackLocation.FPID FP1_100_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");

    @Before
    public void before() throws Exception {
        super.before();
        // change the installHome to be a symbolic link
        installHome = Files.createSymbolicLink(workDir.resolve("link"), installHome);
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator.newFeaturePack(FP1_100_GAV)
                .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "fp1 1.0.0.Final p1");

    }

    @Override
    protected void testPm(ProvisioningManager pm) throws ProvisioningException {
        pm.install(FP1_100_GAV.getLocation());
    }

    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(FP1_100_GAV.getLocation())
                        .build())
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningDescriptionException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(FP1_100_GAV)
                        .addPackage(ProvisionedPackage.newInstance("p1"))
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1/p1.txt", "fp1 1.0.0.Final p1")
                .build();
    }
}
