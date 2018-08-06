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

import java.util.Collection;

import org.jboss.galleon.universe.UniverseRepoTestBase;
import org.jboss.galleon.model.Gaecv;
import org.jboss.galleon.model.ResolvedGaecRange;
import org.jboss.galleon.universe.maven.MavenProducer;
import org.jboss.galleon.universe.maven.MavenChannel;
import org.jboss.galleon.universe.maven.MavenProducerInstaller;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Alexey Loubyansky
 */
public class MavenProducerExtendAndInstallTestCase extends UniverseRepoTestBase {

    private Gaecv producer100Artifact;
    private Gaecv producer101Artifact;

    @Override
    protected void doInit() {
        producer100Artifact = Gaecv.builder().groupId(GROUP_ID).artifactId("test-producer").version("1.0.0.Final")
        .build();
        producer101Artifact = Gaecv.builder().groupId(producer100Artifact.getGaec().getGroupId()).artifactId(producer100Artifact.getGaec().getArtifactId()).version("1.0.1.Final")
        .build();
    }

    @Test
    public void testMain() throws Exception {
        final String fpGroupId = "channel-group1";
        final String fpArtifactId = "channel-artifact1";

        MavenProducerInstaller producerInstaller = new MavenProducerInstaller("producer1", repo, ResolvedGaecRange.ofSingleGaecv(producer100Artifact), fpGroupId, fpArtifactId);
        producerInstaller.addFrequencies("alpha", "beta");
        producerInstaller.addChannel("1.0", "[1.0.0,2.0.0)");
        producerInstaller.addChannel("2.0", "[2.0.0,3.0.0)");
        producerInstaller.install();

        producerInstaller = new MavenProducerInstaller("producer1", repo, ResolvedGaecRange.ofSingleGaecv(producer101Artifact), ResolvedGaecRange.ofSingleGaecvp(repo.resolve(producer100Artifact)));
        producerInstaller.removeFrequency("beta");
        producerInstaller.addFrequency("cr");
        producerInstaller.addChannel("3.0", "[3.0.0,4.0.0)");
        producerInstaller.removeChannel("1.0");
        producerInstaller.install();

        MavenProducer producer = new MavenProducer("producer1", repo, ResolvedGaecRange.ofSingleGaecvp(repo.resolve(producer101Artifact)));
        Collection<String> frequencies = producer.getFrequencies();
        Assert.assertEquals(3, frequencies.size());
        Assert.assertTrue(frequencies.contains("alpha"));
        Assert.assertTrue(frequencies.contains("cr"));
        Assert.assertTrue(frequencies.contains("final"));

        MavenChannel channel = producer.getChannel("2.0");
        Assert.assertEquals(fpGroupId, channel.getFeaturePackGroupId());
        Assert.assertEquals(fpArtifactId, channel.getFeaturePackArtifactId());
        Assert.assertEquals("[2.0.0,3.0.0)", channel.getVersionRange());
        frequencies = channel.getFrequencies();
        Assert.assertEquals(3, frequencies.size());
        Assert.assertTrue(frequencies.contains("alpha"));
        Assert.assertTrue(frequencies.contains("cr"));
        Assert.assertTrue(frequencies.contains("final"));
        Assert.assertTrue(producer.hasChannel("2.0"));

        Assert.assertTrue(producer.hasChannel("3.0"));
        channel = producer.getChannel("3.0");
        Assert.assertEquals(fpGroupId, channel.getFeaturePackGroupId());
        Assert.assertEquals(fpArtifactId, channel.getFeaturePackArtifactId());
        Assert.assertEquals("[3.0.0,4.0.0)", channel.getVersionRange());
        frequencies = channel.getFrequencies();
        Assert.assertEquals(3, frequencies.size());
        Assert.assertTrue(frequencies.contains("alpha"));
        Assert.assertTrue(frequencies.contains("cr"));
        Assert.assertTrue(frequencies.contains("final"));

        Assert.assertFalse(producer.hasChannel("4.0"));

        Assert.assertEquals(2, producer.getChannels().size());
    }
}
