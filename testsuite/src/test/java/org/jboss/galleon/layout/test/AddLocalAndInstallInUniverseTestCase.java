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

package org.jboss.galleon.layout.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.layout.ProvisioningLayoutFactory;
import org.jboss.galleon.layout.LayoutOrderingTestBase;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.util.IoUtils;
import org.junit.Assert;

/**
 *
 * @author Alexey Loubyansky
 */
public class AddLocalAndInstallInUniverseTestCase extends LayoutOrderingTestBase {

    private MvnUniverse mvnUniverse;
    private FeaturePackLocation fpl1;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        mvnUniverse = universe;
        universe.createProducer("prod1");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningDescriptionException {
        fpl1 = newFpl("prod1", "1", "1.0.0.Final");
        creator.newFeaturePack(fpl1.getFPID());
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(fpl1)
                .build();
    }

    @Override
    protected ProvisioningLayoutFactory getLayoutFactory() throws ProvisioningException {
        final Path tmpPath = getTmpDir().resolve("tmp.zip");
        Path fpRepoPath = null;
        try {
            fpRepoPath = mvnUniverse.resolve(fpl1.getFPID());
            IoUtils.copy(fpRepoPath, tmpPath);
        } catch (IOException e) {
            Assert.fail("Failed to copy feature pack from the repo");
        }
        IoUtils.recursiveDelete(fpRepoPath);
        assertFalse(Files.exists(fpRepoPath));
        final ProvisioningLayoutFactory layoutFactory = super.getLayoutFactory();
        layoutFactory.addLocal(tmpPath, true);
        assertTrue(Files.exists(fpRepoPath));
        IoUtils.recursiveDelete(fpRepoPath);
        assertFalse(Files.exists(fpRepoPath));
        return layoutFactory;
    }

    @Override
    protected FPID[] expectedOrder() {
        return new FPID[] {fpl1.getFPID()};
    }
}
