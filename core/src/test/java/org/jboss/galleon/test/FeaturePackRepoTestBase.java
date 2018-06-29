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
package org.jboss.galleon.test;

import java.nio.file.Path;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.repo.RepositoryArtifactResolver;
import org.jboss.galleon.repomanager.FeaturePackRepositoryManager;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.util.TestUtils;
import org.jboss.galleon.util.IoUtils;
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
    protected RepositoryArtifactResolver repo;
    protected FeaturePackCreator creator;

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
        IoUtils.recursiveDelete(workDir);
    }

    protected void doAfter() throws Exception {
    }

    protected RepositoryArtifactResolver initRepoManager(Path repoHome) {
        return FeaturePackRepositoryManager.newInstance(repoHome);
    }

    protected FeaturePackCreator initCreator() throws ProvisioningException {
        return FeaturePackCreator.getInstance().addArtifactResolver(repo);
    }

    protected ProvisioningManager getPm() throws ProvisioningException {
        return ProvisioningManager.builder()
                .addArtifactResolver(repo)
                .setInstallationHome(installHome)
                .build();
    }

    protected Path resolve(String relativePath) {
        return installHome.resolve(relativePath);
    }

    protected static void assertProvisioningConfig(ProvisioningManager pm, ProvisioningConfig config)
            throws ProvisioningException {
        Assert.assertEquals(config, pm.getProvisioningConfig());
    }

    protected void assertProvisionedState(ProvisioningManager pm, ProvisionedState config) throws ProvisioningException {
        Assert.assertEquals(config, pm.getProvisionedState());
    }
}
