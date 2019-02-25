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

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import static org.jboss.galleon.cli.CliTestUtils.PRODUCER1;
import static org.jboss.galleon.cli.CliTestUtils.PRODUCER2;
import static org.jboss.galleon.cli.CliTestUtils.PRODUCER3;
import static org.jboss.galleon.cli.CliTestUtils.UNIVERSE_NAME;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.spec.ConfigLayerSpec;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.spec.PackageDependencySpec;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.UniverseSpec;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author jdenise@redhat.com
 */
public class AdvancedLayersTestCase {

    private static UniverseSpec universeSpec;
    private static CliWrapper cli;
    private static MvnUniverse universe;

    @BeforeClass
    public static void setup() throws Exception {
        cli = new CliWrapper();
        universe = MvnUniverse.getInstance(UNIVERSE_NAME, cli.getSession().getMavenRepoManager());
        universeSpec = CliTestUtils.setupUniverse(universe, cli, UNIVERSE_NAME, Arrays.asList(PRODUCER1, PRODUCER2, PRODUCER3));
    }

    @AfterClass
    public static void tearDown() {
        //cli.close();
    }

    @Test
    public void test() throws Exception {
        FeaturePackLocation prod1 = newFpl(PRODUCER1, "1", "1.0.0.Final");
        buildFP(cli, universeSpec, PRODUCER1, "1.0.0.Final");
        {
            Path path = cli.newDir("prod1", false);
            cli.execute("install " + prod1 + " --dir=" + path
                    + " --layers=layerA-" + PRODUCER1 + " --optional-packages=passive+");

            ProvisionedState state = ProvisioningManager.builder().setInstallationHome(path).build().getProvisionedState();
            Set<String> pkgs = state.getFeaturePack(prod1.getProducer()).getPackageNames();
            assertTrue(pkgs.toString(), pkgs.contains("p1-required"));
            assertTrue(pkgs.toString(), pkgs.contains("p1-ref-from-required"));
            assertTrue(pkgs.toString(), pkgs.contains("p1-optional"));
            assertTrue(pkgs.toString(), pkgs.contains("p1-ref-from-optional1"));
            assertTrue(pkgs.toString(), pkgs.contains("p1-ref-from-optional2"));
            assertFalse(pkgs.toString(), pkgs.contains("p1-passive"));
            assertTrue(pkgs.toString(), pkgs.contains("p2-passive"));

            cli.execute("get-info --dir=" + path + " --type=optional-packages");
            assertTrue(cli.getOutput(), cli.getOutput().contains("p1-optional"));
            assertFalse(cli.getOutput().contains("p1-passive"));
            assertTrue(cli.getOutput().contains("p2-passive"));
            assertFalse(cli.getOutput().contains("p1-required"));
        }
        {
            Path path = cli.newDir("prod2", false);
            cli.execute("install " + prod1 + " --dir=" + path
                    + " --layers=layerA-" + PRODUCER1 + ",layerB-" + PRODUCER1
                    + " --optional-packages=passive+");
            ProvisionedState state = ProvisioningManager.builder().setInstallationHome(path).build().getProvisionedState();
            Set<String> pkgs = state.getFeaturePack(prod1.getProducer()).getPackageNames();
            assertTrue(pkgs.toString(), pkgs.contains("p1-required"));
            assertTrue(pkgs.toString(), pkgs.contains("p1-ref-from-required"));
            assertTrue(pkgs.toString(), pkgs.contains("p1-optional"));
            assertTrue(pkgs.toString(), pkgs.contains("p1-ref-from-optional1"));
            assertTrue(pkgs.toString(), pkgs.contains("p1-ref-from-optional2"));
            assertTrue(pkgs.toString(), pkgs.contains("p1-passive"));
            assertTrue(pkgs.toString(), pkgs.contains("p2-passive"));
            assertTrue(pkgs.toString(), pkgs.contains("p2-required"));
            assertTrue(pkgs.toString(), pkgs.contains("p2-optional"));
        }
        {
            Path path = cli.newDir("prod3", false);
            cli.execute("install " + prod1 + " --dir=" + path
                    + " --layers=layerA-" + PRODUCER1 + " --optional-packages=none");

            cli.execute("get-info --dir=" + path + " --type=optional-packages");
            assertTrue(cli.getOutput().contains("No optional packages."));
            assertFalse(cli.getOutput().contains("p1-passive"));
            assertFalse(cli.getOutput().contains("p2-passive"));
            assertFalse(cli.getOutput().contains("p1-optional"));
        }
        {
            Path path = cli.newDir("prod4", false);
            cli.execute("install " + prod1 + " --dir=" + path
                    + " --layers=layerA-" + PRODUCER1 + " --optional-packages=passive");

            cli.execute("get-info --dir=" + path + " --type=optional-packages");
            assertFalse(cli.getOutput().contains("p1-passive"));
            assertTrue(cli.getOutput().contains("p2-passive"));
            assertFalse(cli.getOutput().contains("p1-optional"));
        }
        {
            Path path = cli.newDir("prod4", false);
            cli.execute("install " + prod1 + " --dir=" + path
                    + " --layers=layerA-" + PRODUCER1 + " --optional-packages=all");

            cli.execute("get-info --dir=" + path + " --type=optional-packages");
            assertTrue(cli.getOutput().contains("p1-passive"));
            assertTrue(cli.getOutput().contains("p2-passive"));
            assertTrue(cli.getOutput().contains("p1-optional"));
            assertFalse(cli.getOutput().contains("p2-optional"));

            cli.execute("feature-pack get-info " + prod1 + " --type=optional-packages");
            assertTrue(cli.getOutput().contains("p1-passive"));
            assertTrue(cli.getOutput().contains("p2-passive"));
            assertTrue(cli.getOutput().contains("p1-optional"));
            assertTrue(cli.getOutput().contains("p2-optional"));
        }
    }

