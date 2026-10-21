/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.layout.test;

import static org.junit.Assert.assertFalse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.creator.FeaturePackCreator;
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
public class NewConfigLayoutForZIPTestCase extends LayoutOrderingTestBase {

    private MvnUniverse mvnUniverse;
    private FeaturePackLocation fpl1;
    private FeaturePackLocation fpl2;
    private Path tmpPath;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        mvnUniverse = universe;
        universe.createProducer("prod1");
        universe.createProducer("prod2");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningDescriptionException {
        fpl2 = newFpl("prod2", "1", "1.0.0.Final");
        creator.newFeaturePack(fpl2.getFPID());

        fpl1 = newFpl("prod1", "1", "1.0.0.Final");
        creator.newFeaturePack(fpl1.getFPID()).addDependency(fpl2);
    }

    @Override
    protected Path featurePackZip() throws ProvisioningException {
        tmpPath = getTmpDir().resolve("tmp.zip");
        Path fpRepoPath = null;
        try {
            fpRepoPath = mvnUniverse.resolve(fpl1.getFPID());
            IoUtils.copy(fpRepoPath, tmpPath);
        } catch (IOException e) {
            Assert.fail("Failed to copy feature pack from the repo");
        }
        IoUtils.recursiveDelete(fpRepoPath);
        assertFalse(Files.exists(fpRepoPath));
        return tmpPath;
    }

    @Override
    protected FPID[] expectedOrder() {
        return new FPID[] {fpl2.getFPID(), fpl1.getFPID()};
    }
}
