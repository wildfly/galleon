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
import org.jboss.galleon.cli.cmd.state.StateCheckUpdatesCommand;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.UniverseSpec;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author jdenise@redhat.com
 */
@Ignore("FIXME")
public class InstallUpdateTestCase {

    private static UniverseSpec universeSpec;
    private static CliWrapper cli;

    @BeforeClass
    public static void setup() throws Exception {
        cli = new CliWrapper();
        universeSpec = CliTestUtils.setupUniverse(cli, UNIVERSE_NAME, Arrays.asList(PRODUCER1, PRODUCER2));
    }

    @AfterClass
    public static void tearDown() {
        cli.close();
    }

    @Test
    public void test() throws Exception {

        Assert.assertEquals(cli.getMvnRepo().toString(), cli.getMvnRepo(),
                cli.getSession().getPmConfiguration().getMavenConfig().getLocalRepository());

        // Add an alpha1 snapshot release.
        CliTestUtils.install(cli, universeSpec, PRODUCER1, "1.0.0.Alpha1-SNAPSHOT");
        CliTestUtils.install(cli, universeSpec, PRODUCER2, "1.0.0.Alpha1-SNAPSHOT");
        FeaturePackLocation finalLoc = CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", "final", null);

        CliTestUtils.checkNoVersionAvailable(cli, CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", null, null), finalLoc);
        CliTestUtils.checkNoVersionAvailable(cli, CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", "beta", null), CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", "beta", null));
        CliTestUtils.checkNoVersionAvailable(cli, CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", "alpha", null), CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", "alpha", null));

        // snapshot implies latest snapshot
        Path install1 = CliTestUtils.installAndCheck(cli, "install1", CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", "snapshot", null),
                CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", "snapshot", "1.0.0.Alpha1-SNAPSHOT"));

        // no update available
        cli.execute("state check-updates --dir=" + install1);
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains(StateCheckUpdatesCommand.UP_TO_DATE));

        // Add an alpha1 release.
        CliTestUtils.install(cli, universeSpec, PRODUCER1, "1.0.0.Alpha1");

        CliTestUtils.checkNoVersionAvailable(cli, CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", null, null), finalLoc);
        CliTestUtils.checkNoVersionAvailable(cli, CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", "beta", null), CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", "beta", null));

        Path install2 = CliTestUtils.installAndCheck(cli, "install2", CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", "alpha", null),
                CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", "alpha", "1.0.0.Alpha1"));

        // no update available
        cli.execute("state check-updates --dir=" + install2);
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains(StateCheckUpdatesCommand.UP_TO_DATE));

        //Install an alpha-SNAPSHOT for producer2 in same directory
        CliTestUtils.installAndCheck(cli, "install1", CliTestUtils.buildFPL(universeSpec, PRODUCER2, "1", "snapshot", null),
                CliTestUtils.buildFPL(universeSpec, PRODUCER2, "1", "snapshot", "1.0.0.Alpha1-SNAPSHOT"));

        // update available for the first installation, only producer1 has update.
        cli.execute("state check-updates --dir=" + install1);
        Assert.assertFalse(cli.getOutput(), cli.getOutput().contains(StateCheckUpdatesCommand.UP_TO_DATE));
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("1.0.0.Alpha1"));
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains(universeSpec.toString()));
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains(PRODUCER1));
        Assert.assertFalse(cli.getOutput(), cli.getOutput().contains(PRODUCER2));

        cli.execute("state check-updates --dir=" + install1 + " --products=" + CliTestUtils.buildFPL(universeSpec, PRODUCER1, null, null, null));
        Assert.assertFalse(cli.getOutput(), cli.getOutput().contains(StateCheckUpdatesCommand.UP_TO_DATE));
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("1.0.0.Alpha1"));
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains(PRODUCER1));
        Assert.assertFalse(cli.getOutput(), cli.getOutput().contains(PRODUCER2));

        cli.execute("state check-updates --dir=" + install1 + " --products=" + CliTestUtils.buildFPL(universeSpec, PRODUCER2, null, null, null));
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains(StateCheckUpdatesCommand.UP_TO_DATE));

        // upgrade to Alpha1, only producer1 upgraded.
        cli.execute("state update --yes --dir=" + install1);
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("1.0.0.Alpha1"));
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains(PRODUCER1));
        Assert.assertFalse(cli.getOutput(), cli.getOutput().contains(PRODUCER2));

        ProvisioningConfig config = CliTestUtils.getConfig(install1);
        FeaturePackConfig cf1 = config.getFeaturePackDep(CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", null, null).getProducer());
        FeaturePackConfig cf2 = config.getFeaturePackDep(CliTestUtils.buildFPL(universeSpec, PRODUCER2, "1", null, null).getProducer());
        Assert.assertEquals(cf1.getLocation().toString(), cf1.getLocation(), CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", "snapshot", "1.0.0.Alpha1"));
        Assert.assertEquals(cf2.getLocation().toString(), cf2.getLocation(), CliTestUtils.buildFPL(universeSpec, PRODUCER2, "1", "snapshot", "1.0.0.Alpha1-SNAPSHOT"));

        // Add an alpha1 release.
        CliTestUtils.install(cli, universeSpec, PRODUCER2, "1.0.0.Alpha1");

        // Then upgrade producer2
        cli.execute("state update --yes --dir=" + install1 + " --products=" + CliTestUtils.buildFPL(universeSpec, PRODUCER2, null, null, null));
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("1.0.0.Alpha1"));
        Assert.assertFalse(cli.getOutput(), cli.getOutput().contains(PRODUCER1));
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains(PRODUCER2));

        config = CliTestUtils.getConfig(install1);
        cf1 = config.getFeaturePackDep(CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", null, null).getProducer());
        cf2 = config.getFeaturePackDep(CliTestUtils.buildFPL(universeSpec, PRODUCER2, "1", null, null).getProducer());
        Assert.assertEquals(cf1.getLocation().toString(), cf1.getLocation(), CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", "snapshot", "1.0.0.Alpha1"));
        Assert.assertEquals(cf2.getLocation().toString(), cf2.getLocation(), CliTestUtils.buildFPL(universeSpec, PRODUCER2, "1", "snapshot", "1.0.0.Alpha1"));

    }
}
