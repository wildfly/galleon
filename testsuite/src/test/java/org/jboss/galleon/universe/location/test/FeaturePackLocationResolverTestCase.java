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

package org.jboss.galleon.universe.location.test;

import static org.jboss.galleon.universe.TestConstants.GROUP_ID;

import java.nio.file.Path;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.ArtifactCoords.Gav;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.UniverseRepoTestBase;
import org.jboss.galleon.universe.UniverseResolver;
import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.model.GaecRange;
import org.jboss.galleon.model.Gaecv;
import org.jboss.galleon.model.ResolvedGaecRange;
import org.jboss.galleon.universe.maven.MavenErrors;
import org.jboss.galleon.universe.maven.MavenProducerInstaller;
import org.jboss.galleon.universe.maven.MavenUniverseFactory;
import org.jboss.galleon.universe.maven.MavenUniverseInstaller;
import org.jboss.galleon.util.IoUtils;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 * @author Alexey Loubyansky
 */
public class FeaturePackLocationResolverTestCase extends UniverseRepoTestBase {

    private static final String PRODUCER1_ARTIFACT_ID = "producer1-artifact";
    private static final String PRODUCER1_FP_ARTIFACT_ID = "producer1-feature-pack-artifact";
    private static final String FP_GROUP_ID = "test.group";

    private UniverseResolver resolver;
    private Gaecv universeArt;

    @Override
    public void doInit() throws Exception {

        final Gaecv artifact = Gaecv.builder().
                groupId(GROUP_ID).
                artifactId(PRODUCER1_ARTIFACT_ID).
                version("1.0.0.Final").build();
        MavenProducerInstaller producerInstaller = new MavenProducerInstaller("producer1", repo, ResolvedGaecRange.ofSingleGaecv(artifact), FP_GROUP_ID, PRODUCER1_FP_ARTIFACT_ID);
        producerInstaller.addChannel("5", "[5.0-alpha,6.0-alpha)");
        producerInstaller.addFrequencies("alpha", "beta");
        producerInstaller.install();

        universeArt = Gaecv.builder().
                groupId(GROUP_ID).
                artifactId("universe1-artifact").
                version("1.0.0.Final").build();
        final MavenUniverseInstaller universeInstaller = new MavenUniverseInstaller(repo, universeArt);
        universeInstaller.addProducer("producer1", GROUP_ID, PRODUCER1_ARTIFACT_ID, "[1.0.0,2.0.0)");
        universeInstaller.install();

        resolver = UniverseResolver.builder().addArtifactResolver(repo).build();
    }


    @Test
    public void testMain() throws Exception {

        try {
            resolver.resolve(FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + '(' + universeArt.toGaecRange() + "):5/alpha"));
            Assert.fail("Artifact does not exist");
        } catch(ProvisioningException e) {
            final GaecRange artifact = GaecRange.builder()
            .groupId(FP_GROUP_ID)
            .artifactId(PRODUCER1_FP_ARTIFACT_ID)
            .extension("zip")
            .versionRange("[5.0-alpha,6.0-alpha)")
            .build();
            Assert.assertEquals(MavenErrors.artifactNotFound(artifact, repoHome).getLocalizedMessage(), e.getLocalizedMessage());
        }

        installFp(ArtifactCoords.newGav(FP_GROUP_ID, PRODUCER1_FP_ARTIFACT_ID, "5.1.0.Alpha1"));

