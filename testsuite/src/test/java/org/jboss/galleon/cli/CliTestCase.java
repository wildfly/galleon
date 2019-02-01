/*
 * Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
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
import org.aesh.command.CommandException;
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
public class CliTestCase {
    private static UniverseSpec universeSpec;
    private static CliWrapper cli;
    private static MvnUniverse universe;

    private static final String UNIVERSE_CUSTOM_NAME = "tutu";

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
    public void testCommandFromChilDir() throws Exception {
        CliTestUtils.install(cli, universeSpec, PRODUCER1, "1.0.0.Final");
        Path p = cli.newDir("installCommandFromChilDir", false);
        FeaturePackLocation fpl = CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", "final", "1.0.0.Final");
        cli.execute("install " + fpl + " --dir=" + p);
        cli.execute("cd " + p.resolve(PRODUCER1));

        cli.execute("installation add-universe --name=" + UNIVERSE_CUSTOM_NAME
                + " --factory=maven --location=" + universeSpec.getLocation());
        cli.execute("list-feature-packs");
        assertTrue(cli.getOutput(), cli.getOutput().contains(UNIVERSE_CUSTOM_NAME + "@1"));

        cli.execute("find " + PRODUCER1);
        assertTrue(cli.getOutput(), cli.getOutput().contains(CliTestUtils.buildFPL(UniverseSpec.
                fromString(UNIVERSE_CUSTOM_NAME), PRODUCER1, "1", null, "1.0.0.Final").toString()));

        cli.execute("installation remove-universe --name=" + UNIVERSE_CUSTOM_NAME);
        cli.execute("list-feature-packs");
        assertFalse(cli.getOutput(), cli.getOutput().contains(UNIVERSE_CUSTOM_NAME + "@1"));

        cli.execute("installation export ./exported.xml");
        assertTrue(Files.exists(p.resolve(PRODUCER1).resolve("exported.xml")));
        cli.execute("installation set-history-limit 777");
        cli.execute("installation get-history-limit");
        assertTrue(cli.getOutput(), cli.getOutput().contains("777"));

        cli.execute("installation clear-history");

        cli.execute("get-info");
        assertTrue(cli.getOutput(), cli.getOutput().contains(PRODUCER1));

        // Will not fail from a child directory.
        cli.execute("check-updates");

        // Now commands that are not allowed to be done from a child command
        // without a target directory
        try {
            cli.execute("install " + fpl);
            throw new Exception("Should have failed");
        } catch (CommandException ex) {
            // XXX OK.
        }

        try {
            cli.execute("undo");
            throw new Exception("Should have failed");
        } catch (CommandException ex) {
            // XXX OK.
        }

        try {
            cli.execute("uninstall " + fpl.getFPID());
            throw new Exception("Should have failed");
        } catch (CommandException ex) {
            // XXX OK.
        }

        try {
            cli.execute("update");
            throw new Exception("Should have failed");
        } catch (CommandException ex) {
            // XXX OK.
        }

        // uninstall from a child directory with a --dir option
        FeaturePackLocation toUnInstall = CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", null, "1.0.0.Final");
        cli.execute("uninstall " + toUnInstall.getFPID() + " --dir=..");
    }
}