    @Test
    public void testWithFPDependency() throws Exception {
        FeaturePackLocation prod2 = newFpl(PRODUCER2, "1", "1.0.0.Final");
        buildFPWithDependency(cli, universeSpec, PRODUCER2, "1.0.0.Final");

        {
            Path path = cli.newDir("prod3", false);
            cli.execute("install " + prod2 + " --dir=" + path
                    + " --layers=layerA-" + PRODUCER2 + " --optional-packages=passive+");

            ProvisionedState state = ProvisioningManager.builder().setInstallationHome(path).build().getProvisionedState();
            Set<String> pkgs = state.getFeaturePack(prod2.getProducer()).getPackageNames();
            assertTrue(pkgs.toString(), pkgs.contains("p1-required"));
            assertTrue(pkgs.toString(), pkgs.contains("p1-ref-from-required"));
            assertTrue(pkgs.toString(), pkgs.contains("p1-optional"));
            assertTrue(pkgs.toString(), pkgs.contains("p1-ref-from-optional"));
            assertFalse(pkgs.toString(), pkgs.contains("p1-passive"));

            FeaturePackLocation dep = newFpl(PRODUCER3, "1", "1.0.0.Final");
            Set<String> pkgs2 = state.getFeaturePack(dep.getProducer()).getPackageNames();
            assertTrue(pkgs2.toString(), pkgs2.contains("p0-required1"));
            assertTrue(pkgs2.toString(), pkgs2.contains("p0-required2"));
            assertFalse(pkgs2.toString(), pkgs2.contains("p0-required3"));
        }

        {
            Path path = cli.newDir("prod3", false);
            cli.execute("install " + prod2 + " --dir=" + path
                    + " --layers=layerA-" + PRODUCER2 + ",layer0-" + PRODUCER3 + " --optional-packages=passive+");

            ProvisionedState state = ProvisioningManager.builder().setInstallationHome(path).build().getProvisionedState();
            Set<String> pkgs = state.getFeaturePack(prod2.getProducer()).getPackageNames();
            assertTrue(pkgs.toString(), pkgs.contains("p1-required"));
            assertTrue(pkgs.toString(), pkgs.contains("p1-ref-from-required"));
            assertTrue(pkgs.toString(), pkgs.contains("p1-optional"));
            assertTrue(pkgs.toString(), pkgs.contains("p1-ref-from-optional"));
            assertTrue(pkgs.toString(), pkgs.contains("p1-passive"));

            FeaturePackLocation dep = newFpl(PRODUCER3, "1", "1.0.0.Final");
            Set<String> pkgs2 = state.getFeaturePack(dep.getProducer()).getPackageNames();
            assertTrue(pkgs2.toString(), pkgs2.contains("p0-required1"));
            assertTrue(pkgs2.toString(), pkgs2.contains("p0-required2"));
            assertTrue(pkgs2.toString(), pkgs2.contains("p0-required3"));
        }

    }

