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

import java.util.Arrays;
import org.aesh.command.CommandException;
import static org.jboss.galleon.cli.CliTestUtils.PRODUCER1;
import static org.jboss.galleon.cli.CliTestUtils.UNIVERSE_NAME;
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
public class FindFeaturePacksTestCase {

    private static final String ALPHA = "1.0.0.Alpha1";
    private static final String ALPHA_2 = "1.0.0.Alpha2";
    private static final String FINAL = "1.0.0.Final";
    private static final String SNAPSHOT = "1.0.1.Final-SNAPSHOT";
    private static final String SNAPSHOT_2 = "1.0.2.Final-SNAPSHOT";

    private static UniverseSpec universeSpec;
    private static CliWrapper cli;
    private static final String XPRODUCER = "xproducer";

    @BeforeClass
    public static void setup() throws Exception {
        cli = new CliWrapper();
        MvnUniverse universe = MvnUniverse.getInstance(UNIVERSE_NAME, cli.getSession().getMavenRepoManager());
        universeSpec = CliTestUtils.setupUniverse(universe, cli, UNIVERSE_NAME,
                Arrays.asList(PRODUCER1, XPRODUCER));
    }

    @AfterClass
    public static void tearDown() {
        cli.close();
    }

    @Test
    public void test() throws Exception {
        CliTestUtils.install(cli, universeSpec, PRODUCER1, FINAL);
        CliTestUtils.install(cli, universeSpec, PRODUCER1, ALPHA);
        CliTestUtils.install(cli, universeSpec, PRODUCER1, SNAPSHOT);

        CliTestUtils.install(cli, universeSpec, XPRODUCER, FINAL);
        CliTestUtils.install(cli, universeSpec, XPRODUCER, ALPHA_2);
        CliTestUtils.install(cli, universeSpec, XPRODUCER, SNAPSHOT_2);

        try {
            cli.execute("find");
            throw new Exception("Should have failed");
        } catch (CommandException ex) {
            // XXX OK, expected
        }

        try {
            cli.execute("find --universe=maven(foo:bar:1.0.0.Final)");
            throw new Exception("Should have failed");
        } catch (CommandException ex) {
            // XXX OK, expected
        }

        cli.execute("find * --universe=" + universeSpec);

        assertTrue(cli.getOutput(), cli.getOutput().contains(CliTestUtils.
                buildFPL(universeSpec, PRODUCER1, "1", null, FINAL).toString()));
        assertTrue(cli.getOutput(), cli.getOutput().contains(CliTestUtils.
                buildFPL(universeSpec, PRODUCER1, "1", null, ALPHA).toString()));
        assertTrue(cli.getOutput(), cli.getOutput().contains(CliTestUtils.
                buildFPL(universeSpec, PRODUCER1, "1", null, SNAPSHOT).toString()));
        assertTrue(cli.getOutput(), cli.getOutput().contains(CliTestUtils.
                buildFPL(universeSpec, XPRODUCER, "1", null, FINAL).toString()));
        assertTrue(cli.getOutput(), cli.getOutput().contains(CliTestUtils.
                buildFPL(universeSpec, XPRODUCER, "1", null, ALPHA_2).toString()));
        assertTrue(cli.getOutput(), cli.getOutput().contains(CliTestUtils.
                buildFPL(universeSpec, XPRODUCER, "1", null, SNAPSHOT_2).toString()));
        assertFalse(cli.getOutput(), cli.getOutput().contains(CliTestUtils.
                buildFPL(universeSpec, PRODUCER1, "1", "alpha", FINAL).toString()));
        assertFalse(cli.getOutput(), cli.getOutput().contains(CliTestUtils.
                buildFPL(universeSpec, PRODUCER1, "1", "beta", FINAL).toString()));
        assertFalse(cli.getOutput(), cli.getOutput().contains(CliTestUtils.
                buildFPL(universeSpec, PRODUCER1, "1", "final", FINAL).toString()));
        assertFalse(cli.getOutput(), cli.getOutput().contains(CliTestUtils.
                buildFPL(universeSpec, PRODUCER1, "1", "snapshot", SNAPSHOT).toString()));

        cli.execute("find " + XPRODUCER + " --universe=" + universeSpec);

        assertTrue(cli.getOutput(), cli.getOutput().contains(CliTestUtils.
                buildFPL(universeSpec, XPRODUCER, "1", null, FINAL).toString()));
        assertTrue(cli.getOutput(), cli.getOutput().contains(CliTestUtils.
                buildFPL(universeSpec, XPRODUCER, "1", null, ALPHA_2).toString()));
        assertTrue(cli.getOutput(), cli.getOutput().contains(CliTestUtils.
                buildFPL(universeSpec, XPRODUCER, "1", null, SNAPSHOT_2).toString()));
        assertFalse(cli.getOutput(), cli.getOutput().contains(CliTestUtils.
                buildFPL(universeSpec, PRODUCER1, "1", null, FINAL).toString()));
        assertFalse(cli.getOutput(), cli.getOutput().contains(CliTestUtils.
                buildFPL(universeSpec, PRODUCER1, "1", null, ALPHA).toString()));
        assertFalse(cli.getOutput(), cli.getOutput().contains(CliTestUtils.
                buildFPL(universeSpec, PRODUCER1, "1", null, SNAPSHOT).toString()));
        assertFalse(cli.getOutput(), cli.getOutput().contains(CliTestUtils.
                buildFPL(universeSpec, XPRODUCER, "1", "alpha", FINAL).toString()));
        assertFalse(cli.getOutput(), cli.getOutput().contains(CliTestUtils.
                buildFPL(universeSpec, XPRODUCER, "1", "beta", FINAL).toString()));
        assertFalse(cli.getOutput(), cli.getOutput().contains(CliTestUtils.
                buildFPL(universeSpec, XPRODUCER, "1", "final", FINAL).toString()));
        assertFalse(cli.getOutput(), cli.getOutput().contains(CliTestUtils.
                buildFPL(universeSpec, XPRODUCER, "1", "snapshot", SNAPSHOT).toString()));

        cli.execute("find " + PRODUCER1 + "*/ --universe=" + universeSpec);

        assertTrue(cli.getOutput(), cli.getOutput().contains(CliTestUtils.
                buildFPL(universeSpec, PRODUCER1, "1", "alpha", FINAL).toString()));
        assertTrue(cli.getOutput(), cli.getOutput().contains(CliTestUtils.
                buildFPL(universeSpec, PRODUCER1, "1", "beta", FINAL).toString()));
        assertTrue(cli.getOutput(), cli.getOutput().contains(CliTestUtils.
                buildFPL(universeSpec, PRODUCER1, "1", "final", FINAL).toString()));
        assertTrue(cli.getOutput(), cli.getOutput().contains(CliTestUtils.
                buildFPL(universeSpec, PRODUCER1, "1", "snapshot", SNAPSHOT).toString()));
        assertFalse(cli.getOutput(), cli.getOutput().contains(CliTestUtils.
                buildFPL(universeSpec, PRODUCER1, "1", null, FINAL).toString()));
        assertFalse(cli.getOutput(), cli.getOutput().contains(CliTestUtils.
                buildFPL(universeSpec, PRODUCER1, "1", null, ALPHA).toString()));
        assertFalse(cli.getOutput(), cli.getOutput().contains(CliTestUtils.
                buildFPL(universeSpec, PRODUCER1, "1", null, SNAPSHOT).toString()));
    }
}