        try {
            resolver.resolve(FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + '(' + universeArt.toGaecRange() + "):5"));
            Assert.fail("No final releases yet");
        } catch(ProvisioningException e) {
            // ignore
        }

        Gaecv.Builder artifactBuider = Gaecv.builder().
                groupId(FP_GROUP_ID).
                artifactId(PRODUCER1_FP_ARTIFACT_ID).
                extension("zip").
                version("5.1.0.Alpha1");
        Path path = resolver.resolve(FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + '(' + universeArt.toGaecRange() + "):5/alpha"));
        Assert.assertEquals(artifactBuider.build().getArtifactFileName(), path.getFileName().toString());

        installFp(ArtifactCoords.newGav(FP_GROUP_ID, PRODUCER1_FP_ARTIFACT_ID, "4.1.0.Beta2"));
        path = resolver.resolve(FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + '(' + universeArt.toGaecRange() + "):5/alpha"));
        Assert.assertEquals(artifactBuider.build().getArtifactFileName(), path.getFileName().toString());

        installFp(ArtifactCoords.newGav(FP_GROUP_ID, PRODUCER1_FP_ARTIFACT_ID, "5.2.0.Final"));
        artifactBuider.version("5.2.0.Final");
        path = resolver.resolve(FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + '(' + universeArt.toGaecRange() + "):5/alpha"));
        Assert.assertEquals(artifactBuider.build().getArtifactFileName(), path.getFileName().toString());
        path = resolver.resolve(FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + '(' + universeArt.toGaecRange() + "):5"));
        Assert.assertEquals(artifactBuider.build().getArtifactFileName(), path.getFileName().toString());

        installFp(ArtifactCoords.newGav(FP_GROUP_ID, PRODUCER1_FP_ARTIFACT_ID, "5.2.1.Beta1"));
        artifactBuider.version("5.2.1.Beta1");
        path = resolver.resolve(FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + '(' + universeArt.toGaecRange() + "):5/alpha"));
        Assert.assertEquals(artifactBuider.build().getArtifactFileName(), path.getFileName().toString());
        path = resolver.resolve(FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + '(' + universeArt.toGaecRange() + "):5/beta"));
        Assert.assertEquals(artifactBuider.build().getArtifactFileName(), path.getFileName().toString());
        path = resolver.resolve(FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + '(' + universeArt.toGaecRange() + "):5"));
        artifactBuider.version("5.2.0.Final");
        Assert.assertEquals(artifactBuider.build().getArtifactFileName(), path.getFileName().toString());

        installFp(ArtifactCoords.newGav(FP_GROUP_ID, PRODUCER1_FP_ARTIFACT_ID, "6.0.0.Alpha1"));
        path = resolver.resolve(FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + '(' + universeArt.toGaecRange() + "):5"));
        Assert.assertEquals(artifactBuider.build().getArtifactFileName(), path.getFileName().toString());
        path = resolver.resolve(FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + '(' + universeArt.toGaecRange() + "):5/alpha"));
        artifactBuider.version("5.2.1.Beta1");
        Assert.assertEquals(artifactBuider.build().getArtifactFileName(), path.getFileName().toString());

        installFp(ArtifactCoords.newGav(FP_GROUP_ID, PRODUCER1_FP_ARTIFACT_ID, "6.0.0.Final"));
        path = resolver.resolve(FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + '(' + universeArt.toGaecRange() + "):5"));
        artifactBuider.version("5.2.0.Final");
        Assert.assertEquals(artifactBuider.build().getArtifactFileName(), path.getFileName().toString());

        artifactBuider.version("5.1.0.Alpha1");
        path = resolver.resolve(FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + '(' + universeArt.toGaecRange() + "):5#5.1.0.Alpha1"));
        Assert.assertEquals(artifactBuider.build().getArtifactFileName(), path.getFileName().toString());

        artifactBuider.version("4.1.0.Beta2");
        path = resolver.resolve(FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + '(' + universeArt.toGaecRange() + "):5#4.1.0.Beta2"));
        Assert.assertEquals(artifactBuider.build().getArtifactFileName(), path.getFileName().toString());
    }

    @Test
    public void testResolutionLocalPathInstalled() throws Exception {
        FeaturePackLocation fpl = FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + '(' + universeArt.toGaecRange() + "):5#5.0.0.Final");

        Gaecv artifact = Gaecv.builder()
                .groupId(FP_GROUP_ID)
                .artifactId(PRODUCER1_FP_ARTIFACT_ID)
                .extension("zip")
                .version("5.0.0.Final").build();

        FeaturePackCreator.getInstance()
                .addArtifactResolver(repo)
                .newFeaturePack()
                .setFPID(fpl.getFPID())
                .getCreator()
                .install();

        Path path = resolver.resolve(fpl);
        Assert.assertEquals(artifact.getArtifactFileName(), path.getFileName().toString());

        Path externalPath = repoHome.resolve("external").resolve(path.getFileName().toString());
        IoUtils.copy(path, externalPath);
        IoUtils.recursiveDelete(path);

        try {
            resolver.resolve(fpl);
            Assert.fail(String.format("The %s artifact is still installed in the local repository", artifact));
        } catch (ProvisioningException e) {
            // Expected exception
        }

        UniverseResolver resolver = UniverseResolver.builder()
                .addArtifactResolver(repo)
                .addLocalFeaturePack(externalPath)
                .build();

        try {
            path = resolver.resolve(fpl);
            Assert.assertEquals(artifact.getArtifactFileName(), path.getFileName().toString());
        } catch (ProvisioningException e) {
            Assert.fail(String.format("Cannot resolve %s artifact using a resolved with a local feature pack location", artifact));
        }
    }

    private void installFp(Gav fpGav) throws ProvisioningException {
        FeaturePackCreator.getInstance()
        .addArtifactResolver(repo)
        .newFeaturePack(LegacyGalleon1Universe.toFpl(fpGav).getFPID())
        .getCreator()
        .install();
    }
}
