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
import org.aesh.command.CommandException;
import org.jboss.galleon.Constants;
import org.jboss.galleon.ProvisioningManager;
import static org.jboss.galleon.cli.CliTestUtils.PRODUCER1;
import static org.jboss.galleon.cli.CliTestUtils.PRODUCER2;
import static org.jboss.galleon.cli.CliTestUtils.UNIVERSE_NAME;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.UniverseSpec;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jdenise@redhat.com
 */
public class LayersTestCase {
    private static UniverseSpec universeSpec;
    private static CliWrapper cli;
    private static MvnUniverse universe;

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
        FeaturePackLocation prod1 = newFpl(PRODUCER1, "1", "1.0.0.Final");
        FeaturePackLocation prod2 = newFpl(PRODUCER2, "1", "1.0.0.Final");
        CliTestUtils.installWithLayers(cli, universeSpec, PRODUCER1, "1.0.0.Final");
        CliTestUtils.install(cli, universeSpec, PRODUCER2, "1.0.0.Final");

        cli.execute("find * --layers=layerZ --universe=" + universeSpec);
        assertFalse(cli.getOutput(), cli.getOutput().contains(prod1.toString()));
        cli.execute("find * --layers=layerB --universe=" + universeSpec);
        assertTrue(cli.getOutput(), cli.getOutput().contains(prod1.toString()));
        assertTrue(cli.getOutput(), cli.getOutput().contains("layerB-" + PRODUCER1));

        cli.execute("feature-pack get-info " + prod1 + " --type=layers");
        assertTrue(cli.getOutput(), cli.getOutput().contains("base-" + PRODUCER1));
        assertTrue(cli.getOutput(), cli.getOutput().contains("layerA-" + PRODUCER1));
        assertTrue(cli.getOutput(), cli.getOutput().contains("layerB-" + PRODUCER1));
        assertTrue(cli.getOutput(), cli.getOutput().contains("layerC-" + PRODUCER1));

        cli.execute("feature-pack get-info " + prod1 + " --type=configs");
        assertFalse(cli.getOutput(), cli.getOutput().contains("base-" + PRODUCER1));
        assertFalse(cli.getOutput(), cli.getOutput().contains("layerA-" + PRODUCER1));
        assertTrue(cli.getOutput(), cli.getOutput().contains("layerB-" + PRODUCER1));
        assertFalse(cli.getOutput(), cli.getOutput().contains("layerC-" + PRODUCER1));

        Path path = cli.newDir("prod1", false);

        try {
            cli.execute("install " + prod1 + " --dir=" + path + " --layers=foo");
            throw new Exception("Should have failed");
        } catch (CommandException ex) {
            // XXX OK, expected
        }

        cli.execute("install " + prod1 + " --dir=" + path + " --layers=" + "layerA-" + PRODUCER1
                + ",layerC-" + PRODUCER1);

        ProvisioningConfig config = ProvisioningManager.builder().
                setInstallationHome(path).build().getProvisioningConfig();
        ConfigModel conf = config.getDefinedConfig(new ConfigId("testmodel", "testmodel.xml"));
        assertNotNull(conf);
        assertTrue(conf.getIncludedLayers().size() == 2);
        assertTrue(conf.getIncludedLayers().contains("layerA-" + PRODUCER1));
        assertTrue(conf.getIncludedLayers().contains("layerC-" + PRODUCER1));
        String opt = config.getPluginOption(Constants.OPTIONAL_PACKAGES);
        assertNotNull(opt);
        assertEquals(Constants.PASSIVE_PLUS, opt);

        cli.execute("get-info --dir=" + path + " --type=configs");
        assertTrue(cli.getOutput(), cli.getOutput().contains("layerA-" + PRODUCER1));
        assertTrue(cli.getOutput(), cli.getOutput().contains("layerC-" + PRODUCER1));
        assertFalse(cli.getOutput(), cli.getOutput().contains("base-" + PRODUCER1));

        // Multiple configurations are invalid with layers
        try {
            cli.execute("install " + prod1 + " --dir=" + path
                    + " --config=testmodel/foobar.xml,testmodel/foobar2.xml, --layers=" + "layerB-" + PRODUCER1);
            throw new Exception("should have failed");
        } catch (CommandException ex) {
            // XXX OK expected
        }

        Path path2 = cli.newDir("prod2", false);
        try {
            cli.execute("install " + prod1 + " --dir=" + path2
                    + " --config=moo/foobar.xml --layers=" + "layerB-" + PRODUCER1);
            throw new Exception("should have failed");
        } catch (CommandException ex) {
            // XXX OK expected
        }

        cli.execute("install " + prod1 + " --dir=" + path2
                + " --config=testmodel/foobar.xml --layers=" + "layerB-" + PRODUCER1);

        ProvisioningConfig config2 = ProvisioningManager.builder().
                setInstallationHome(path2).build().getProvisioningConfig();
        ConfigModel conf2 = config2.getDefinedConfig(new ConfigId("testmodel", "foobar.xml"));
        assertNotNull(conf2);
        assertTrue(conf2.getIncludedLayers().size() == 1);
        assertTrue(conf2.getIncludedLayers().contains("layerB-" + PRODUCER1));

