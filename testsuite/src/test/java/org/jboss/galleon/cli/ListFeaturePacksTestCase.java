/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli;

import java.util.Arrays;
import org.aesh.command.CommandException;
import static org.jboss.galleon.cli.CliTestUtils.PRODUCER1;
import static org.jboss.galleon.cli.CliTestUtils.UNIVERSE_NAME;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.UniverseSpec;
import org.junit.AfterClass;
import static org.junit.Assert.assertFalse;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author jdenise@redhat.com
 */
public class ListFeaturePacksTestCase {
    private static UniverseSpec universeSpec;
    private static CliWrapper cli;

    private static final String DEFAULT_FREQUENCY = "alpha";
    @BeforeClass
    public static void setup() throws Exception {
        cli = new CliWrapper();
        MvnUniverse universe = MvnUniverse.getInstance(UNIVERSE_NAME, cli.getSession().getMavenRepoManager());
        universeSpec = CliTestUtils.setupUniverse(universe, cli, UNIVERSE_NAME,
                Arrays.asList(PRODUCER1), DEFAULT_FREQUENCY);
    }

    @AfterClass
    public static void tearDown() {
        cli.close();
    }

    @Test
    public void test() throws Exception {
        CliTestUtils.install(cli, universeSpec, PRODUCER1, "1.0.0.Final");

        try {
            cli.execute("list-feature-packs --universe=maven(foo:bar:1.0.0.Final)");
            throw new Exception("Should have failed");
        } catch (CommandException ex) {
            // XXX OK, expected
        }

        cli.execute("list-feature-packs --universe=" + universeSpec);
        assertTrue(cli.getOutput(), cli.getOutput().contains("1/" + DEFAULT_FREQUENCY));
        assertFalse(cli.getOutput(), cli.getOutput().contains("1/beta"));
        assertFalse(cli.getOutput(), cli.getOutput().contains("1/snapshot"));
        assertFalse(cli.getOutput(), cli.getOutput().contains("1/final"));

        cli.execute("list-feature-packs --universe=" + universeSpec + " --all-frequencies");
        assertTrue(cli.getOutput(), cli.getOutput().contains("1/" + DEFAULT_FREQUENCY));
        assertTrue(cli.getOutput(), cli.getOutput().contains("1/beta"));
        assertTrue(cli.getOutput(), cli.getOutput().contains("1/snapshot"));
        assertTrue(cli.getOutput(), cli.getOutput().contains("1/final"));
    }
}
