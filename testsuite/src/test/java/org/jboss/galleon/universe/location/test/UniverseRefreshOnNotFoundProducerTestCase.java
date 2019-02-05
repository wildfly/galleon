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

package org.jboss.galleon.universe.location.test;

import static org.jboss.galleon.universe.TestConstants.GROUP_ID;
import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.repo.RepositoryArtifactResolver;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.Producer;
import org.jboss.galleon.universe.UniverseRepoTestBase;
import org.jboss.galleon.universe.UniverseResolver;
import org.jboss.galleon.universe.UniverseSpec;
import org.jboss.galleon.universe.maven.MavenArtifact;
import org.jboss.galleon.universe.maven.MavenProducerInstaller;
import org.jboss.galleon.universe.maven.MavenUniverseException;
import org.jboss.galleon.universe.maven.MavenUniverseFactory;
import org.jboss.galleon.universe.maven.MavenUniverseInstaller;
import org.jboss.galleon.universe.maven.repo.SimplisticMavenRepoManager;
import org.junit.Test;

/**
 * This test makes sure that the local universe artifact is picked up first.
 * If the requested producer is not found in it then the latest version of the universe
 * is pulled in from the remote repositories. And if the latest universe version does not
 * contain the producer then the resolution fails.
 *
 * @author Alexey Loubyansky
 */
public class UniverseRefreshOnNotFoundProducerTestCase extends UniverseRepoTestBase {

    private static final String PRODUCER1 = "producer1";
    private static final String PRODUCER2 = "producer2";
    private static final String PRODUCER3 = "producer3";
    private static final String PRODUCER4 = "producer4";

    private UniverseResolver resolver;
    private MavenArtifact universeArt;
    private SimplisticMavenRepoManager fakeRemoteRepo;

    @Override
    protected SimplisticMavenRepoManager initResolver(Path repoHome) {

        fakeRemoteRepo = SimplisticMavenRepoManager.getInstance(mkdirs("fake-remote-repo"));

        final SimplisticMavenRepoManager repo = SimplisticMavenRepoManager.getInstance(repoHome, fakeRemoteRepo);
        repo.setLocallyAvailableVersionRangesPreferred(false);
        return repo;
    }

    @Override
    public void doInit() throws Exception {

        universeArt = new MavenArtifact()
                .setGroupId(GROUP_ID)
                .setArtifactId("universe1-artifact")
                .setVersionRange("[0,)");

        setupUniverse(repo, PRODUCER1, PRODUCER4);
        setupUniverse(fakeRemoteRepo, PRODUCER2, PRODUCER3, PRODUCER4);

        resolver = UniverseResolver.builder().addArtifactResolver(repo).build();

        // it's important to init the fake one first due to how FeaturePackCreator works with UniverseFactoryLoader
        installFp(fakeRemoteRepo, PRODUCER2, "1", "1.0.0.Final");
        installFp(repo, PRODUCER1, "1", "1.0.0.Final");
    }

    private void setupUniverse(SimplisticMavenRepoManager repo, String... producers) throws MavenUniverseException {
        final MavenUniverseInstaller universeInstaller = new MavenUniverseInstaller(repo,
                new MavenArtifact()
                .setGroupId(GROUP_ID)
                .setArtifactId("universe1-artifact")
                .setVersion("1"));

        for(String producer : producers) {
            final MavenArtifact producerArtifact = new MavenArtifact().setGroupId(GROUP_ID).setArtifactId(producer).setVersion("1");
            final String producerFpArtifactId = producer + "-fp";
            MavenProducerInstaller producerInstaller = new MavenProducerInstaller(producer, repo, producerArtifact, GROUP_ID, producerFpArtifactId);
            producerInstaller.addChannel("1", "[1.0-alpha,2.0-alpha)");
            producerInstaller.addFrequencies("alpha", "beta");
            producerInstaller.install();
            universeInstaller.addProducer(producer, GROUP_ID, producer, "[0,)");
        }
        universeInstaller.install();
    }

    @Test
    public void testMain() throws Exception {
        resolver.resolve(getFpl(PRODUCER1, "1"));
        assertUniverseProducers(PRODUCER1, PRODUCER4);

        // trigger universe reload from the remote repo
        resolver.resolve(getFpl(PRODUCER2, "1"));
        assertUniverseProducers(PRODUCER2, PRODUCER3, PRODUCER4);
    }

    @SuppressWarnings("unchecked")
    private void assertUniverseProducers(String... expected) throws ProvisioningException {
        final Collection<Producer<?>> producers = (Collection<Producer<?>>) resolver.getUniverse(getUniverseSpec()).getProducers();
        final Set<String> actual = new HashSet<>(producers.size());
        for(Producer<?> producer : producers) {
            actual.add(producer.getName());
        }
        assertEquals(new HashSet<>(Arrays.asList(expected)), actual);
    }

    private void installFp(RepositoryArtifactResolver repo, String producer, String channel, String version) throws ProvisioningException {
        FeaturePackCreator.getInstance()
        .addArtifactResolver(repo)
        .newFeaturePack(getFpl(producer, channel, version).getFPID())
        .getCreator()
        .install();
    }

    private FeaturePackLocation getFpl(String producer, String channel) {
        return getFpl(producer, channel, null);
    }

    private FeaturePackLocation getFpl(String producer, String channel, String version) {
        final StringBuilder buf = new StringBuilder();
        buf.append(producer).append('@').append(MavenUniverseFactory.ID).append('(').append(universeArt.getCoordsAsString()).append(')').append(':').append(channel);
        if(version != null) {
            buf.append('#').append(version);
        }
        return FeaturePackLocation.fromString(buf.toString());
    }

    private UniverseSpec getUniverseSpec() {
        return new UniverseSpec(MavenUniverseFactory.ID, universeArt.getCoordsAsString());
    }
}
