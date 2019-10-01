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
package org.jboss.galleon.creator.maven.test;

import java.nio.file.Path;

import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.UniverseRepoTestBase;
import org.jboss.galleon.universe.UniverseResolver;
import org.jboss.galleon.universe.maven.MavenArtifact;
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
public class MavenUniverseFeaturePackCreatorTestCase extends UniverseRepoTestBase {

    @Override
    protected void doInit() throws Exception {

        final MavenProducerInstaller producer1 = new MavenProducerInstaller("producer1", repo,
                new MavenArtifact().setGroupId("universe.producer.maven.test").setArtifactId("maven-producer1").setVersion("1.0.0.Final"),
                "universe.feature-pack.maven.test", "feature-pack1")
                .addFrequencies("alpha", "beta")
                .addChannel("1.0", "[1.0.0,2.0.0)")
                .install();

        new MavenUniverseInstaller(repo,
                new MavenArtifact().setGroupId("universe.maven.test").setArtifactId("maven-universe1").setVersion("1.0.0.Final"))
                .addProducer(producer1.getName(), producer1.getArtifact().setPath(null).setVersionRange("[1.0,2.0-alpha)"))
                .install();

        FeaturePackCreator.getInstance()
        .addArtifactResolver(repo)
        .newFeaturePack()
            .setFPID(FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + "(universe.maven.test:maven-universe1:[1.0,2.0-alpha)):1.0#1.0.0.Final").getFPID())
            .newPackage("p1", true)
                .writeContent("p1.txt", "p1 text")
                .getFeaturePack()
            .getCreator()
        .install();
    }

    @Test
    public void testMain() throws Exception {
        final UniverseResolver universeResolver = UniverseResolver.builder()
                .addArtifactResolver(SimplisticMavenRepoManager.getInstance(repoHome))
                .build();
        Path path = universeResolver.resolve(FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + "(universe.maven.test:maven-universe1:[1.0,2.0-alpha)):1.0"));
        Assert.assertEquals("feature-pack1-1.0.0.Final.zip", path.getFileName().toString());
    }
}
