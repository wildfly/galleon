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
import org.jboss.galleon.ProvisioningException;
import static org.jboss.galleon.cli.CliTestUtils.PRODUCER1;
import static org.jboss.galleon.cli.CliTestUtils.PRODUCER2;
import static org.jboss.galleon.cli.CliTestUtils.UNIVERSE_NAME;
import org.jboss.galleon.cli.cmd.Headers;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.universe.FeaturePackLocation;
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
public class PatchTransitiveDepTestCase {
    private static UniverseSpec universeSpec;
    private static CliWrapper cli;
    private static MvnUniverse universe;
    private static FeaturePackLocation fp1;
    private static FeaturePackLocation fp1Patch;
    private static FeaturePackLocation fp2;

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
    public void test() throws Exception {
        install();
        Path p = CliTestUtils.installAndCheck(cli, "install", fp2, fp2);
        cli.execute("get-info --dir=" + p);
        Assert.assertFalse(cli.getOutput(), cli.getOutput().contains(PRODUCER1));
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains(PRODUCER2));

        cli.execute("get-info --dir=" + p + " --type=all");
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains(PRODUCER1));
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains(PRODUCER2));

        cli.execute("install " + fp1Patch + " --dir=" + p);

        cli.execute("get-info --dir=" + p);
        Assert.assertFalse(cli.getOutput(), cli.getOutput().contains("1.0.0.Patch.Final"));

        cli.execute("get-info --dir=" + p + " --type=dependencies");
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("1.0.0.Patch.Final"));

        cli.execute("get-info --dir=" + p + " --type=patches");
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("1.0.0.Patch.Final"));
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains(Headers.PATCH_FOR));
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains(Headers.PATCH));
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("1.0.0.Final"));
    }

    private static void install() throws ProvisioningException {
        FeaturePackCreator creator = FeaturePackCreator.getInstance().addArtifactResolver(cli.getSession().getMavenRepoManager());
        fp1 = new FeaturePackLocation(universeSpec,
                PRODUCER1, "1", null, "1.0.0.Final");
        creator.newFeaturePack(fp1.getFPID())
                .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "fp1 p1");

        fp1Patch = new FeaturePackLocation(universeSpec,
                PRODUCER1, "1", null, "1.0.0.Patch.Final");
        creator.newFeaturePack(fp1Patch.getFPID())
                .setPatchFor(fp1.getFPID())
                .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "fp1 p1 patch");

        fp2 = new FeaturePackLocation(universeSpec,
                PRODUCER2, "1", null, "1.0.0.Final");
        creator.newFeaturePack(fp2.getFPID())
                .addDependency(fp1);

        creator.install();
    }
}
