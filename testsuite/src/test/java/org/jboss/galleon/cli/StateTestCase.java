/*
 * Copyright 2016-2020 Red Hat, Inc. and/or its affiliates
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
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import static org.jboss.galleon.cli.CliTestUtils.PRODUCER1;
import static org.jboss.galleon.cli.CliTestUtils.PRODUCER2;
import static org.jboss.galleon.cli.CliTestUtils.PRODUCER3;
import static org.jboss.galleon.cli.CliTestUtils.PRODUCER4;
import static org.jboss.galleon.cli.CliTestUtils.UNIVERSE_NAME;
import org.jboss.galleon.cli.path.PathParser;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.spec.ConfigLayerSpec;
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
    private static FeaturePackLocation locLayers;
    private static FeaturePackLocation locWithTransitive;
    private static FeaturePackLocation transitive;

    @BeforeClass
    public static void setup() throws Exception {
        cli = new CliWrapper();
        universe = MvnUniverse.getInstance(UNIVERSE_NAME, cli.getSession().getMavenRepoManager());
        universeSpec = CliTestUtils.setupUniverse(universe, cli, UNIVERSE_NAME, Arrays.asList(PRODUCER1, PRODUCER2, PRODUCER3, PRODUCER4));
        install("1.0.0.Final");
        installLayers("1.0.0.Final");
        installWithDependency("1.0.0.Final");
        loc = CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", null, null);
        locLayers = CliTestUtils.buildFPL(universeSpec, PRODUCER2, "1", null, null);
        locWithTransitive = CliTestUtils.buildFPL(universeSpec, PRODUCER4, "1", null, null);
        transitive = CliTestUtils.buildFPL(universeSpec, PRODUCER3, "1", null, "1.0.0.Final");
    }

    @AfterClass
    public static void tearDown() {
        cli.close();
    }

    @Test
    public void testEdit() throws Exception {
        Path dir = cli.newDir("installEdit", false);
        cli.execute("install " + loc + " --dir=" + dir.toString());

        cli.execute("cd " + dir.toFile().getAbsolutePath());

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

        cli.execute("cd " + dir.toFile().getAbsolutePath());

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

        cli.execute("cd " + dir.toFile().getAbsolutePath());

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

    @Test
    public void testTransitivePackages() throws Exception {
        cli.execute("state new");
        try {
            cli.execute("add-dependency " + locWithTransitive + " --default-configs-inherit");
            cli.execute("get-info --type=optional-packages");
            Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("p1"));
            Assert.assertTrue(cli.getOutput(), cli.getOutput().contains(PRODUCER3));
            cli.execute("exclude-package " + transitive.getProducer() + "/p1");
            cli.execute("get-info --type=optional-packages");
            Assert.assertFalse(cli.getOutput(), cli.getOutput().contains("p1"));
            Assert.assertFalse(cli.getOutput(), cli.getOutput().contains(PRODUCER3));

            Path p1 = cli.newDir("exported-transitive1", false);
            cli.execute("provision --dir=" + p1.toFile().getAbsolutePath());
            ProvisioningManager mgr1 = ProvisioningManager.builder().setInstallationHome(p1).build();
            Assert.assertTrue(mgr1.getProvisioningConfig().getTransitiveDeps().size() == 1);
            Assert.assertTrue(mgr1.getProvisioningConfig().getTransitiveDeps().iterator().next().
                    getExcludedPackages().contains("p1"));
            cli.execute("undo");
            cli.execute("get-info --type=optional-packages");
            Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("p1"));
            Assert.assertTrue(cli.getOutput(), cli.getOutput().contains(PRODUCER3));

            cli.execute("exclude-package " + transitive.getProducer() + "/p1");
            cli.execute("get-info --type=optional-packages");
            Assert.assertFalse(cli.getOutput(), cli.getOutput().contains("p1"));
            Assert.assertFalse(cli.getOutput(), cli.getOutput().contains(PRODUCER3));
            cli.execute("remove-excluded-package p1");
            cli.execute("get-info --type=optional-packages");
            Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("p1"));
            Assert.assertTrue(cli.getOutput(), cli.getOutput().contains(PRODUCER3));
            cli.execute("undo");
            cli.execute("get-info --type=optional-packages");
            Assert.assertFalse(cli.getOutput(), cli.getOutput().contains("p1"));
            Assert.assertFalse(cli.getOutput(), cli.getOutput().contains(PRODUCER3));

            cli.execute("remove-excluded-package p1");
            Path p2 = cli.newDir("exported-transitive2", false);
            cli.execute("provision --dir=" + p2.toFile().getAbsolutePath());
            ProvisioningManager mgr2 = ProvisioningManager.builder().setInstallationHome(p2).build();
            Assert.assertTrue(mgr2.getProvisioningConfig().getTransitiveDeps().isEmpty());

            boolean failed = false;
            try {
                cli.execute("ls /packages/" + transitive.getProducer() + "/p2");
            } catch (Exception ex) {
                // expected.
                failed = true;
            }
            if (!failed) {
                throw new Exception("Package p2 shouldn't be present");
            }
            cli.execute("include-package " + transitive.getProducer() + "/p2");

            Path p3 = cli.newDir("exported-transitive3", false);
            cli.execute("provision --dir=" + p3.toFile().getAbsolutePath());
            ProvisioningManager mgr3 = ProvisioningManager.builder().setInstallationHome(p3).build();
            Assert.assertTrue(mgr3.getProvisioningConfig().getTransitiveDeps().size() == 1);
            Assert.assertTrue(mgr3.getProvisioningConfig().getTransitiveDeps().iterator().next().
                    getIncludedPackages().contains("p2"));
            cli.execute("ls /packages/" + transitive.getProducer() + "/p2");
            cli.execute("undo");
            failed = false;
            try {
                cli.execute("ls /packages/" + transitive.getProducer() + "/p2");
            } catch (Exception ex) {
                // expected.
                failed = true;
            }
            if (!failed) {
                throw new Exception("Package p2 shouldn't be present");
            }
            cli.execute("include-package " + transitive.getProducer() + "/p2");

            cli.execute("remove-included-package p2");
            failed = false;
            try {
                cli.execute("ls /packages/" + transitive.getProducer() + "/p2");
            } catch (Exception ex) {
                // expected.
                failed = true;
            }
            if (!failed) {
                throw new Exception("Package p2 shouldn't be present");
            }
            cli.execute("undo");
            cli.execute("ls /packages/" + transitive.getProducer() + "/p2");
            cli.execute("remove-included-package p2");

            Path p4 = cli.newDir("exported-transitive4", false);
            cli.execute("provision --dir=" + p4.toFile().getAbsolutePath());
            ProvisioningManager mgr4 = ProvisioningManager.builder().setInstallationHome(p4).build();
            Assert.assertTrue(mgr4.getProvisioningConfig().getTransitiveDeps().isEmpty());
        } finally {
            cli.execute("leave-state");
        }
    }

    @Test
    public void testTransitiveWithVersion() throws Exception {
        ProvisioningConfig config = ProvisioningConfig.builder().addFeaturePackDep(locWithTransitive).
                addTransitiveDep(transitive).build();
        Path p = cli.newDir("version_transitive", false);
        //Provision a config with a transitive with version set.
        ProvisioningManager mgr1 = cli.getSession().newProvisioningManager(p, true);
        mgr1.provision(config);
        cli.execute("state edit " + p.toFile().getAbsolutePath());
        try {
            cli.execute("get-info --type=optional-packages");
            Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("p1"));
            Assert.assertTrue(cli.getOutput(), cli.getOutput().contains(PRODUCER3));
            cli.execute("exclude-package " + transitive.getProducer() + "/p1");
            cli.execute("get-info --type=optional-packages");
            Assert.assertFalse(cli.getOutput(), cli.getOutput().contains("p1"));
            Assert.assertFalse(cli.getOutput(), cli.getOutput().contains(PRODUCER3));
            cli.execute("undo");
            // Check that we still have the transitive dep.
            ProvisioningManager mgr2 = ProvisioningManager.builder().setInstallationHome(p).build();
            Assert.assertTrue(mgr2.getProvisioningConfig().getTransitiveDeps().size() == 1);
            Assert.assertTrue(mgr2.getProvisioningConfig().getTransitiveDeps().iterator().next().
                    getExcludedPackages().isEmpty());
        } finally {
            cli.execute("leave-state");
        }
    }

    @Test
    public void testTransitiveWithExistingPackages() throws Exception {
        FeaturePackConfig transitiveConfig = FeaturePackConfig.transitiveBuilder(transitive).
                excludePackage("p1").includePackage("p2").build();
        FeaturePackConfig topLevelConfig = FeaturePackConfig.builder(locWithTransitive).setInheritPackages(false).build();
        ProvisioningConfig config = ProvisioningConfig.builder().addFeaturePackDep(topLevelConfig).
                addFeaturePackDep(transitiveConfig).build();
        Path p = cli.newDir("transitive_packages", false);
        //Provision a config with a transitive with already excluded package
        ProvisioningManager mgr1 = cli.getSession().newProvisioningManager(p, true);
        mgr1.provision(config);
        cli.execute("state edit " + p.toFile().getAbsolutePath());
        try {
            cli.execute("get-info --type=optional-packages");
            Assert.assertFalse(cli.getOutput(), cli.getOutput().contains("p1"));
            Assert.assertFalse(cli.getOutput(), cli.getOutput().contains(PRODUCER3));
            cli.execute("remove-excluded-package p1");
            cli.execute("get-info --type=optional-packages");
            Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("p1"));
            Assert.assertTrue(cli.getOutput(), cli.getOutput().contains(PRODUCER3));
            cli.execute("undo");

            cli.execute("ls /packages/" + transitive.getProducer() + "/p2");
            cli.execute("remove-included-package p2");
            boolean failed = false;
            try {
                cli.execute("ls /packages/" + transitive.getProducer() + "/p2");
            } catch (Exception ex) {
                // expected.
                failed = true;
            }
            if (!failed) {
                throw new Exception("Package p2 shouldn't be present");
            }
            cli.execute("undo");
            Path p2 = cli.newDir("transitive_packages_reprovisioned", false);
            cli.execute("provision --dir=" + p2.toFile().getAbsolutePath());
            // Check that we still have the transitive dep and its original content.
            ProvisioningManager mgr2 = ProvisioningManager.builder().setInstallationHome(p2).build();
            Assert.assertTrue(mgr2.getProvisioningConfig().getTransitiveDeps().size() == 1);
            Assert.assertTrue(mgr2.getProvisioningConfig().getTransitiveDeps().iterator().next().
                    getExcludedPackages().contains("p1"));
            Assert.assertTrue(mgr2.getProvisioningConfig().getTransitiveDeps().iterator().next().
                    getIncludedPackages().contains("p2"));
        } finally {
            cli.execute("leave-state");
        }
    }

    @Test
    public void testLayers() throws Exception {
        cli.execute("state new");
        try {

            cli.execute("add-dependency " + locLayers);
            cli.execute("get-info --type=configs");
            Assert.assertFalse(cli.getOutput(), cli.getOutput().contains("testmodel"));

            try {
                cli.execute("include-layers testmodel/testmodel1 --layers=layer1");
                throw new Exception("Should have failed");
            } catch (CommandException ex) {
                // OK expected,config must exist.
            }

            cli.execute("define-config --model=testmodel --name=foo.xml");
            cli.execute("get-info --type=configs");
            Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("foo.xml"));
            Assert.assertFalse(cli.getOutput(), cli.getOutput().contains("layer1"));

            cli.execute("undo");
            cli.execute("get-info --type=configs");
            Assert.assertFalse(cli.getOutput(), cli.getOutput().contains("foo.xml"));

            cli.execute("define-config --model=testmodel --name=foo.xml");

            cli.execute("include-layers testmodel/foo.xml --layers=layer1,layer2");
            cli.execute("get-info --type=configs");
            Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("layer1"));
            Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("layer2"));

            cli.execute("undo");
            cli.execute("get-info --type=configs");
            Assert.assertFalse(cli.getOutput(), cli.getOutput().contains("layer1"));
            Assert.assertFalse(cli.getOutput(), cli.getOutput().contains("layer2"));

            cli.execute("include-layers testmodel/foo.xml --layers=layer1,layer2");

            cli.execute("remove-included-layers testmodel/foo.xml --layers=layer1,layer2");
            cli.execute("get-info --type=configs");
            Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("foo.xml"));
            Assert.assertFalse(cli.getOutput(), cli.getOutput().contains("layer1"));
            Assert.assertFalse(cli.getOutput(), cli.getOutput().contains("layer2"));

            cli.execute("undo");
            cli.execute("get-info --type=configs");
            Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("foo.xml"));
            Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("layer1"));
            Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("layer2"));

            cli.execute("remove-included-layers testmodel/foo.xml --layers=layer1,layer2");

            try {
                cli.execute("include-layers testmodel --layers=layer1");
                throw new Exception("Should have failed");
            } catch (CommandException ex) {
                // OK expected,config must exist.
            }

            try {
                cli.execute("include-layers testmodel/ --layers=layer1");
                throw new Exception("Should have failed");
            } catch (CommandException ex) {
                // OK expected,config must exist.
            }

            cli.execute("reset-config testmodel/foo.xml");
            cli.execute("get-info --type=configs");
            Assert.assertFalse(cli.getOutput(), cli.getOutput().contains("foo.xml"));

            cli.execute("undo");
            cli.execute("get-info --type=configs");
            Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("foo.xml"));

            cli.execute("reset-config testmodel/foo.xml");

            cli.execute("include-config testmodel/testmodel1");
            try {
                cli.execute("exclude-layers testmodel/testmodel1 --layers=base");
                throw new Exception("Should have failed");
            } catch (CommandException ex) {
                // OK expected, base is required by layer1.
            }

            try {
                cli.execute("include-layers testmodel/testmodel1 --layers=base");
                throw new Exception("Should have failed");
            } catch (CommandException ex) {
                // OK expected, base is already included.
            }

            try {
                cli.execute("remove-included-layers testmodel/testmodel1 --layers=layer1");
                throw new Exception("Should have failed");
            } catch (CommandException ex) {
                // OK expected, layer1 is not explicitly included.
            }

            try {
                cli.execute("remove-excluded-layers testmodel/testmodel1 --layers=layer1");
                throw new Exception("Should have failed");
            } catch (CommandException ex) {
                // OK expected, layer1 is not explicitly excluded.
            }

            try {
                cli.execute("exclude-layers testmodel/testmodel1 --layers=layer2");
                throw new Exception("Should have failed");
            } catch (CommandException ex) {
                // OK expected, layer2 is not included in default config.
            }

            cli.execute("exclude-layers testmodel/testmodel1 --layers=layer1");
            cli.execute("get-info --type=configs");
            Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("layer1(excluded)"));

            cli.execute("undo");
            cli.execute("get-info --type=configs");
            Assert.assertFalse(cli.getOutput(), cli.getOutput().contains("layer1(excluded)"));

            cli.execute("exclude-layers testmodel/testmodel1 --layers=layer1");

            cli.execute("remove-excluded-layers testmodel/testmodel1 --layers=layer1");
            cli.execute("get-info --type=configs");
            Assert.assertFalse(cli.getOutput(), cli.getOutput().contains("layer1(excluded)"));

            cli.execute("include-layers testmodel/testmodel1 --layers=layer2");
            cli.execute("get-info --type=configs");
            Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("layer1"));
            Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("layer2"));
            cli.execute("remove-included-layers testmodel/testmodel1 --layers=layer2");
            cli.execute("get-info --type=configs");
            Assert.assertTrue(cli.getOutput(), cli.getOutput().contains("layer1"));
            Assert.assertFalse(cli.getOutput(), cli.getOutput().contains("layer2"));
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

    public static void installWithDependency(String version) throws ProvisioningException {
        FeaturePackCreator creator = FeaturePackCreator.getInstance().addArtifactResolver(cli.getSession().getMavenRepoManager());
        FeaturePackLocation fp1 = new FeaturePackLocation(universeSpec,
                PRODUCER3, "1", null, version);
        creator.newFeaturePack(fp1.getFPID()).addFeatureSpec(FeatureSpec.builder("specA").addPackageDep("p1", true)
                .addParam(FeatureParameterSpec.createId("p1"))
                .build())
                .addConfig(ConfigModel.builder().setModel("model1").
                        setName("name1").
                        addFeature(new FeatureConfig("specA").setParam("p1", "1")).build(), true)
                .newPackage("p1", false)
                .writeContent("fp1/p1.txt", "fp1 p1").getFeaturePack().
                newPackage("p2", true)
                .writeContent("fp2/p2.txt", "fp1 p2");

        FeaturePackLocation fp2 = new FeaturePackLocation(universeSpec,
                PRODUCER4, "1", null, version);
        FeaturePackConfig dep = FeaturePackConfig.builder(fp1).setInheritConfigs(false).setInheritPackages(false).build();
        creator.newFeaturePack(fp2.getFPID())
                .addDependency(dep).addFeatureSpec(FeatureSpec.builder("specB")
                .addParam(FeatureParameterSpec.createId("p1"))
                .build()).
                addConfig(ConfigModel.builder().setModel("model1").
                        setName("name1").addFeature(new FeatureConfig("specB").setParam("p1", "1")).
                        addFeature(new FeatureConfig("specA").setParam("p1", "1")).build(), true);
        creator.install();
    }

    public static void installLayers(String version) throws ProvisioningException {
        FeaturePackCreator creator = FeaturePackCreator.getInstance().addArtifactResolver(cli.getSession().getMavenRepoManager());
        FeaturePackLocation fp1 = new FeaturePackLocation(universeSpec,
                PRODUCER2, "1", null, version);
        creator.newFeaturePack(fp1.getFPID())
                .addFeatureSpec(FeatureSpec.builder(PRODUCER2 + "-FeatureA")
                        .addParam(FeatureParameterSpec.createId("id"))
                        .build())
                .addConfigLayer(ConfigLayerSpec.builder()
                        .setModel("testmodel").setName("base")
                        .addFeature(new FeatureConfig(PRODUCER2 + "-FeatureA").setParam("id", "base"))
                        .build())
                .addConfigLayer(ConfigLayerSpec.builder()
                        .setModel("testmodel").setName("layer1")
                        .addLayerDep("base")
                        .addFeature(new FeatureConfig(PRODUCER2 + "-FeatureA").setParam("id", "layer1"))
                        .build())
                .addConfigLayer(ConfigLayerSpec.builder()
                        .setModel("testmodel").setName("layer2")
                        .addFeature(new FeatureConfig(PRODUCER2 + "-FeatureA").setParam("id", "layer2"))
                        .build())
                .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "fp1 p1").
                getFeaturePack()
                .addConfig(ConfigModel.builder().setModel("testmodel").
                        setName("testmodel1").includeLayer("layer1").build());
        creator.install();
    }
}
