/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli;

import java.nio.file.Path;
import java.util.Arrays;
import org.aesh.command.CommandException;
import static org.jboss.galleon.cli.CliTestUtils.PRODUCER1;
import static org.jboss.galleon.cli.CliTestUtils.UNIVERSE_NAME;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.UniverseSpec;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jdenise@redhat.com
 */
public class CliHistoryTestCase {

    private static UniverseSpec universeSpec;
    private static CliWrapper cli;
    private static MvnUniverse universe;

    @BeforeClass
    public static void setup() throws Exception {
        cli = new CliWrapper();
        universe = MvnUniverse.getInstance(UNIVERSE_NAME, cli.getSession().getMavenRepoManager());
        universeSpec = CliTestUtils.setupUniverse(universe, cli, UNIVERSE_NAME, Arrays.asList(PRODUCER1));
    }

    @AfterClass
    public static void tearDown() {
        cli.close();
    }

    @Test
    public void noDirectoryTest() throws Exception {
        try {
            cli.execute("installation set-history-limit 99");
            throw new Exception("set-history-limit should have failed");
        } catch (CommandException ex) {
            // XXX OK expected.
        }

        try {
            cli.execute("installation get-history-limit");
            throw new Exception("set-history-limit should have failed");
        } catch (CommandException ex) {
            // XXX OK expected.
        }

        try {
            cli.execute("undo");
            throw new Exception("undo should have failed");
        } catch (CommandException ex) {
            // XXX OK expected.
        }

        try {
            cli.execute("installation clear-history");
            throw new Exception("clear-history should have failed");
        } catch (CommandException ex) {
            // XXX OK expected.
        }
    }

    @Test
    public void test() throws Exception {
        CliTestUtils.install(cli, universeSpec, PRODUCER1, "1.0.0.Alpha1");
        Path p = cli.newDir("install", false);
        cli.execute("install " + CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", "alpha", "1.0.0.Alpha1") + " --dir=" + p);
        cli.execute("install " + CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", "alpha", "1.0.0.Alpha1") + " --dir=" + p);
        cli.execute("install " + CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", "alpha", "1.0.0.Alpha1") + " --dir=" + p);

        try {
            cli.execute("installation set-history-limit rt --dir=" + p);
            throw new Exception("set-history-limit should have failed");
        } catch (CommandException ex) {
            // XXX OK expected.
        }

        cli.execute("installation set-history-limit 66 --dir=" + p);
        cli.execute("installation get-history-limit --dir=" + p);
        Assert.assertTrue(cli.getOutput().contains("66"));

        cli.execute("undo --dir=" + p);

        cli.execute("installation clear-history --dir=" + p);

        try {
            cli.execute("undo --dir=" + p);
            throw new Exception("undo should have failed");
        } catch (CommandException ex) {
            // XXX OK expected.
        }

    }
}
