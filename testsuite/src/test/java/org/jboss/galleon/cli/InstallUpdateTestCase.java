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
import org.aesh.command.CommandException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.cli.cmd.state.StateCheckUpdatesCommand;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.LatestVersionNotAvailableException;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.TestConstants;
import org.jboss.galleon.universe.UniverseSpec;
import org.jboss.galleon.universe.maven.MavenUniverseFactory;
import org.jboss.galleon.universe.maven.repo.MavenRepoManager;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jdenise@redhat.com
 */
public class InstallUpdateTestCase {

    private static final String PRODUCER1 = "producer1";
    private static final String UNIVERSE_NAME = "cli-test-universe";

    private static UniverseSpec universeSpec;
    private static CliWrapper cli;

    @BeforeClass
    public static void setup() throws Exception {
        cli = new CliWrapper();
        setupUniverse();
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
        install(PRODUCER1, "1.0.0.Alpha1-SNAPSHOT");
        FeaturePackLocation finalLoc = buildFPL(PRODUCER1, "1", "final", null);

        checkNoVersionAvailable(buildFPL(PRODUCER1, "1", null, null), finalLoc);
        checkNoVersionAvailable(buildFPL(PRODUCER1, "1", "beta", null), buildFPL(PRODUCER1, "1", "beta", null));
        checkNoVersionAvailable(buildFPL(PRODUCER1, "1", "alpha", null), buildFPL(PRODUCER1, "1", "alpha", null));

        // snapshot implies latest snapshot
        Path install1 = checkversionInstalled("install1", buildFPL(PRODUCER1, "1", "snapshot", null),
                buildFPL(PRODUCER1, "1", "snapshot", "1.0.0.Alpha1-SNAPSHOT"));

        // no update available
        cli.execute("state check-updates --dir=" + install1);
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains(StateCheckUpdatesCommand.UP_TO_DATE));

        // Add an alpha1 release.
        install(PRODUCER1, "1.0.0.Alpha1");

        checkNoVersionAvailable(buildFPL(PRODUCER1, "1", null, null), finalLoc);
        checkNoVersionAvailable(buildFPL(PRODUCER1, "1", "beta", null), buildFPL(PRODUCER1, "1", "beta", null));

        Path install2 = checkversionInstalled("install2", buildFPL(PRODUCER1, "1", "alpha", null),
                buildFPL(PRODUCER1, "1", "alpha", "1.0.0.Alpha1"));

        // no update available
        cli.execute("state check-updates --dir=" + install2);
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains(StateCheckUpdatesCommand.UP_TO_DATE));

        // update available for the first installation
        cli.execute("state check-updates --dir=" + install1);
        Assert.assertFalse(cli.getOutput(), cli.getOutput().contains(StateCheckUpdatesCommand.UP_TO_DATE));
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("1.0.0.Alpha1"));

        // upgrade to Alpha1
        cli.execute("state upgrade --yes --dir=" + install1);
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("1.0.0.Alpha1"));

        ProvisioningConfig config = getConfig(install1);
        FeaturePackConfig cf = config.getFeaturePackDeps().iterator().next();
        Assert.assertEquals(cf.getLocation().toString(), cf.getLocation(), buildFPL(PRODUCER1, "1", "snapshot", "1.0.0.Alpha1"));

    }

    private static void checkNoVersionAvailable(FeaturePackLocation toInstall, FeaturePackLocation expected) throws Exception {
        Path dir = cli.newDir("install" + System.currentTimeMillis(), false);
        FeaturePackLocation loc = null;
        try {
            cli.execute("install " + toInstall + " --dir=" + dir.toString());
            throw new Exception("Install should have failed");
        } catch (CommandException ex) {
            if (ex.getCause() instanceof CommandExecutionException) {
                if (ex.getCause().getCause() != null) {
                    if (ex.getCause().getCause() instanceof LatestVersionNotAvailableException) {
                        LatestVersionNotAvailableException lex = (LatestVersionNotAvailableException) ex.getCause().getCause();
                        loc = lex.getLocation();
                    }
                }
            }
        }
        if (loc == null) {
            throw new Exception("Expected exception not found");
        }
        Assert.assertEquals(loc.toString(), loc, expected);
        Assert.assertFalse(dir.toFile().exists());
    }

    private static Path checkversionInstalled(String dirName, FeaturePackLocation toInstall, FeaturePackLocation expected) throws Exception {
        Path dir = cli.newDir(dirName, false);
        cli.execute("install " + toInstall + " --dir=" + dir.toString());
        Assert.assertTrue(dir.toFile().exists());
        ProvisioningConfig config = getConfig(dir);
        Assert.assertEquals(1, config.getFeaturePackDeps().size());
        FeaturePackConfig cf = config.getFeaturePackDeps().iterator().next();
        Assert.assertEquals(cf.getLocation().toString(), cf.getLocation(), expected);
        return dir;
    }

    private static ProvisioningConfig getConfig(Path dir) throws ProvisioningException {
        return ProvisioningManager.builder().setInstallationHome(dir).build().getProvisioningConfig();
    }

    private static FeaturePackLocation buildFPL(String producer, String channel, String frq, String build) {
        return new FeaturePackLocation(universeSpec, producer, channel, frq, build);
    }

    private static void setupUniverse() throws ProvisioningException {
        MavenRepoManager mgr = cli.getSession().getMavenRepoManager();
        MvnUniverse universe = MvnUniverse.getInstance(UNIVERSE_NAME, mgr);
        universe.createProducer(PRODUCER1);
        universe.install();
        universeSpec = new UniverseSpec(MavenUniverseFactory.ID, TestConstants.GROUP_ID + ":" + UNIVERSE_NAME);
    }

    private static void install(String producer, String version) throws ProvisioningException {
        FeaturePackCreator creator = FeaturePackCreator.getInstance().addArtifactResolver(cli.getSession().getMavenRepoManager());
        FeaturePackLocation fp1 = new FeaturePackLocation(universeSpec,
                producer, "1", null, version);
        creator.newFeaturePack(fp1.getFPID())
                .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "fp1 p1");
        creator.install();
    }
}
