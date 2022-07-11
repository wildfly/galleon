/*
 * Copyright 2016-2022 Red Hat, Inc. and/or its affiliates
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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import javax.xml.stream.XMLStreamException;

import static org.jboss.galleon.cli.CliTestUtils.PRODUCER1;
import static org.jboss.galleon.cli.CliTestUtils.UNIVERSE_NAME;

import org.jboss.galleon.Constants;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.state.ProvisionedConfig;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.UniverseSpec;
import org.jboss.galleon.xml.ProvisionedConfigBuilder;
import org.jboss.galleon.xml.ProvisionedConfigXmlWriter;
import org.jboss.galleon.xml.ProvisionedFeatureBuilder;
import org.junit.AfterClass;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jdenise@redhat.com
 */
public class ChangesTestCase {
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
        Path p = cli.newDir("install", false);
        FeaturePackLocation fpl = CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", "alpha", "1.0.0.Alpha1");
        cli.execute("install " + fpl + " --dir=" + p);
        Path root = p.resolve(PRODUCER1);
        Path file = root.resolve("p1.txt");
        Path newFile = p.resolve("newfile.xml");
        Files.createFile(newFile);
        Files.write(file, "HelloWorld".getBytes());

        cli.execute("cd " + p.resolve(".."));

        cli.execute("get-changes --dir=" + p.getFileName());

        Path nf = p.getParent().relativize(newFile);
        Path f = p.getParent().relativize(file);
        assertTrue(cli.getOutput(), cli.getOutput().contains(" + " + nf));
        assertTrue(cli.getOutput(), cli.getOutput().contains(" M " + f));

        cli.execute("cd " + p);
        cli.execute("get-changes");
        nf = p.relativize(newFile);
        f = p.relativize(file);
        assertTrue(cli.getOutput(), cli.getOutput().contains(" + " + nf));
        assertTrue(cli.getOutput(), cli.getOutput().contains(" M " + f));

        cli.execute("cd " + root);
        cli.execute("get-changes");

        nf = root.relativize(newFile);
        f = root.relativize(file);
        assertTrue(cli.getOutput(), cli.getOutput().contains(" + " + nf));
        assertTrue(cli.getOutput(), cli.getOutput().contains(" M " + f));

        Files.delete(file);

        cli.execute("get-changes");
        assertTrue(cli.getOutput(), cli.getOutput().contains(" + " + nf));
        assertTrue(cli.getOutput(), cli.getOutput().contains(" - " + f));
    }

    @Test
    public void testPersist() throws Exception {
        CliTestUtils.installWithLayers(cli, universeSpec, PRODUCER1, "1.0.0.Alpha2");
        Path p = cli.newDir("install-persist", false);
        FeaturePackLocation fpl = CliTestUtils.buildFPL(universeSpec, PRODUCER1, "1", "alpha", "1.0.0.Alpha2");
        cli.execute("install " + fpl + " --dir=" + p);
        cli.execute("persist-changes --dir=" + p);
        assertTrue(cli.getOutput(), cli.getOutput().contains("No changes to persist"));
        Path root = p.resolve(PRODUCER1);
        Path file = root.resolve("p1.txt");
        Files.write(file, "HelloWorld".getBytes());
        cli.execute("persist-changes --dir=" + p);
        assertTrue(cli.getOutput(), cli.getOutput().contains("No changes to persist"));

        overwrite(p, ProvisionedConfigBuilder.builder()
                .setModel("testmodel")
                .setName("config1")
                .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(fpl.getProducer(), PRODUCER1 + "-FeatureA", "id", "2")).build())
                .build());
        cli.execute("persist-changes --dir=" + p);
        assertFalse(cli.getOutput(), cli.getOutput().contains("No changes to persist"));
    }

    protected void overwrite(Path home, ProvisionedConfig config) throws ProvisioningException {
        Path p = home.resolve(Constants.CONFIGS);
        if(config.getModel() != null) {
            p = p.resolve(config.getModel());
        }
        p = p.resolve(config.getName());
        try {
            Files.createDirectories(p.getParent());
        } catch (IOException e1) {
            throw new ProvisioningException(Errors.mkdirs(p.getParent()), e1);
        }
        try(BufferedWriter writer = Files.newBufferedWriter(p)) {
            ProvisionedConfigXmlWriter.getInstance().write(config, writer);
        } catch (IOException | XMLStreamException e) {
            throw new ProvisioningException("Failed to store " + new ConfigId(config.getModel(), config.getName()) + " in a string", e);
        }
    }
}