    private static void buildFP(CliWrapper cli, UniverseSpec universeSpec,
            String producer, String version) throws ProvisioningException {
        FeaturePackCreator creator = FeaturePackCreator.getInstance().addArtifactResolver(cli.getSession().getMavenRepoManager());
        FeaturePackLocation fp1 = new FeaturePackLocation(universeSpec,
                producer, "1", null, version);
        creator.newFeaturePack(fp1.getFPID())
                .addFeatureSpec(FeatureSpec.builder().addPackageDep("p1-required").
                        addPackageDep("p1-optional", true).
                        addPackageDep(PackageDependencySpec.newInstance("p1-passive", PackageDependencySpec.PASSIVE)).
                        addPackageDep(PackageDependencySpec.newInstance("p2-passive", PackageDependencySpec.PASSIVE)).
                        setName("feat1").addParam(FeatureParameterSpec.createId("p1")).build())
                .addFeatureSpec(FeatureSpec.builder().addPackageDep("p2-required").
                        addPackageDep("p2-optional", true).
                        setName("feat2").addParam(FeatureParameterSpec.createId("p2")).build())
                .addConfigLayer(ConfigLayerSpec.builder()
                        .setModel("testmodel").setName("base-" + producer)
                        .build())
                .addConfigLayer(ConfigLayerSpec.builder()
                        .setModel("testmodel").setName("layerA-" + producer)
                        .addLayerDep("base-" + producer).addFeature(FeatureConfig.newConfig("feat1").setParam("p1", "1"))
                        .build())
                .addConfigLayer(ConfigLayerSpec.builder()
                        .setModel("testmodel").setName("layerB-" + producer)
                        .addLayerDep("base-" + producer).addFeature(FeatureConfig.newConfig("feat2").setParam("p2", "1"))
                        .build())
                .addConfig(ConfigModel.builder("testmodel", "foo.xml").
                        includeLayer("layerA-" + producer).
                        includeLayer("layerB-" + producer).build(), true)
                .newPackage("p1-required", false).addDependency("p1-ref-from-required")
                .writeContent("fp1/p1-required.txt", "fp1 p1").getFeaturePack().
                newPackage("p1-ref-from-required", false)
                .writeContent("fp1/p1-ref-from-required.txt", "fp1 p1").getFeaturePack().
                newPackage("p1-optional", false).addDependency("p1-ref-from-optional1").
                addDependency("p1-ref-from-optional2", true)
                .writeContent("fp1/p1-optional.txt", "fp1 p1").getFeaturePack().
                newPackage("p1-ref-from-optional1", false)
                .writeContent("fp1/p1-ref-from-optional1.txt", "fp1 p1").getFeaturePack().
                newPackage("p1-ref-from-optional2", false)
                .writeContent("fp1/p1-ref-from-optional2.txt", "fp1 p1").getFeaturePack().
                newPackage("p1-passive", false).addDependency("p2-required").addDependency("p2-passive")
                .writeContent("fp1/p1-passive.txt", "fp1 p1").getFeaturePack().
                newPackage("p2-passive", false).addDependency("p1-required")
                .writeContent("fp1/p2-passive.txt", "fp1 p1").getFeaturePack().
                newPackage("p2-required", false)
                .writeContent("fp1/p2-required.txt", "fp1 p1").getFeaturePack().
                newPackage("p2-optional", false)
                .writeContent("fp1/p2-optional.txt", "fp1 p1").getFeaturePack();
        creator.install();
    }

