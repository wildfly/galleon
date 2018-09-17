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
package org.jboss.galleon.cli;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import static org.jboss.galleon.cli.CliTestUtils.PRODUCER1;
import static org.jboss.galleon.cli.CliTestUtils.PRODUCER2;
import static org.jboss.galleon.cli.CliTestUtils.UNIVERSE_NAME;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.UniverseSpec;
import org.jboss.galleon.util.LayoutUtils;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jdenise@redhat.com
 */
public class CliTestCase {
    private static UniverseSpec universeSpec;
    private static CliWrapper cli;
    private static MvnUniverse universe;

    @BeforeClass
    public static void setup() throws Exception {
        cli = new CliWrapper();
        universe = MvnUniverse.getInstance(UNIVERSE_NAME, cli.getSession().getMavenRepoManager());
        universeSpec = CliTestUtils.setupUniverse(universe, cli, UNIVERSE_NAME, Arrays.asList(PRODUCER1, PRODUCER2));
    }

    @AfterClass
    public static void tearDown() {
        cli.close();
    }

    @Test
    public void testCache() throws Exception {
        CliTestUtils.install(cli, universeSpec, PRODUCER1, "1.0.0.Alpha1");
        CliTestUtils.install(cli, universeSpec, PRODUCER1, "1.0.0.Alpha1-SNAPSHOT");
        Path cache = cli.getSession().getPmConfiguration().getLayoutCache();
        assertFalse(Files.exists(cache));

        cli.execute("feature-pack clear-cache");

        Path p = cli.newDir("install", false);
        FeaturePackLocation fpl = CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", "alpha", "1.0.0.Alpha1");
        cli.execute("install " + fpl + " --dir=" + p);
        assertTrue(Files.exists(cache));
        assertTrue(cache.toFile().list().length == 1);
        assertTrue(Files.exists(LayoutUtils.getFeaturePackDir(cache, fpl.getFPID(), true)));
        String lastUsage = cli.getSession().getPmConfiguration().getLayoutCacheContent().getProperty(fpl.getFPID().toString());
        assertNotNull(lastUsage);
        assertTrue(cli.getSession().getPmConfiguration().getLayoutCacheContent().size() == 1);

        cli.execute("install " + fpl
                + " --dir=" + p);
        assertTrue(Files.exists(cache));
        assertTrue(cache.toFile().list().length == 1);
        assertTrue(Files.exists(LayoutUtils.getFeaturePackDir(cache, fpl.getFPID(), true)));
        assertEquals(lastUsage, cli.getSession().getPmConfiguration().getLayoutCacheContent().getProperty(fpl.getFPID().toString()));

        cli.execute("feature-pack clear-cache");
        assertFalse(Files.exists(cache));
        assertTrue(cli.getSession().getPmConfiguration().getLayoutCacheContent().isEmpty());

        // SNAPSHOT MUST BE OVERWRITTEN each time.
        FeaturePackLocation fplSnapshot = CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", "alpha", "1.0.0.Alpha1-SNAPSHOT");
        Path snapshotPath = cli.newDir("install-snapshot", false);
        cli.execute("install " + fplSnapshot
                + " --dir=" + snapshotPath);
        String time1 = cli.getSession().getPmConfiguration().getLayoutCacheContent().getProperty(fplSnapshot.getFPID().toString());
        assertNotNull(time1);
        cli.execute("install " + fplSnapshot
                + " --dir=" + snapshotPath);
        String time2 = cli.getSession().getPmConfiguration().getLayoutCacheContent().getProperty(fplSnapshot.getFPID().toString());
        assertNotNull(time2);
        assertFalse(time1.equals(time2));
        Path path = LayoutUtils.getFeaturePackDir(cache, fplSnapshot.getFPID());
        assertTrue(Files.exists(path));
        cli.getSession().cleanupLayoutCache();
        assertTrue(Files.exists(path));
    }

    @Test
    public void testCleanupCache() throws Exception {
        Path root = cli.getSession().getPmConfiguration().getLayoutCache();
        Path useless = root.resolve("useless");
        Files.createDirectory(useless);
        assertTrue(Files.exists(useless));
        cli.getSession().cleanupLayoutCache();
        assertFalse(Files.exists(useless));
    }
}
