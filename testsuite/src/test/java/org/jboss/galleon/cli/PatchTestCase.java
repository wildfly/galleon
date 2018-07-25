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
import org.aesh.command.CommandException;
import static org.jboss.galleon.cli.CliTestUtils.PRODUCER1;
import static org.jboss.galleon.cli.CliTestUtils.UNIVERSE_NAME;
import org.jboss.galleon.cli.cmd.Headers;
import org.jboss.galleon.cli.cmd.featurepack.InfoCommand;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
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
public class PatchTestCase {
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
    public void test() throws Exception {
        CliTestUtils.install(cli, universeSpec, PRODUCER1, "1.0.0.Alpha1");
        Path install1 = CliTestUtils.installAndCheck(cli, "install1", CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", "snapshot", null),
                CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", "snapshot", "1.0.0.Alpha1"));

        // No patches information
        Assert.assertFalse(cli.getOutput(), cli.getOutput().contains(Headers.PATCHES));

        Path patchDir = cli.newDir("patches", true);
        FPID patchID = CliTestUtils.installPatch(cli, universeSpec, PRODUCER1, "1.0.0", "Alpha1", patchDir);
        Path patchFile = patchDir.toFile().listFiles()[0].toPath();

        // try to install from the FPID, should fail.
        try {
            cli.execute("install " + patchID + " --dir=" + install1);
            throw new Exception("Install should have failed");
        } catch (CommandException ex) {
            // XXX OK.
        }

        //import the patch into universe
        cli.execute("feature-pack import " + patchFile + " --install-in-universe=true");
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains(patchID.toString()));

        // Now we can use it.
        cli.execute("install " + patchID + " --dir=" + install1);
        ProvisioningConfig config = CliTestUtils.getConfig(install1);
        FeaturePackConfig cf1 = config.getFeaturePackDep(CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", null, null).getProducer());
        Assert.assertTrue(cf1.hasPatches());
        Assert.assertTrue(cf1.getPatches().contains(patchID));

        //Get info from the patch
        cli.execute("feature-pack info --file=" + patchFile);
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains(InfoCommand.PATCH_FOR
                + CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", null, "1.0.0.Alpha1")));

        //Check that output contains the patch.
        cli.execute("state info --dir=" + install1);
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains(Headers.PATCHES));
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains(patchID.getBuild()));

        // uninstall the patch
        cli.execute("uninstall --dir=" + install1 + " " + patchID);

        config = CliTestUtils.getConfig(install1);
        cf1 = config.getFeaturePackDep(CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", null, null).getProducer());
        Assert.assertFalse(cf1.hasPatches());

        //install the patch using the file
        cli.execute("install --dir=" + install1 + " --file=" + patchFile);
        config = CliTestUtils.getConfig(install1);
        cf1 = config.getFeaturePackDep(CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", null, null).getProducer());
        Assert.assertTrue(cf1.hasPatches());
        Assert.assertTrue(cf1.getPatches().contains(patchID));
    }
}
