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
package org.jboss.galleon.cache;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import org.jboss.galleon.cache.FeaturePackCacheManager.OverwritePolicy;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.repo.RepositoryArtifactResolver;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.TestConstants;
import org.jboss.galleon.universe.UniverseResolver;
import org.jboss.galleon.universe.UniverseSpec;
import org.jboss.galleon.universe.maven.MavenUniverseFactory;
import org.jboss.galleon.universe.maven.repo.MavenRepoManager;
import org.jboss.galleon.universe.maven.repo.SimplisticMavenRepoManager;
import org.jboss.galleon.util.IoUtils;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jdenise@redhat.com
 */
public class FeaturePackCacheManagerTestCase {

    private static class OverwritePolicyTest implements OverwritePolicy {

        Set<FeaturePackLocation.FPID> seen = new HashSet<>();
        boolean expired;
        @Override
        public boolean hasExpired(Path fpDir, FeaturePackLocation.FPID fpid) {
            return expired;
        }

        @Override
        public void cached(FeaturePackLocation.FPID fpid) {
            seen.add(fpid);
        }

    }

    private static final String UNIVERSE_NAME = "test-universe";
    private static FeaturePackLocation.FPID FPID;
    private static UniverseResolver universeResolver;
    private static Path installDir;
    private static Path repoHome;
    private static Path fpFile;

    @BeforeClass
    public static void startup() throws Exception {
        installDir = IoUtils.createRandomTmpDir();
        repoHome = IoUtils.createRandomTmpDir();
        RepositoryArtifactResolver repo = SimplisticMavenRepoManager.getInstance(repoHome);
        universeResolver = UniverseResolver.builder().addArtifactResolver((MavenRepoManager) repo).build();
        MvnUniverse universe = MvnUniverse.getInstance(UNIVERSE_NAME, (MavenRepoManager) repo);
        universe.createProducer("prod1");
        universe.install();
        FPID = new FeaturePackLocation(new UniverseSpec(MavenUniverseFactory.ID,
                TestConstants.GROUP_ID + ":" + UNIVERSE_NAME), "prod1", "1", null, "1.0.0").getFPID();
        FeaturePackCreator creator = FeaturePackCreator.getInstance().addArtifactResolver((MavenRepoManager) repo);
        creator.newFeaturePack(FPID)
                .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "fp1 p1");
        creator.install();
        creator.install(installDir);
        fpFile = installDir.toFile().listFiles()[0].toPath();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        try {
            IoUtils.recursiveDelete(installDir);
        } finally {
            IoUtils.recursiveDelete(repoHome);
        }
    }

    @Test
    public void testPut() throws Exception {
        try (FeaturePackCacheManager mgr = new FeaturePackCacheManager()) {
            Path fp = mgr.put(universeResolver, FPID.getLocation());
            assertTrue(Files.exists(fp));

            mgr.remove(FPID);
            assertFalse(Files.exists(fp));
        }
    }

    @Test
    public void testOverwrite() throws Exception {
        Path home = IoUtils.createRandomTmpDir();
        Path fp = null;
        OverwritePolicyTest policy = new OverwritePolicyTest();
        try (FeaturePackCacheManager mgr = new FeaturePackCacheManager(home, policy)) {
            assertEquals(home, mgr.getHome());

            fp = mgr.put(universeResolver, FPID.getLocation());
            assertTrue(policy.seen.contains(FPID));

            policy.seen.clear();
            mgr.put(universeResolver, FPID.getLocation());
            assertFalse(policy.seen.contains(FPID));

            policy.expired = true;
            policy.seen.clear();
            mgr.put(universeResolver, FPID.getLocation());
            assertTrue(policy.seen.contains(FPID));
        } finally {
            assertFalse(Files.exists(home));
            if (fp != null) {
                assertFalse(Files.exists(fp));
            }
        }
    }

    @Test
    public void testFile() throws Exception {
        assertTrue(Files.exists(fpFile));
        OverwritePolicyTest policy = new OverwritePolicyTest();
        try (FeaturePackCacheManager mgr = new FeaturePackCacheManager(null, policy)) {
            Path fp = mgr.put(fpFile, FPID);
            assertTrue(Files.exists(fp));
            assertTrue(policy.seen.contains(FPID));
            policy.seen.clear();
            mgr.put(fpFile, FPID);
            assertTrue(policy.seen.contains(FPID));
        }
    }
}
