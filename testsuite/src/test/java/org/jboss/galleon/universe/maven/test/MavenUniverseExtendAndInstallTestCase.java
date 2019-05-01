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
package org.jboss.galleon.universe.maven.test;

import static org.jboss.galleon.universe.TestConstants.*;

import org.jboss.galleon.universe.UniverseRepoTestBase;
import org.jboss.galleon.universe.maven.MavenArtifact;
import org.jboss.galleon.universe.maven.MavenProducer;
import org.jboss.galleon.universe.maven.MavenProducerInstaller;
import org.jboss.galleon.universe.maven.MavenUniverse;
import org.jboss.galleon.universe.maven.MavenUniverseInstaller;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Alexey Loubyansky
 */
public class MavenUniverseExtendAndInstallTestCase extends UniverseRepoTestBase {

    private MavenArtifact universe100Artifact;
    private MavenArtifact universe101Artifact;

    @Override
    protected void doInit() throws Exception {
        universe100Artifact = new MavenArtifact();
        universe100Artifact.setGroupId(GROUP_ID);
        universe100Artifact.setArtifactId("test-universe");
        universe100Artifact.setVersion("1.0.0.Final");
        universe101Artifact = new MavenArtifact();
        universe101Artifact.setGroupId(universe100Artifact.getGroupId());
        universe101Artifact.setArtifactId(universe100Artifact.getArtifactId());
        universe101Artifact.setVersion("1.0.1.Final");

        MavenArtifact artifact = new MavenArtifact();
        artifact.setGroupId(GROUP_ID);
        artifact.setArtifactId("producer1");
        artifact.setVersion("1.0.0.Final");
        MavenProducerInstaller producerInstaller = new MavenProducerInstaller("producer1", repo, artifact);
        producerInstaller.addFrequencies("alpha", "beta");
        producerInstaller.install();

        artifact = new MavenArtifact();
        artifact.setGroupId(GROUP_ID);
        artifact.setArtifactId("producer2");
        artifact.setVersion("3.0.0.Final");
        producerInstaller = new MavenProducerInstaller("producer2", repo, artifact);
        producerInstaller.addFrequencies("alpha", "beta");
        producerInstaller.install();

        artifact = new MavenArtifact();
        artifact.setGroupId(GROUP_ID);
        artifact.setArtifactId("producer3");
        artifact.setVersion("2.5.1.Final");
        producerInstaller = new MavenProducerInstaller("producer3", repo, artifact);
        producerInstaller.addFrequencies("alpha", "beta");
        producerInstaller.install();

    }

    @Test
    public void testMain() throws Exception {
        MavenUniverseInstaller universeInstaller = new MavenUniverseInstaller(repo, universe100Artifact);
        universeInstaller.addProducer("producer1", GROUP_ID, "producer1", "[1.0.0,2.0.0)");
        universeInstaller.addProducer("producer2", GROUP_ID, "producer2", "[3.0.0,4.0.0)");
        universeInstaller.install();

        universeInstaller = new MavenUniverseInstaller(repo, universe101Artifact);
        universeInstaller.extendUniverse(universe100Artifact);
        universeInstaller.addProducer("producer3", GROUP_ID, "producer3", "[2.0.0,3.0.0)");
        universeInstaller.removeProducer("producer2");
        universeInstaller.install();

        universe101Artifact.setPath(null);
        final MavenUniverse universe = new MavenUniverse(repo, universe101Artifact);

        Assert.assertTrue(universe.hasProducer("producer1"));
        MavenProducer producer = universe.getProducer("producer1");
        Assert.assertEquals(GROUP_ID, producer.getArtifact().getGroupId());
        Assert.assertEquals("producer1", producer.getArtifact().getArtifactId());
        Assert.assertEquals("[1.0.0,2.0.0)", producer.getArtifact().getVersionRange());

        Assert.assertFalse(universe.hasProducer("producer2"));

        Assert.assertEquals(2, universe.getProducers().size());

        Assert.assertTrue(universe.hasProducer("producer3"));
        producer = universe.getProducer("producer3");
        Assert.assertEquals(GROUP_ID, producer.getArtifact().getGroupId());
        Assert.assertEquals("producer3", producer.getArtifact().getArtifactId());
        Assert.assertEquals("[2.0.0,3.0.0)", producer.getArtifact().getVersionRange());
    }
}