        cli.execute("get-info --dir=" + path2 + " --type=configs");
        assertTrue(cli.getOutput(), cli.getOutput().contains("testmodel"));
        assertTrue(cli.getOutput(), cli.getOutput().contains("foobar.xml"));
        assertTrue(cli.getOutput(), cli.getOutput().contains("layerB-" + PRODUCER1));
        assertFalse(cli.getOutput(), cli.getOutput().contains("layerA-" + PRODUCER1));
        assertFalse(cli.getOutput(), cli.getOutput().contains("layerC-" + PRODUCER1));
        assertFalse(cli.getOutput(), cli.getOutput().contains("base-" + PRODUCER1));

        // Default model and config names
        Path path3 = cli.newDir("prod3", false);
        cli.execute("install " + prod1 + " --dir=" + path3
                + " --config=foobar.xml --layers=" + "layerB-" + PRODUCER1);
        cli.execute("get-info --dir=" + path3 + " --type=configs");
        assertTrue(cli.getOutput(), cli.getOutput().contains("testmodel"));
        assertTrue(cli.getOutput(), cli.getOutput().contains("foobar.xml"));
        config = ProvisioningManager.builder().
                setInstallationHome(path3).build().getProvisioningConfig();
        assertTrue(config.getDefinedConfigs().size() == 1);
        conf = config.getDefinedConfig(new ConfigId("testmodel", "foobar.xml"));
        assertNotNull(conf);

        try {
            cli.execute("install " + prod1 + " --dir=" + path3
                    + " --config=foomodel/ --layers=" + "layerB-" + PRODUCER1);
            throw new Exception("Should have failed");
        } catch (CommandException ex) {
            // XXX OK, expected
        }

        Path path4 = cli.newDir("prod4", false);
        cli.execute("install " + prod1 + " --dir=" + path4
                + " --config=testmodel/ --layers=" + "layerB-" + PRODUCER1);
        cli.execute("get-info --dir=" + path4 + " --type=configs");
        assertTrue(cli.getOutput(), cli.getOutput().contains("testmodel"));
        assertTrue(cli.getOutput(), cli.getOutput().contains("testmodel.xml"));
        config = ProvisioningManager.builder().
                setInstallationHome(path4).build().getProvisioningConfig();
        assertTrue(config.getDefinedConfigs().size() == 1);
        conf = config.getDefinedConfig(new ConfigId("testmodel", "testmodel.xml"));
        assertNotNull(conf);

        //Install a specified config without layers
        Path path5 = cli.newDir("prod5", false);
        cli.execute("install " + prod1 + " --dir=" + path5);
        cli.execute("get-info --dir=" + path5 + " --type=configs");
        assertTrue(cli.getOutput(), cli.getOutput().contains("config1.xml"));
        assertTrue(cli.getOutput(), cli.getOutput().contains("config2.xml"));

        cli.execute("install " + prod1 + " --dir=" + path5
                + " --default-configs=testmodel/config1.xml");
        cli.execute("get-info --dir=" + path5 + " --type=configs");
        assertTrue(cli.getOutput(), cli.getOutput().contains("config1.xml"));
        assertFalse(cli.getOutput(), cli.getOutput().contains("config2.xml"));

        cli.execute("install " + prod1 + " --dir=" + path5
                + " --default-configs=testmodel/config1.xml,testmodel/config2.xml");
        cli.execute("get-info --dir=" + path5 + " --type=configs");
        assertTrue(cli.getOutput(), cli.getOutput().contains("config1.xml"));
        assertTrue(cli.getOutput(), cli.getOutput().contains("config2.xml"));

        //Install multiple producers, installing default-config should not erase existing producer.
        Path path6 = cli.newDir("prod6", false);
        cli.execute("install " + prod2 + " --dir=" + path6);
        cli.execute("install " + prod1 + " --dir=" + path6
                + " --default-configs=testmodel/config1.xml");
        cli.execute("get-info --dir=" + path6);
        assertTrue(cli.getOutput(), cli.getOutput().contains(PRODUCER1));
        assertTrue(cli.getOutput(), cli.getOutput().contains(PRODUCER2));

        //Install a default-config into empty directory
        Path path7 = cli.newDir("prod7", false);
        cli.execute("install " + prod1 + " --dir=" + path7
                + " --default-configs=testmodel/config1.xml");
        cli.execute("get-info --dir=" + path7 + " --type=configs");
        assertTrue(cli.getOutput(), cli.getOutput().contains(PRODUCER1));
        assertTrue(cli.getOutput(), cli.getOutput().contains("config1.xml"));

        //Install layers in multiple steps
        Path path8 = cli.newDir("prod8", false);
        cli.execute("install " + prod1 + " --dir=" + path8 + " --layers=" + "layerA-" + PRODUCER1);
        cli.execute("install " + prod1 + " --dir=" + path8 + " --layers=" + "layerC-" + PRODUCER1);
        ProvisioningConfig config3 = ProvisioningManager.builder().
                setInstallationHome(path8).build().getProvisioningConfig();
        ConfigModel conf3 = config3.getDefinedConfig(new ConfigId("testmodel", "testmodel.xml"));
        assertNotNull(conf3);
        assertTrue(conf3.getIncludedLayers().size() == 2);
        assertTrue(conf3.getIncludedLayers().contains("layerA-" + PRODUCER1));
        assertTrue(conf3.getIncludedLayers().contains("layerC-" + PRODUCER1));
    }

    protected FeaturePackLocation newFpl(String producer, String channel, String build) {
        return new FeaturePackLocation(universeSpec, producer, channel, null, build);
    }
}
