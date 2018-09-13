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
import static org.jboss.galleon.cli.CliTestUtils.UNIVERSE_NAME;
import org.jboss.galleon.cli.path.PathParser;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
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
public class StateTestCase {

    private static UniverseSpec universeSpec;
    private static CliWrapper cli;
    private static MvnUniverse universe;
    private static FeaturePackLocation loc;

    @BeforeClass
    public static void setup() throws Exception {
        cli = new CliWrapper();
        universe = MvnUniverse.getInstance(UNIVERSE_NAME, cli.getSession().getMavenRepoManager());
        universeSpec = CliTestUtils.setupUniverse(universe, cli, UNIVERSE_NAME, Arrays.asList(PRODUCER1));
        install("1.0.0.Final");
        loc = CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", null, null);
    }

    @AfterClass
    public static void tearDown() {
        cli.close();
    }

    @Test
    public void testEdit() throws Exception {
        Path dir = cli.newDir("installEdit", false);
        cli.execute("install " + loc + " --dir=" + dir.toString());

        cli.execute("filesystem cd " + dir.toFile().getAbsolutePath());

        cli.execute("state edit");
        try {
            doNavigationTest();
            doEditionTest();
        } finally {
            cli.execute("leave-state");
        }
    }

    @Test
    public void testProvision() throws Exception {
        Path dir = cli.newDir("installProvision", false);
        Path provFile = dir.resolve(".galleon").resolve("provisioning.xml");
        cli.execute("install " + loc + " --dir=" + dir.toString());

        cli.execute("filesystem cd " + dir.toFile().getAbsolutePath());

        Path target = cli.newDir("provisioned", false);
        cli.execute("provision " + provFile.toFile().getAbsolutePath()
                + " --dir=" + target.toFile().getAbsolutePath());

        cli.execute("state edit " + target.toFile().getAbsolutePath());
        try {
            doNavigationTest();
            doEditionTest();
        } finally {
            cli.execute("leave-state");
        }

        // Edit the provisioned state.
        Path target2 = cli.newDir("installProvision2", false);
        cli.execute("state edit " + target.toFile().getAbsolutePath());
        try {
            // Re-provision from it
            cli.execute("provision --dir=" + target2.toFile().getAbsolutePath());
        } finally {
            cli.execute("leave-state");
        }
        // Edit and check the re-provisioned state
        cli.execute("state edit " + target2.toFile().getAbsolutePath());
        try {
            doNavigationTest();
            doEditionTest();
        } finally {
            cli.execute("leave-state");
        }
    }

    @Test
    public void testExport() throws Exception {
        Path dir = cli.newDir("installExport", false);
        cli.execute("install " + loc + " --dir=" + dir.toString());

        cli.execute("filesystem cd " + dir.toFile().getAbsolutePath());

        Path provFile = cli.newDir("xml", true).resolve("prov.xml");
        cli.execute("installation export " + provFile.toFile().getAbsolutePath());

        cli.execute("state edit " + provFile.toFile().getAbsolutePath());
        try {
            doNavigationTest();
            doEditionTest();
        } finally {
            cli.execute("leave-state");
        }

        // Edit the export state file.
        Path provFile2 = cli.newDir("xml", true).resolve("prov2.xml");
        cli.execute("state edit " + provFile.toFile().getAbsolutePath());
        try {
            // Re-export from it
            cli.execute("export " + provFile2.toFile().getAbsolutePath());
        } finally {
            cli.execute("leave-state");
        }
        // Edit and check the re-exported state
        cli.execute("state edit " + provFile2.toFile().getAbsolutePath());
        try {
            doNavigationTest();
            doEditionTest();
        } finally {
            cli.execute("leave-state");
        }
    }

    @Test
    public void testNew() throws Exception {
        cli.execute("state new");
        try {
            cli.execute("get-info --type=all");
            Assert.assertTrue(cli.getOutput(), cli.getOutput().isEmpty());

            cli.execute("add-dependency " + loc + " --default-configs-inherit --packages-inherit");
            doNavigationTest();
            doEditionTest();
        } finally {
            cli.execute("leave-state");
        }
    }

    private void doNavigationTest() throws Exception {
        cli.execute("ls /configs/final");
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("model1"));
        cli.execute("cd /configs/final/model1");
        cli.execute("ls");
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("name1"));
        cli.execute("cd name1");
        cli.execute("ls");
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("specA"));

        cli.execute("cd /");

        cli.execute("ls feature-specs");
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains(loc.getProducer().toString()));
        cli.execute("ls /feature-specs/" + loc.getProducer());
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("specA"));

        cli.execute("ls packages");
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains(loc.getProducer().toString()));
        cli.execute("ls /packages/" + loc.getProducer());
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("p1"));

        cli.execute("search --query=p1");
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("/packages/" + loc.getProducer() + "/p1"));

        cli.execute("search --query=specA");
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("/configs/final/model1/name1/specA"));
    }

    private void doEditionTest() throws Exception {
        // In edit mode we do see all in order to be able to compose a state.
        cli.execute("ls dependencies");
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains(loc.getProducer().toString()));

        cli.execute("exclude-package " + loc.getProducer() + PathParser.PATH_SEPARATOR + "p1");
        cli.execute("ls /packages/" + loc.getProducer());
        Assert.assertTrue(cli.getOutput(), cli.getOutput().isEmpty());

        cli.execute("undo");
        cli.execute("ls /packages/" + loc.getProducer());
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("p1"));

        cli.execute("exclude-config model1/name1");
        cli.execute("ls /configs/final");
        Assert.assertTrue(cli.getOutput(), cli.getOutput().isEmpty());

        cli.execute("undo");
        cli.execute("ls /configs/final");
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("model1"));
        cli.execute("cd /configs/final/model1");
        cli.execute("ls");
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("name1"));

        cli.execute("get-info --type=configs");
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("1.0.0.Final"));
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("model1"));
        Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("name1"));
    }

    public static void install(String version) throws ProvisioningException {
        FeaturePackCreator creator = FeaturePackCreator.getInstance().addArtifactResolver(cli.getSession().getMavenRepoManager());
        FeaturePackLocation fp1 = new FeaturePackLocation(universeSpec,
                PRODUCER1, "1", null, version);
        creator.newFeaturePack(fp1.getFPID())
                .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "fp1 p1").
                getFeaturePack().
                addFeatureSpec(FeatureSpec.builder("specA")
                        .addParam(FeatureParameterSpec.createId("p1"))
                        .build()).
                addConfig(ConfigModel.builder().setModel("model1").
                        setName("name1").addFeature(new FeatureConfig("specA").setParam("p1", "1")).build());
        creator.install();
    }
}
