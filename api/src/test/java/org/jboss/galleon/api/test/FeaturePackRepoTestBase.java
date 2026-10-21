/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.api.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jboss.galleon.BaseErrors;

import org.jboss.galleon.DefaultMessageWriter;
import org.jboss.galleon.MessageWriter;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.api.GalleonBuilder;
import org.jboss.galleon.api.Provisioning;
import org.jboss.galleon.api.config.GalleonProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.repo.RepositoryArtifactResolver;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.api.test.util.TestUtils;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.UniverseSpec;
import org.jboss.galleon.universe.galleon1.LegacyGalleon1RepositoryManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeaturePackRepoTestBase {

    protected Path workDir;
    protected Path repoHome;
    protected Path installHome;
    private Path tmpDir;
    protected RepositoryArtifactResolver repo;
    protected FeaturePackCreator creator;
    private boolean recordState = true;

    @Before
    public void before() throws Exception {
        workDir = TestUtils.mkRandomTmpDir();
        installHome = TestUtils.mkdirs(workDir, "dist");
        repoHome = TestUtils.mkdirs(workDir, "repo");
        repo = initRepoManager(repoHome);
        doBefore();
    }

    protected void doBefore() throws Exception {
    }

    @After
    public void after() throws Exception {
        doAfter();
        //IoUtils.recursiveDelete(workDir);
    }

    protected void doAfter() throws Exception {
    }

    protected void setRecordState(boolean recordState) {
        this.recordState = recordState;
    }

    protected boolean isRecordState() {
        return recordState;
    }

    protected RepositoryArtifactResolver initRepoManager(Path repoHome) {
        return LegacyGalleon1RepositoryManager.newInstance(repoHome);
    }

    protected FeaturePackCreator initCreator() throws ProvisioningException {
        return FeaturePackCreator.getInstance().addArtifactResolver(repo);
    }

    protected MessageWriter getMessageWriter() {
        return new DefaultMessageWriter();
    }

    protected Provisioning getPm() throws ProvisioningException {
        GalleonBuilder provider = new GalleonBuilder();
        provider.addArtifactResolver(repo);
        return provider.newProvisioningBuilder()
                .setInstallationHome(installHome)
                .setRecordState(recordState)
                .setMessageWriter(getMessageWriter())
                .build();
    }

    protected ProvisioningManager getCorePm() throws ProvisioningException {
        return ProvisioningManager.builder()
                .setInstallationHome(installHome)
                .build();
    }

    protected Path resolve(String relativePath) {
        return installHome.resolve(relativePath);
    }

    protected static void assertProvisioningConfig(Provisioning pm, GalleonProvisioningConfig config)
            throws ProvisioningException {
        Assert.assertEquals(config, pm.getProvisioningConfig());
    }

    protected void assertProvisionedState(ProvisioningManager pm, ProvisionedState config) throws ProvisioningException {
        Assert.assertEquals(config, pm.getProvisionedState());
    }

    protected Path getTmpDir() throws ProvisioningException {
        try {
            return tmpDir == null ? tmpDir = Files.createDirectory(workDir.resolve("tmp")) : tmpDir;
        } catch (IOException e) {
            throw new ProvisioningException(BaseErrors.mkdirs(workDir.resolve("tmp")), e);
        }
    }

    protected FeaturePackLocation newFpl(String producer, String universe, String channel, String frequency, String build) {
        return new FeaturePackLocation(new UniverseSpec(universe, null), producer, channel, frequency, build);
    }
}
