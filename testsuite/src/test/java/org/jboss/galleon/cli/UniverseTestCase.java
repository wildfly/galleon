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

import java.nio.file.Path;
import java.util.Arrays;
import static org.jboss.galleon.cli.CliTestUtils.PRODUCER1;
import static org.jboss.galleon.cli.CliTestUtils.PRODUCER2;
import static org.jboss.galleon.cli.CliTestUtils.UNIVERSE_NAME;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.UniverseSpec;
import org.junit.AfterClass;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jdenise@redhat.com
 */
public class UniverseTestCase {

    private static final String UNIVERSE_NAME2 = "a-custom-universe";
    private static final String UNIVERSE_CUSTOM_NAME = "toto";

    private static UniverseSpec universeSpec1;
    private static UniverseSpec universeSpec2;
    private static CliWrapper cli;

    @BeforeClass
    public static void setup() throws Exception {
        cli = new CliWrapper();
        MvnUniverse universe1 = MvnUniverse.getInstance(UNIVERSE_NAME, cli.getSession().getMavenRepoManager());
        universeSpec1 = CliTestUtils.setupUniverse(universe1, cli, UNIVERSE_NAME,
                Arrays.asList(PRODUCER1));
        MvnUniverse universe2 = MvnUniverse.getInstance(UNIVERSE_NAME2, cli.getSession().getMavenRepoManager());
        universeSpec2 = CliTestUtils.setupUniverse(universe2, cli, UNIVERSE_NAME2,
                Arrays.asList(PRODUCER2));
    }

    @AfterClass
    public static void tearDown() {
        cli.close();
    }

    @Test
    public void test() throws Exception {
        CliTestUtils.install(cli, universeSpec1, PRODUCER1, "1.0.0.Final");
        CliTestUtils.install(cli, universeSpec2, PRODUCER2, "1.0.0.Final");
        Path dir = cli.newDir("install1", false);
        FeaturePackLocation toInstall = CliTestUtils.buildFPL(universeSpec1,
                PRODUCER1, "1", null, null);
        cli.execute("install " + toInstall + " --dir=" + dir);
        cli.execute("get-info --dir=" + dir);
        assertTrue(cli.getOutput(), cli.getOutput().contains(universeSpec1 + "@1"));

        cli.execute("installation add-universe --name=" + UNIVERSE_CUSTOM_NAME
                + " --factory=maven --location=" + universeSpec2.getLocation() + " --dir=" + dir);

        FeaturePackLocation toInstall2 = CliTestUtils.buildFPL(UniverseSpec.
                fromString(UNIVERSE_CUSTOM_NAME), PRODUCER2, "1", null, null);

        cli.execute("install " + toInstall2 + " --dir=" + dir);

        cli.execute("get-info --dir=" + dir);
        assertTrue(cli.getOutput(), cli.getOutput().contains(UNIVERSE_CUSTOM_NAME + "@1"));

        FeaturePackLocation toUnInstall = CliTestUtils.buildFPL(UniverseSpec.
                fromString(UNIVERSE_CUSTOM_NAME), PRODUCER2, "1", null, "1.0.0.Final");

        cli.execute("uninstall " + toUnInstall + " --dir=" + dir);

        cli.execute("undo --dir=" + dir);

        cli.execute("filesystem cd " + dir);
        cli.execute("list-feature-packs");
        assertTrue(cli.getOutput(), cli.getOutput().contains(UNIVERSE_CUSTOM_NAME + "@1"));

        cli.execute("find " + PRODUCER2 + "@");
        assertTrue(cli.getOutput(), cli.getOutput().contains(CliTestUtils.buildFPL(UniverseSpec.
                fromString(UNIVERSE_CUSTOM_NAME), PRODUCER2, "1", null, "1.0.0.Final").toString()));

        cli.execute("find " + PRODUCER2);
        assertTrue(cli.getOutput(), cli.getOutput().contains(CliTestUtils.buildFPL(UniverseSpec.
                fromString(UNIVERSE_CUSTOM_NAME), PRODUCER2, "1", null, "1.0.0.Final").toString()));

        cli.execute("installation remove-universe --name=" + UNIVERSE_CUSTOM_NAME);
        cli.execute("list-feature-packs");
        assertFalse(cli.getOutput(), cli.getOutput().contains(UNIVERSE_CUSTOM_NAME + "@1"));
        assertFalse(cli.getOutput(), cli.getOutput().contains(CliTestUtils.buildFPL(UniverseSpec.
                fromString(UNIVERSE_CUSTOM_NAME), PRODUCER2, "1", null, "1.0.0.Final").toString()));

        cli.execute("find " + PRODUCER2);
        assertFalse(cli.getOutput(), cli.getOutput().contains(CliTestUtils.buildFPL(UniverseSpec.
                fromString(UNIVERSE_CUSTOM_NAME), PRODUCER2, "1", null, "1.0.0.Final").toString()));

        // Only spec is now usable to reference universe.
        FeaturePackLocation toUnInstall2 = CliTestUtils.buildFPL(universeSpec2, PRODUCER2, "1", null, "1.0.0.Final");
        cli.execute("uninstall " + toUnInstall2 + " --dir=" + dir);
    }

}
