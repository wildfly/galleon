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

package org.jboss.galleon.universe.maven.test;

import static org.jboss.galleon.universe.TestConstants.*;

import org.jboss.galleon.universe.UniverseRepoTestBase;
import org.jboss.galleon.model.Gaec;
import org.jboss.galleon.model.Gaecv;
import org.jboss.galleon.model.Gaecvp;
import org.jboss.galleon.model.ResolvedGaecRange;
import org.jboss.galleon.universe.maven.MavenProducer;
import org.jboss.galleon.universe.maven.MavenProducerInstaller;
import org.jboss.galleon.universe.maven.MavenErrors;
import org.jboss.galleon.universe.maven.MavenUniverse;
import org.jboss.galleon.universe.maven.MavenUniverseInstaller;
import org.jboss.galleon.universe.maven.MavenUniverseException;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Alexey Loubyansky
 */
public class MavenUniverseInstallTestCase extends UniverseRepoTestBase {

    private Gaecv universeArtifact;

    @Override
    protected void doInit() throws Exception {
        universeArtifact = Gaecv.builder()
        .groupId(GROUP_ID)
        .artifactId("test-universe")
        .version("1.0.0.Final")
        .build();

        Gaecv artifact = Gaecv.builder()
        .groupId(GROUP_ID)
        .artifactId("producer1")
        .version("1.0.0.Final")
        .build();
        MavenProducerInstaller producerInstaller = new MavenProducerInstaller("producer1", repo, ResolvedGaecRange.ofSingleGaecv(artifact));
        producerInstaller.addFrequencies("alpha", "beta");
        producerInstaller.install();

        artifact = Gaecv.builder().groupId(GROUP_ID).artifactId("producer2").version("3.0.0.Final")
        .build();
        producerInstaller = new MavenProducerInstaller("producer2", repo, ResolvedGaecRange.ofSingleGaecv(artifact));
        producerInstaller.addFrequencies("alpha", "beta");
        producerInstaller.install();
    }

    @Test
    public void testMain() throws Exception {

        final MavenUniverseInstaller universeInstaller = new MavenUniverseInstaller(repo, universeArtifact);
        universeInstaller.addProducer("producer1", GROUP_ID, "producer1", "[1.0.0,2.0.0)");
        universeInstaller.addProducer("producer2", GROUP_ID, "producer2", "[3.0.0,4.0.0)");
        Gaecvp gaecvp = universeInstaller.install();

        final MavenUniverse universe = new MavenUniverse(repo, gaecvp);

        Assert.assertTrue(universe.hasProducer("producer1"));
        {
            final MavenProducer producer = universe.getProducer("producer1");
            final Gaec gaec = producer.getArtifact().getResolved().getGaecv().getGaec();
            Assert.assertEquals(GROUP_ID, gaec.getGroupId());
            Assert.assertEquals("producer1", gaec.getArtifactId());
            Assert.assertEquals("[1.0.0,2.0.0)", producer.getArtifact().getGaecRange().getVersionRange());
        }
        {
            final MavenProducer producer = universe.getProducer("producer2");
            final Gaec gaec = producer.getArtifact().getResolved().getGaecv().getGaec();
            Assert.assertEquals(GROUP_ID, gaec.getGroupId());
            Assert.assertEquals("producer2", gaec.getArtifactId());
            Assert.assertEquals("[3.0.0,4.0.0)", producer.getArtifact().getGaecRange().getVersionRange());
        }

        Assert.assertTrue(universe.hasProducer("producer2"));

        Assert.assertFalse(universe.hasProducer("producerN"));

        try {
            universe.getProducer("producerN");
        } catch(MavenUniverseException e) {
            Assert.assertEquals(MavenErrors.producerNotFound("producerN").getLocalizedMessage(), e.getLocalizedMessage());
        }

        Assert.assertEquals(2, universe.getProducers().size());
    }
}
