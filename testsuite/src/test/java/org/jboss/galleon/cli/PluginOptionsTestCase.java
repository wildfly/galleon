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

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.aesh.command.CommandException;
import org.jboss.galleon.ProvisioningException;
import static org.jboss.galleon.cli.CliTestUtils.PRODUCER1;
import static org.jboss.galleon.cli.CliTestUtils.PRODUCER2;
import static org.jboss.galleon.cli.CliTestUtils.UNIVERSE_NAME;
import org.jboss.galleon.plugin.InstallPlugin;
import org.jboss.galleon.plugin.PluginOption;
import org.jboss.galleon.runtime.ProvisioningRuntime;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.UniverseSpec;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jdenise@redhat.com
 */
public class PluginOptionsTestCase {

    public static class PluginTest implements InstallPlugin {
        private static final Map<String, PluginOption> OPTIONS = new HashMap<>();
        private static final String OPT1_REQUIRED = "opt1-req";
        private static final String OPT2_NOT_REQUIRED = "opt2-not-req";
        private static final String OPT3_NOVALUE = "opt3-not-value";
        static {
            OPTIONS.put(OPT1_REQUIRED, PluginOption.builder(OPT1_REQUIRED).setRequired().build());
            OPTIONS.put(OPT2_NOT_REQUIRED, PluginOption.builder(OPT2_NOT_REQUIRED).build());
            OPTIONS.put(OPT3_NOVALUE, PluginOption.builder(OPT3_NOVALUE).hasNoValue().build());
        }

        @Override
        public void postInstall(ProvisioningRuntime ctx) throws ProvisioningException {
            // NO-OP.
        }

        @Override
        public Map<String, PluginOption> getOptions() {
            return OPTIONS;
        }
    ;
    }
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
        CliTestUtils.install(cli, universeSpec, PRODUCER1, "1.0.0.Alpha1", PluginTest.class);
        CliTestUtils.install(cli, universeSpec, PRODUCER2, "1.0.0.Alpha1", PluginTest.class);
        Path p = cli.newDir("install", false);
        // INSTALL command
        // Invalid option
        try {
            cli.execute("install " + CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", "alpha", "1.0.0.Alpha1")
                    + " --dir=" + p + " --foo");
            throw new Exception("Should have failed, --foo beeing unknown");
        } catch (CommandException ex) {
            // XXX OK.
        }
        // missing required option
        try {
            cli.execute("install " + CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", "alpha", "1.0.0.Alpha1")
                    + " --dir=" + p);
            throw new Exception("Should have failed, --opt1-req is required.");
        } catch (CommandException ex) {
            // XXX OK.
        }
        // successfull installation
        cli.execute("install " + CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", "alpha", "1.0.0.Alpha1")
                + " --dir=" + p + " --" + PluginTest.OPT1_REQUIRED + "=XXX");
        cli.execute("install " + CliTestUtils.buildFPL(universeSpec, PRODUCER2, "1", "alpha", "1.0.0.Alpha1")
                + " --dir=" + p + " --" + PluginTest.OPT1_REQUIRED + "=XXX");

        // PROVISION command
        Path target = cli.newDir("target", false);
        // Invalid option
        try {
            cli.execute("state provision " + p + File.separator + ".galleon" + File.separator
                    + "provisioning.xml --dir=" + target + " --foo");
            throw new Exception("Should have failed, --foo beeing unknown");
        } catch (CommandException ex) {
            // XXX OK.
        }
        // missing required option
        try {
            cli.execute("state provision " + p + File.separator + ".galleon" + File.separator
                    + "provisioning.xml --dir=" + target);
            throw new Exception("Should have failed, --opt1-req is required.");
        } catch (CommandException ex) {
            // XXX OK.
        }
        // successfull provisioning
        cli.execute("state provision " + p + File.separator + ".galleon" + File.separator
                + "provisioning.xml --dir=" + target + " --" + PluginTest.OPT1_REQUIRED + "=XXX");

        // UNINSTALL command, remaining PRODUCER2 expects some options.
        try {
            cli.execute("uninstall " + CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", "alpha", "1.0.0.Alpha1")
                    + " --dir=" + p + " --foo");
            throw new Exception("Should have failed, --foo beeing unknown");
        } catch (CommandException ex) {
            // XXX OK.
        }
        // missing required option
        try {
            cli.execute("uninstall " + CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", "alpha", "1.0.0.Alpha1")
                    + " --dir=" + p);
            throw new Exception("Should have failed, --opt1-req is required.");
        } catch (CommandException ex) {
            // XXX OK.
        }
        // successfull uninstall
        cli.execute("uninstall " + CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", "alpha", "1.0.0.Alpha1")
                + " --dir=" + p + " --" + PluginTest.OPT1_REQUIRED + "=XXX");
    }
}
