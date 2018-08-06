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

import static org.jboss.galleon.universe.TestConstants.GROUP_ID;

import java.nio.file.Path;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.UniverseRepoTestBase;
import org.jboss.galleon.universe.UniverseResolver;
import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.model.Gaec;
import org.jboss.galleon.model.GaecRange;
import org.jboss.galleon.model.Gaecv;
import org.jboss.galleon.model.ResolvedGaecRange;
import org.jboss.galleon.universe.maven.MavenErrors;
import org.jboss.galleon.universe.maven.MavenProducerInstaller;
import org.jboss.galleon.universe.maven.MavenUniverseException;
import org.jboss.galleon.universe.maven.MavenUniverseFactory;
import org.jboss.galleon.universe.maven.MavenUniverseInstaller;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 * @author Alexey Loubyansky
 */
public class MavenUniverseLocationTestCase extends UniverseRepoTestBase {

    private static final String PRODUCER1_ARTIFACT_ID = "producer1-artifact";
    private static final String PRODUCER2_ARTIFACT_ID = "producer2-artifact";
    private static final String PRODUCER1_FP_ARTIFACT_ID = "producer1-feature-pack-artifact";
    private static final String FP_GROUP_ID = "test.group";

    private UniverseResolver resolver;
    private Gaecv fpArt;

    @Override
    public void doInit() throws Exception {

        {
            final Gaecv artifact = Gaecv.builder().groupId(GROUP_ID).artifactId(PRODUCER1_ARTIFACT_ID).version("1.0.0.Final")
                    .build();
            final MavenProducerInstaller producerInstaller = new MavenProducerInstaller("producer1", repo, ResolvedGaecRange.ofSingleGaecv(artifact), FP_GROUP_ID, PRODUCER1_FP_ARTIFACT_ID);
            producerInstaller.addChannel("5", "[5.0-alpha,6.0-alpha)");
            producerInstaller.addFrequencies("alpha", "beta");
            producerInstaller.install();
        }

        {
            final Gaecv artifact = Gaecv.builder().groupId(GROUP_ID).artifactId(PRODUCER2_ARTIFACT_ID).version("1.0.0.Final")
                    .build();
            final MavenProducerInstaller producerInstaller = new MavenProducerInstaller("producer2", repo, ResolvedGaecRange.ofSingleGaecv(artifact), FP_GROUP_ID, PRODUCER1_FP_ARTIFACT_ID);
            producerInstaller.addChannel("5", "[5.0-alpha,6.0-alpha)");
            producerInstaller.addFrequencies("alpha", "beta");
            producerInstaller.install();
        }

        {
            final Gaecv universeArt = Gaecv.builder().groupId(GROUP_ID).artifactId("universe1-artifact").version("1.0.0.Final")
                    .build();
            final MavenUniverseInstaller universeInstaller = new MavenUniverseInstaller(repo, universeArt);
            universeInstaller.addProducer("producer1", GROUP_ID, PRODUCER1_ARTIFACT_ID, "[1.0.0,2.0.0)");
            universeInstaller.install();
        }

        {
            final Gaecv universeArt = Gaecv.builder().groupId(GROUP_ID).artifactId("universe1-artifact").version("1.0.1.Final")
                    .build();
            final MavenUniverseInstaller universeInstaller = new MavenUniverseInstaller(repo, universeArt);
            universeInstaller.addProducer("producer1", GROUP_ID, PRODUCER1_ARTIFACT_ID, "[1.0.0,2.0.0)");
            universeInstaller.addProducer("producer2", GROUP_ID, "producer2-artifact", "[1.0.0,2.0.0)");
            universeInstaller.install();
        }

        resolver = UniverseResolver.builder().addArtifactResolver(repo).build();

        fpArt = Gaecv.builder().groupId(FP_GROUP_ID).artifactId(PRODUCER1_FP_ARTIFACT_ID).extension("zip").version("5.1.0.Alpha1")
                .build();
        final Gaec gaec = fpArt.getGaec();
        FeaturePackCreator.getInstance()
        .addArtifactResolver(repo)
        .newFeaturePack(LegacyGalleon1Universe.toFpl(ArtifactCoords.newGav(gaec.getGroupId(), gaec.getArtifactId(), fpArt.getVersion())).getFPID())
        .getCreator()
        .install();
    }


    @Test
    public void testUniverseVersion() throws Exception {

        final Gaecv.Builder universeArtBuilder = Gaecv.builder().groupId(GROUP_ID).artifactId("universe1-artifact").version("1.0.0.Final");
        {
            final Gaecv universeArt = universeArtBuilder.build();
            final Path path = resolver.resolve(FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + '(' + universeArt.toGaecRange() + "):5/alpha"));
            Assert.assertEquals(fpArt.getArtifactFileName(), path.getFileName().toString());
            try {
                resolver.resolve(FeaturePackLocation
                        .fromString("producer2@" + MavenUniverseFactory.ID + '(' + universeArt.toGaecRange() + "):5/alpha"));
                Assert.fail("producer2 is not in this universe version");
            } catch(MavenUniverseException e) {
                Assert.assertEquals(MavenErrors.producerNotFound("producer2").getLocalizedMessage(), e.getLocalizedMessage());
            }
        }

        {
            universeArtBuilder.version("1.0.1.Final");
            final Gaecv universeArt = universeArtBuilder.build();
            final Path path = resolver.resolve(FeaturePackLocation.fromString("producer2@" + MavenUniverseFactory.ID + '(' + universeArt.toGaecRange() + "):5/alpha"));
            Assert.assertEquals(fpArt.getArtifactFileName(), path.getFileName().toString());
        }

    }

    @Test
    public void testUniverseVersionRange() throws Exception {

        final GaecRange universeArt = GaecRange.builder()
                .groupId(GROUP_ID)
                .artifactId("universe1-artifact")
                .versionRange("[1.0,2.0-alpha)")
                .build();

        {
            final Path path = resolver.resolve(FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + '(' + universeArt + "):5/alpha"));
            Assert.assertEquals(fpArt.getArtifactFileName(), path.getFileName().toString());
        }
        {
            final Path path = resolver.resolve(FeaturePackLocation.fromString("producer2@" + MavenUniverseFactory.ID + '(' + universeArt + "):5/alpha"));
            Assert.assertEquals(fpArt.getArtifactFileName(), path.getFileName().toString());
        }
    }

    @Test
    public void testUniverseWithoutVersion() throws Exception {

        final GaecRange universeArt = GaecRange.builder()
                .groupId(GROUP_ID)
                .artifactId("universe1-artifact")
                .build();

        {
            final Path path = resolver.resolve(FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + '(' + universeArt + "):5/alpha"));
            Assert.assertEquals(fpArt.getArtifactFileName(), path.getFileName().toString());
        }
        {
            final Path path = resolver.resolve(FeaturePackLocation.fromString("producer2@" + MavenUniverseFactory.ID + '(' + universeArt + "):5/alpha"));
            Assert.assertEquals(fpArt.getArtifactFileName(), path.getFileName().toString());
        }
    }
}
