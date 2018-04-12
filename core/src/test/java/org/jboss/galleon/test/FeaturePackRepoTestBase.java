/*
 * Copyright ${license.git.copyrightYears} Red Hat, Inc. and/or its affiliates
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
import org.jboss.galleon.repomanager.FeaturePackRepositoryManager;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.util.TestUtils;
import org.jboss.galleon.util.IoUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeaturePackRepoTestBase {

    protected static Path repoHome;

    @BeforeClass
    public static void beforeClass() throws Exception {
        repoHome = TestUtils.mkRandomTmpDir();
        doBeforeClass();
    }

    protected static void doBeforeClass() throws Exception {
    }

    @AfterClass
    public static void afterClass() throws Exception {
        doAfterClass();
        IoUtils.recursiveDelete(repoHome);
    }

    protected static void doAfterClass() throws Exception {
    }

    protected static FeaturePackRepositoryManager getRepoManager() {
        return FeaturePackRepositoryManager.newInstance(repoHome);
    }

    protected Path installHome;

    @Before
    public void before() throws Exception {
        installHome = TestUtils.mkRandomTmpDir();
        doBefore();
    }

    protected void doBefore() throws Exception {
    }

    @After
    public void after() throws Exception {
        doAfter();
        IoUtils.recursiveDelete(installHome);
    }

    protected void doAfter() throws Exception {
    }

    protected ProvisioningManager getPm() {
        return ProvisioningManager.builder()
                .setArtifactResolver(getRepoManager())
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
