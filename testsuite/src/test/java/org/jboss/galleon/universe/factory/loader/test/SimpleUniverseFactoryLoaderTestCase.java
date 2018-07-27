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

package org.jboss.galleon.universe.factory.loader.test;

import static org.jboss.galleon.universe.TestConstants.GROUP_ID;

import org.jboss.galleon.universe.Universe;
import org.jboss.galleon.universe.UniverseFactoryLoader;
import org.jboss.galleon.universe.UniverseRepoTestBase;
import org.jboss.galleon.model.GaecRange;
import org.jboss.galleon.model.Gaecv;
import org.jboss.galleon.model.ResolvedGaecRange;
import org.jboss.galleon.universe.maven.MavenProducerInstaller;
import org.jboss.galleon.universe.maven.MavenUniverseFactory;
import org.jboss.galleon.universe.maven.MavenUniverseInstaller;
import org.jboss.galleon.universe.maven.repo.SimplisticMavenRepoManager;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Alexey Loubyansky
 */
public class SimpleUniverseFactoryLoaderTestCase extends UniverseRepoTestBase {

    private static final String UNIVERSE_ARTIFACT_ID = "universe-artifact1";
    private static final String FP_ARTIFACT_ID = "test-producer1-artifact";
    private static final String FP_GROUP_ID = "test.group";

    @Override
    public void doInit() throws Exception {
        {
            final Gaecv artifact = Gaecv.builder().groupId(GROUP_ID).artifactId("producer-artifact1").version("1.0.0.Final")
                    .build();
            final MavenProducerInstaller producerInstaller = new MavenProducerInstaller("producer1", repo, ResolvedGaecRange.ofSingleGaecv(artifact), FP_GROUP_ID, FP_ARTIFACT_ID);
            producerInstaller.addChannel("5", "[5.0,6.0)");
            producerInstaller.addFrequencies("alpha", "beta");
            producerInstaller.install();
        }
        {
            final Gaecv artifact = Gaecv.builder().groupId(GROUP_ID).artifactId(UNIVERSE_ARTIFACT_ID).version("1.0.0.Final")
                    .build();
            final MavenUniverseInstaller universeInstaller = new MavenUniverseInstaller(repo, artifact);
            universeInstaller.addProducer("producer1", GROUP_ID, "producer-artifact1", "[1.0.0,2.0.0)");
            universeInstaller.install();
        }
    }

    @Test
    public void testActualResolver() throws Exception {

        final Universe<?> universe = UniverseFactoryLoader.getInstance()
                .getUniverse(MavenUniverseFactory.ID,
                        GaecRange.builder().groupId(GROUP_ID).artifactId(UNIVERSE_ARTIFACT_ID).versionRange("[1.0,)").build(),
                        SimplisticMavenRepoManager.getInstance(repoHome));
        Assert.assertNotNull(universe);
        Assert.assertTrue(universe.hasProducer("producer1"));
        Assert.assertTrue(universe.getProducer("producer1").hasChannel("5"));
    }

    @Test
    public void testRepoId() throws Exception {

        final Universe<?> universe = UniverseFactoryLoader.getInstance()
                .addArtifactResolver(SimplisticMavenRepoManager.getInstance(repoHome))
                .getUniverse(MavenUniverseFactory.ID, GaecRange.builder().groupId(GROUP_ID).artifactId(UNIVERSE_ARTIFACT_ID).versionRange("[1.0,)").build());
        Assert.assertNotNull(universe);
        Assert.assertTrue(universe.hasProducer("producer1"));
        Assert.assertTrue(universe.getProducer("producer1").hasChannel("5"));
    }
}
