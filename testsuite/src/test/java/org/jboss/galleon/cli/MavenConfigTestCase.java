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

import java.nio.file.Files;
import java.nio.file.Path;
import org.aesh.command.CommandException;
import org.jboss.galleon.cli.config.Configuration;
import org.jboss.galleon.cli.config.mvn.MavenConfig;
import org.jboss.galleon.cli.config.mvn.MavenRemoteRepository;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jdenise@redhat.com
 */
public class MavenConfigTestCase {

    private static CliWrapper cli;

    @BeforeClass
    public static void setup() throws Exception {
        cli = new CliWrapper();
    }

    @AfterClass
    public static void tearDown() {
        cli.close();
    }

    @Test
    public void testRepository() throws Exception {
        MavenConfig config = cli.getSession().getPmConfiguration().getMavenConfig();
        String name = "foofoo";
        String name2 = "barbar";
        String url = "http://foo";
        try {
            cli.execute("mvn add-repository --name=" + name + " --url=" + url + " --release-update-policy=foo");
            throw new Exception("Should have failed");
        } catch (CommandException ex) {
            // XXX OK
        }

        try {
            cli.execute("mvn add-repository --name=" + name + " --url=" + url + " --snapshot-update-policy=foo");
            throw new Exception("Should have failed");
        } catch (CommandException ex) {
            // XXX OK
        }
        cli.execute("mvn add-repository --name=" + name + " --url=" + url);
        cli.execute("mvn add-repository --name=" + name2 + " --url=" + url + " --snapshot-update-policy=always --release-update-policy=never");
        checkRepositories(config, url, name, name2);
        checkRepositories(Configuration.parse().getMavenConfig(), url, name, name2);

        try {
            cli.execute("mvn remove-repository XXX");
            throw new Exception("Should have failed");
        } catch (CommandException ex) {
            // XXX OK
        }
        cli.execute("mvn info");
        Assert.assertTrue(cli.getOutput().contains(name));
        Assert.assertTrue(cli.getOutput().contains(name2));

        cli.execute("mvn remove-repository " + name);
        cli.execute("mvn remove-repository " + name2);
        checkNoRepositories(config, name, name2);
        checkNoRepositories(Configuration.parse().getMavenConfig(), name, name2);
    }

    @Test
    public void testLocalRepository() throws Exception {
        MavenConfig config = cli.getSession().getPmConfiguration().getMavenConfig();
        Path defaultOriginalPath = config.getLocalRepository();
        Path foo = cli.newDir("foo", true);
        Assert.assertFalse(config.getLocalRepository().endsWith("foo"));
        cli.execute("mvn set-local-repository " + foo.toFile().getAbsolutePath());
        Assert.assertEquals(foo, config.getLocalRepository());
        Assert.assertEquals(foo, Configuration.parse().getMavenConfig().getLocalRepository());

        cli.execute("mvn info");
        Assert.assertTrue(cli.getOutput().contains(foo.toFile().getAbsolutePath()));

        cli.execute("mvn set-local-repository");
        Assert.assertEquals(defaultOriginalPath, config.getLocalRepository());
        Assert.assertEquals(defaultOriginalPath, Configuration.parse().getMavenConfig().getLocalRepository());
    }

    @Test
    public void testSettingsFile() throws Exception {
        MavenConfig config = cli.getSession().getPmConfiguration().getMavenConfig();
        Path p = cli.newDir("foo", true);
        Path settings = p.resolve("settings.xml");
        Files.createFile(settings);
        Path existingSettings = config.getSettings();
        Assert.assertNull(existingSettings);
        cli.execute("mvn set-settings-file " + settings.toFile().getAbsolutePath());
        Assert.assertEquals(settings, config.getSettings());
        Assert.assertEquals(settings, Configuration.parse().getMavenConfig().getSettings());

        cli.execute("mvn info");
        Assert.assertTrue(cli.getOutput().contains(settings.toFile().getAbsolutePath()));

        cli.execute("mvn set-settings-file");
        Assert.assertNull(config.getSettings());
        Assert.assertNull(Configuration.parse().getMavenConfig().getSettings());
    }

    @Test
    public void testUpdatePolicies() throws Exception {
        MavenConfig config = cli.getSession().getPmConfiguration().getMavenConfig();
        String defaultRelease = config.getDefaultReleasePolicy();
        String defaultSnapshot = config.getDefaultSnapshotPolicy();
        String snapshotPolicy = "interval:700";
        String releasePolicy = "never";
        try {
            cli.execute("mvn set-release-update-policy interval:");
            throw new Exception("Should have failed");
        } catch (CommandException ex) {
            // XXX OK
        }

        try {
            cli.execute("mvn set-snapshot-update-policy interval:");
            throw new Exception("Should have failed");
        } catch (CommandException ex) {
            // XXX OK
        }

        cli.execute("mvn set-snapshot-update-policy " + snapshotPolicy);
        cli.execute("mvn set-release-update-policy " + releasePolicy);

        cli.execute("mvn info");
        Assert.assertTrue(cli.getOutput().contains("snapshotUpdatePolicy=" + snapshotPolicy));
        Assert.assertTrue(cli.getOutput().contains("releaseUpdatePolicy=" + releasePolicy));

        Assert.assertEquals(snapshotPolicy, config.getDefaultSnapshotPolicy());
        Assert.assertEquals(releasePolicy, config.getDefaultReleasePolicy());

        Assert.assertEquals(snapshotPolicy, Configuration.parse().getMavenConfig().getDefaultSnapshotPolicy());
        Assert.assertEquals(releasePolicy, Configuration.parse().getMavenConfig().getDefaultReleasePolicy());

        cli.execute("mvn set-snapshot-update-policy");
        cli.execute("mvn set-release-update-policy");

        Assert.assertEquals(defaultSnapshot, config.getDefaultSnapshotPolicy());
        Assert.assertEquals(defaultRelease, config.getDefaultReleasePolicy());

        Assert.assertEquals(defaultSnapshot, Configuration.parse().getMavenConfig().getDefaultSnapshotPolicy());
        Assert.assertEquals(defaultRelease, Configuration.parse().getMavenConfig().getDefaultReleasePolicy());
    }


    private static void checkNoRepositories(MavenConfig config, String... names) throws Exception {
        for (String s : names) {
            Assert.assertFalse(config.getRemoteRepositoryNames().contains(s));
        }
    }

    private static void checkRepositories(MavenConfig config, String url, String name, String name2) throws Exception {
        MavenRemoteRepository repo = getRepository(config, name);
        Assert.assertNotNull(repo);
        Assert.assertNull(repo.getReleaseUpdatePolicy());
        Assert.assertNull(repo.getSnapshotUpdatePolicy());
        Assert.assertEquals("default", repo.getType());
        Assert.assertEquals(url, repo.getUrl());

        repo = getRepository(config, name2);
        Assert.assertNotNull(repo);
        Assert.assertEquals("always", repo.getSnapshotUpdatePolicy());
        Assert.assertEquals("never", repo.getReleaseUpdatePolicy());
        Assert.assertEquals(url, repo.getUrl());

    }

    private static MavenRemoteRepository getRepository(MavenConfig config, String name) {
        for (MavenRemoteRepository repo : config.getRemoteRepositories()) {
            if (repo.getName().equals(name)) {
                return repo;
            }
        }
        return null;
    }
}