    private static void buildFPWithDependency(CliWrapper cli, UniverseSpec universeSpec,
            String producer, String version) throws ProvisioningException {
        FeaturePackLocation fpDep = new FeaturePackLocation(universeSpec,
                PRODUCER3, "1", null, version);
        FeaturePackCreator creatorDep = FeaturePackCreator.getInstance().
                addArtifactResolver(cli.getSession().getMavenRepoManager());
        creatorDep.newFeaturePack(fpDep.getFPID()).
                addFeatureSpec(FeatureSpec.builder().addPackageDep("p0-required3").
                        setName("feat0").build()).
                addConfigLayer(ConfigLayerSpec.builder()
                        .setModel("testmodel").setName("layer0-" + PRODUCER3)
                        .addConfigItem(FeatureConfig.newConfig("feat0"))
                        .build()).
                newPackage("p0-required1",
                        false).writeContent(PRODUCER3 + "/p0-required1.txt", "fp1 p1").getFeaturePack().
                newPackage("p0-required2", false).
                writeContent(PRODUCER3 + "/p0-required2.txt", "fp1 p1").getFeaturePack().
                newPackage("p0-required3",
                        false).
                writeContent(PRODUCER3 + "/p0-required3.txt", "fp1 p1").getFeaturePack();
        creatorDep.install();

        FeaturePackCreator creator = FeaturePackCreator.getInstance().
                addArtifactResolver(cli.getSession().getMavenRepoManager());

        FeaturePackLocation fp1 = new FeaturePackLocation(universeSpec,
                producer, "1", null, version);
        creator.newFeaturePack(fp1.getFPID()).addDependency(PRODUCER3, fpDep)
                .addFeatureSpec(FeatureSpec.builder().addPackageDep("p1-required").
                        addPackageDep("p1-optional", true).
                        addPackageDep(PackageDependencySpec.newInstance("p1-passive", PackageDependencySpec.PASSIVE)).
                        setName("feat1").build())
                .addConfigLayer(ConfigLayerSpec.builder()
                        .setModel("testmodel").setName("base-" + producer)
                        .build())
                .addConfigLayer(ConfigLayerSpec.builder()
                        .setModel("testmodel").setName("layerA-" + producer)
                        .addLayerDep("base-" + producer).addConfigItem(FeatureConfig.newConfig("feat1"))
                        .build())
                .newPackage("p1-required", false).addDependency("p1-ref-from-required").
                addDependency(PRODUCER3, "p0-required1")
                .writeContent("fp1/p1-required.txt", "fp1 p1").getFeaturePack().
                newPackage("p1-ref-from-required", false)
                .writeContent("fp1/p1-ref-from-required.txt", "fp1 p1").getFeaturePack().
                newPackage("p1-passive", false).addDependency(PRODUCER3, "p0-required3")
                .writeContent("fp1/p1-optional.txt", "fp1 p1").getFeaturePack().
                newPackage("p1-optional", false).addDependency("p1-ref-from-optional").
                addDependency(PRODUCER3, "p0-required2")
                .writeContent("fp1/p1-optional.txt", "fp1 p1").getFeaturePack().
                newPackage("p1-ref-from-optional", false)
                .writeContent("fp1/p1-ref-from-optional.txt", "fp1 p1").getFeaturePack();
        creator.install();
    }

    private FeaturePackLocation newFpl(String producer, String channel, String build) {
        return new FeaturePackLocation(universeSpec, producer, channel, null, build);
    }
}
