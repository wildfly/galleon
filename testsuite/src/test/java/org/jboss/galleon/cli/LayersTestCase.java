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
import org.jboss.galleon.Constants;
import org.jboss.galleon.ProvisioningManager;
import static org.jboss.galleon.cli.CliTestUtils.PRODUCER1;
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
        universeSpec = CliTestUtils.setupUniverse(universe, cli, UNIVERSE_NAME, Arrays.asList(PRODUCER1));
    }

    @AfterClass
    public static void tearDown() {
        cli.close();
    }

    @Test
    public void test() throws Exception {
        FeaturePackLocation prod1 = newFpl(PRODUCER1, "1", "1.0.0.Final");
        CliTestUtils.installWithLayers(cli, universeSpec, PRODUCER1, "1.0.0.Final");
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
        assertEquals(Constants.PASSIVE, opt);

        cli.execute("get-info --dir=" + path + " --type=configs");
        assertTrue(cli.getOutput(), cli.getOutput().contains("layerA-" + PRODUCER1));
        assertTrue(cli.getOutput(), cli.getOutput().contains("layerC-" + PRODUCER1));
        assertFalse(cli.getOutput(), cli.getOutput().contains("base-" + PRODUCER1));

        Path path2 = cli.newDir("prod2", false);
        try {
            cli.execute("install " + prod1 + " --dir=" + path2 + " --model=moo "
                    + "--config=foobar.xml --layers=" + "layerB-" + PRODUCER1);
            throw new Exception("should have failed");
        } catch (CommandException ex) {
            // XXX OK expected
        }

        cli.execute("install " + prod1 + " --dir=" + path2 + " --model=testmodel "
                + "--config=foobar.xml --layers=" + "layerB-" + PRODUCER1);

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
    }

    protected FeaturePackLocation newFpl(String producer, String channel, String build) {
        return new FeaturePackLocation(universeSpec, producer, channel, null, build);
    }
}
