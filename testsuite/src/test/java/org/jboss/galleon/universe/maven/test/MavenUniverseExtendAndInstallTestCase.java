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
import org.jboss.galleon.model.Gaecv;
import org.jboss.galleon.model.ResolvedGaecRange;
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

    private Gaecv universe100Artifact;
    private Gaecv universe101Artifact;

    @Override
    protected void doInit() throws Exception {
        universe100Artifact = Gaecv.builder().groupId(GROUP_ID).artifactId("test-universe").version("1.0.0.Final")
        .build();
        universe101Artifact = Gaecv.builder().groupId(universe100Artifact.getGaec().getGroupId()).artifactId(universe100Artifact.getGaec().getArtifactId()).version("1.0.1.Final")
        .build();

        {
            final Gaecv artifact = Gaecv.builder().groupId(GROUP_ID).artifactId("producer1").version("1.0.0.Final")
                    .build();
                    final MavenProducerInstaller producerInstaller = new MavenProducerInstaller("producer1", repo, ResolvedGaecRange.ofSingleGaecv(artifact));
                    producerInstaller.addFrequencies("alpha", "beta");
                    producerInstaller.install();
        }

        {
            final Gaecv artifact = Gaecv.builder().groupId(GROUP_ID).artifactId("producer2").version("3.0.0.Final")
                    .build();
            final MavenProducerInstaller producerInstaller = new MavenProducerInstaller("producer2", repo, ResolvedGaecRange.ofSingleGaecv(artifact));
                    producerInstaller.addFrequencies("alpha", "beta");
                    producerInstaller.install();
        }
        {
            final Gaecv artifact = Gaecv.builder().groupId(GROUP_ID).artifactId("producer3").version("2.5.1.Final")
                    .build();
            final MavenProducerInstaller producerInstaller = new MavenProducerInstaller("producer3", repo, ResolvedGaecRange.ofSingleGaecv(artifact));
                    producerInstaller.addFrequencies("alpha", "beta");
                    producerInstaller.install();
        }

    }

    @Test
    public void testMain() throws Exception {
        {
            final MavenUniverseInstaller universeInstaller = new MavenUniverseInstaller(repo, universe100Artifact);
            universeInstaller.addProducer("producer1", GROUP_ID, "producer1", "[1.0.0,2.0.0)");
            universeInstaller.addProducer("producer2", GROUP_ID, "producer2", "[3.0.0,4.0.0)");
            universeInstaller.install();
        }

        {
            final MavenUniverseInstaller universeInstaller = new MavenUniverseInstaller(repo, universe101Artifact);
            universeInstaller.extendUniverse(repo.resolve(universe100Artifact));
            universeInstaller.addProducer("producer3", GROUP_ID, "producer3", "[2.0.0,3.0.0)");
            universeInstaller.removeProducer("producer2");
            universeInstaller.install();
        }

        final MavenUniverse universe = new MavenUniverse(repo, repo.resolve(universe101Artifact));

        Assert.assertTrue(universe.hasProducer("producer1"));
        {
            final MavenProducer producer = universe.getProducer("producer1");
            Assert.assertEquals(GROUP_ID, producer.getArtifact().getResolved().getGaecv().getGaec().getGroupId());
            Assert.assertEquals("producer1", producer.getArtifact().getResolved().getGaecv().getGaec().getArtifactId());
            Assert.assertEquals("[1.0.0,2.0.0)", producer.getArtifact().getGaecRange().getVersionRange());
        }

        Assert.assertFalse(universe.hasProducer("producer2"));

        Assert.assertEquals(2, universe.getProducers().size());

        Assert.assertTrue(universe.hasProducer("producer3"));
        {
            final MavenProducer producer = universe.getProducer("producer3");
            Assert.assertEquals(GROUP_ID, producer.getArtifact().getResolved().getGaecv().getGaec().getGroupId());
            Assert.assertEquals("producer3", producer.getArtifact().getResolved().getGaecv().getGaec().getArtifactId());
            Assert.assertEquals("[2.0.0,3.0.0)", producer.getArtifact().getGaecRange().getVersionRange());
        }
    }
}
