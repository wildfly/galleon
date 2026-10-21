/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.install.local.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.util.IoUtils;
import org.junit.Assert;
import org.jboss.galleon.universe.SingleUniverseTestBase;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class InstallLocalTestBase  extends SingleUniverseTestBase {

    protected boolean installInUniverse() {
        return true;
    }

    protected abstract FPID installLocalFPID();

    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder().addFeaturePackDep(installLocalFPID().getLocation()).build();
    }

    @Override
    protected void testPm(ProvisioningManager pm) throws ProvisioningException {

        final Path tmpPath = getTmpDir().resolve("tmp.zip");
        Path fpRepoPath = null;
        try {
            fpRepoPath = mvnUniverse.resolve(installLocalFPID());
            IoUtils.copy(fpRepoPath, tmpPath);
        } catch (IOException e) {
            Assert.fail("Failed to copy feature pack from the repo");
        }
        IoUtils.recursiveDelete(fpRepoPath);
        assertFalse(Files.exists(fpRepoPath));

        final boolean installInUniverse = installInUniverse();
        pm.install(tmpPath, installInUniverse);
        assertEquals(installInUniverse, Files.exists(fpRepoPath));
    }
}
