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
package org.jboss.galleon.featurepack.location.test;

import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.ChannelSpec;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;
import org.jboss.galleon.universe.UniverseSpec;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeaturePackLocationStringTestCase {

    @Test
    public void testProducerFromString() throws Exception {
        final FeaturePackLocation parsedSrc = FeaturePackLocation.fromString("producer");
        Assert.assertNotNull(parsedSrc);
        Assert.assertNull(parsedSrc.getUniverse());
        Assert.assertEquals("producer", parsedSrc.getProducerName());
        Assert.assertNull(parsedSrc.getChannelName());
        Assert.assertNull(parsedSrc.getFrequency());
        Assert.assertNull(parsedSrc.getBuild());
    }

    @Test
    public void testProducerToString() throws Exception {
        Assert.assertEquals("producer",
                new FeaturePackLocation(null, "producer", null, null, null)
        .toString());
    }

    @Test
    public void testProducerBuildFromString() throws Exception {
        final FeaturePackLocation parsedSrc = FeaturePackLocation.fromString("producer#build");
        Assert.assertNotNull(parsedSrc);
        Assert.assertNull(parsedSrc.getUniverse());
        Assert.assertEquals("producer", parsedSrc.getProducerName());
        Assert.assertNull(parsedSrc.getChannelName());
        Assert.assertNull(parsedSrc.getFrequency());
        Assert.assertEquals("build", parsedSrc.getBuild());
    }

    @Test
    public void testProducerBuildToString() throws Exception {
        Assert.assertEquals("producer#build",
                new FeaturePackLocation(null, "producer", null, null, "build")
        .toString());
    }

    @Test
    public void testProducerFrequencyBuildToString() throws Exception {
        Assert.assertEquals("producer/frequency#build",
                new FeaturePackLocation(null, "producer", null, "frequency", "build")
        .toString());
    }

    @Test
    public void testProducerFrequencyBuildFromString() throws Exception {
        final FeaturePackLocation parsedSrc = FeaturePackLocation.fromString("producer/frequency#build");
        Assert.assertNotNull(parsedSrc);
        Assert.assertNull(parsedSrc.getUniverse());
        Assert.assertEquals("producer", parsedSrc.getProducerName());
        Assert.assertNull(parsedSrc.getChannelName());
        Assert.assertEquals("frequency", parsedSrc.getFrequency());
        Assert.assertEquals("build", parsedSrc.getBuild());
    }

    @Test
    public void testProducerFrequencyToString() throws Exception {
        Assert.assertEquals("producer/frequency",
                new FeaturePackLocation(null, "producer", null, "frequency", null)
        .toString());
    }

    @Test
    public void testProducerFrequencyFromString() throws Exception {
        final FeaturePackLocation parsedSrc = FeaturePackLocation.fromString("producer/frequency");
        Assert.assertNotNull(parsedSrc);
        Assert.assertNull(parsedSrc.getUniverse());
        Assert.assertEquals("producer", parsedSrc.getProducerName());
        Assert.assertNull(parsedSrc.getChannelName());
        Assert.assertEquals("frequency", parsedSrc.getFrequency());
        Assert.assertNull(null, parsedSrc.getBuild());
    }

    @Test
    public void testProducerUniverseFactoryToString() throws Exception {
        Assert.assertEquals("producer@factory",
                new FeaturePackLocation(new UniverseSpec("factory"), "producer", null, null, null)
        .toString());
    }

    @Test
    public void testProducerUniverseFactoryFromString() throws Exception {
        final FeaturePackLocation parsedSrc = FeaturePackLocation.fromString("producer@factory");
        Assert.assertNotNull(parsedSrc);
        Assert.assertEquals(new UniverseSpec("factory"), parsedSrc.getUniverse());
        Assert.assertEquals("producer", parsedSrc.getProducerName());
        Assert.assertNull(parsedSrc.getChannelName());
        Assert.assertNull(parsedSrc.getFrequency());
        Assert.assertNull(parsedSrc.getBuild());
    }

    @Test
    public void testProducerUniverseFactoryLocationToString() throws Exception {
        Assert.assertEquals("producer@factory(location)",
                new FeaturePackLocation(new UniverseSpec("factory", "location"), "producer", null, null, null)
        .toString());
    }

    @Test
    public void testProducerUniverseFactoryLocationFromString() throws Exception {
        final FeaturePackLocation parsedSrc = FeaturePackLocation.fromString("producer@factory(location)");
        Assert.assertNotNull(parsedSrc);
        Assert.assertEquals(new UniverseSpec("factory", "location"), parsedSrc.getUniverse());
        Assert.assertEquals("producer", parsedSrc.getProducerName());
        Assert.assertNull(parsedSrc.getChannelName());
        Assert.assertNull(parsedSrc.getFrequency());
        Assert.assertNull(parsedSrc.getBuild());
    }

    @Test
    public void testProducerUniverseFactoryLocationFrequencyToString() throws Exception {
        Assert.assertEquals("producer@factory(location)/frequency",
                new FeaturePackLocation(new UniverseSpec("factory", "location"), "producer", null, "frequency", null)
        .toString());
    }

    @Test
    public void testProducerUniverseFactoryLocationFrequencyFromString() throws Exception {
        final FeaturePackLocation parsedSrc = FeaturePackLocation.fromString("producer@factory(location)/frequency");
        Assert.assertNotNull(parsedSrc);
        Assert.assertEquals(new UniverseSpec("factory", "location"), parsedSrc.getUniverse());
        Assert.assertEquals("producer", parsedSrc.getProducerName());
        Assert.assertNull(parsedSrc.getChannelName());
        Assert.assertEquals("frequency", parsedSrc.getFrequency());
        Assert.assertNull(parsedSrc.getBuild());
    }

    @Test
    public void testProducerUniverseFactoryLocationFrequencyBuildToString() throws Exception {
        Assert.assertEquals("producer@factory(location)/frequency#build",
                new FeaturePackLocation(new UniverseSpec("factory", "location"), "producer", null, "frequency", "build")
        .toString());
    }

    @Test
    public void testProducerUniverseFactoryLocationFrequencyBuildFromString() throws Exception {
        final FeaturePackLocation parsedSrc = FeaturePackLocation.fromString("producer@factory(location)/frequency#build");
        Assert.assertNotNull(parsedSrc);
        Assert.assertEquals(new UniverseSpec("factory", "location"), parsedSrc.getUniverse());
        Assert.assertEquals("producer", parsedSrc.getProducerName());
        Assert.assertNull(parsedSrc.getChannelName());
        Assert.assertEquals("frequency", parsedSrc.getFrequency());
        Assert.assertEquals("build", parsedSrc.getBuild());
    }

    @Test
    public void testProducerChannelFromString() throws Exception {
        final FeaturePackLocation parsedSrc = FeaturePackLocation.fromString("producer:channel");
        Assert.assertNotNull(parsedSrc);
        Assert.assertNull(parsedSrc.getUniverse());
        Assert.assertEquals("producer", parsedSrc.getProducerName());
        Assert.assertEquals("channel", parsedSrc.getChannelName());
        Assert.assertNull(parsedSrc.getFrequency());
        Assert.assertNull(parsedSrc.getBuild());
    }

    @Test
    public void testProducerChannelBuildFromString() throws Exception {
        final FeaturePackLocation parsedSrc = FeaturePackLocation.fromString("producer:channel#build");
        Assert.assertNotNull(parsedSrc);
        Assert.assertNull(parsedSrc.getUniverse());
        Assert.assertEquals("producer", parsedSrc.getProducerName());
        Assert.assertEquals("channel", parsedSrc.getChannelName());
        Assert.assertNull(parsedSrc.getFrequency());
        Assert.assertEquals("build", parsedSrc.getBuild());
    }

    @Test
    public void testGavFromString() throws Exception {
        final FeaturePackLocation parsedSrc = FeaturePackLocation.fromString("groupId:artifactId:version");
        Assert.assertNotNull(parsedSrc);
        Assert.assertEquals("maven", parsedSrc.getUniverse().getFactory());
        Assert.assertNull(parsedSrc.getUniverse().getLocation());
        Assert.assertEquals("groupId:artifactId::zip", parsedSrc.getProducerName());
        Assert.assertNull(parsedSrc.getChannelName());
        Assert.assertNull("frequency", parsedSrc.getFrequency());
        Assert.assertEquals("version", parsedSrc.getBuild());
    }

    @Test
    public void testGavToString() throws Exception {
        Assert.assertEquals("groupId:artifactId:version",
                new FeaturePackLocation(
                        new UniverseSpec("maven"),
                        "groupId:artifactId::zip", null, null, "version")
        .toString());
    }

    @Test
    public void testGacvFromString() throws Exception {
        final FeaturePackLocation parsedSrc = FeaturePackLocation.fromString("groupId:artifactId:classifier:version");
        Assert.assertNotNull(parsedSrc);
        Assert.assertEquals("maven", parsedSrc.getUniverse().getFactory());
        Assert.assertNull(parsedSrc.getUniverse().getLocation());
        Assert.assertEquals("groupId:artifactId:classifier:zip", parsedSrc.getProducerName());
        Assert.assertNull(parsedSrc.getChannelName());
        Assert.assertNull(parsedSrc.getFrequency());
        Assert.assertEquals("version", parsedSrc.getBuild());
    }

    @Test
    public void testGacvToString() throws Exception {
        Assert.assertEquals("groupId:artifactId:classifier:version",
                new FeaturePackLocation(
                        new UniverseSpec("maven"),
                        "groupId:artifactId:classifier:zip", null, null, "version")
        .toString());
    }

    @Test
    public void testGactvFromString() throws Exception {
        final FeaturePackLocation parsedSrc = FeaturePackLocation.fromString("groupId:artifactId:classifier:type:version");
        Assert.assertNotNull(parsedSrc);
        Assert.assertEquals("maven", parsedSrc.getUniverse().getFactory());
        Assert.assertNull(parsedSrc.getUniverse().getLocation());
        Assert.assertEquals("groupId:artifactId:classifier:type", parsedSrc.getProducerName());
        Assert.assertNull(parsedSrc.getChannelName());
        Assert.assertNull(parsedSrc.getFrequency());
        Assert.assertEquals("version", parsedSrc.getBuild());
    }

    @Test
    public void testGactvToString() throws Exception {
        Assert.assertEquals("groupId:artifactId:classifier:type:version",
                new FeaturePackLocation(
                        new UniverseSpec("maven"),
                        "groupId:artifactId:classifier:type", null, null, "version")
        .toString());
    }

    @Test
    public void testGatvFromString() throws Exception {
        final FeaturePackLocation parsedSrc = FeaturePackLocation.fromString("groupId:artifactId::type:version");
        Assert.assertNotNull(parsedSrc);
        Assert.assertEquals("maven", parsedSrc.getUniverse().getFactory());
        Assert.assertNull(parsedSrc.getUniverse().getLocation());
        Assert.assertEquals("groupId:artifactId::type", parsedSrc.getProducerName());
        Assert.assertNull(parsedSrc.getChannelName());
        Assert.assertNull(parsedSrc.getFrequency());
        Assert.assertEquals("version", parsedSrc.getBuild());
    }

    @Test
    public void testGatvToString() throws Exception {
        Assert.assertEquals("groupId:artifactId::type:version",
                new FeaturePackLocation(
                        new UniverseSpec("maven"),
                        "groupId:artifactId::type", null, null, "version")
        .toString());
    }

    @Test
    public void testCompleteLocationToString() throws Exception {
        Assert.assertEquals("producer@factory(location):channel/frequency#build",
                new FeaturePackLocation(
                        new UniverseSpec("factory", "location"),
                        "producer", "channel", "frequency", "build")
        .toString());
    }

    @Test
    public void testCompleteLocationFromString() throws Exception {
        final FeaturePackLocation parsedSrc = FeaturePackLocation.fromString("producer@factory(location):channel/frequency#build");
        Assert.assertNotNull(parsedSrc);
        Assert.assertEquals("factory", parsedSrc.getUniverse().getFactory());
        Assert.assertEquals("location", parsedSrc.getUniverse().getLocation());
        Assert.assertEquals("producer", parsedSrc.getProducerName());
        Assert.assertEquals("channel", parsedSrc.getChannelName());
        Assert.assertEquals("frequency", parsedSrc.getFrequency());
        Assert.assertEquals("build", parsedSrc.getBuild());
    }

    @Test
    public void testLocationWithoutUniverseLocationToString() throws Exception {
        Assert.assertEquals("producer@factory:channel/frequency#build",
                new FeaturePackLocation(
                        new UniverseSpec("factory", null),
                        "producer", "channel", "frequency", "build")
        .toString());
    }

    @Test
    public void testLocationWithoutUniverseLocationFromString() throws Exception {
        final FeaturePackLocation parsedSrc = FeaturePackLocation.fromString("producer@factory:channel/frequency#build");
        Assert.assertNotNull(parsedSrc);
        Assert.assertEquals("factory", parsedSrc.getUniverse().getFactory());
        Assert.assertNull(parsedSrc.getUniverse().getLocation());
        Assert.assertEquals("producer", parsedSrc.getProducerName());
        Assert.assertEquals("channel", parsedSrc.getChannelName());
        Assert.assertEquals("frequency", parsedSrc.getFrequency());
        Assert.assertEquals("build", parsedSrc.getBuild());
    }

    @Test
    public void testChannelWithUniverseLocationAndFrequencyToString() throws Exception {
        final FeaturePackLocation.ChannelSpec channel = new FeaturePackLocation(
                new UniverseSpec("factory", "location"),
                "producer", "channel", "frequency", "build")
        .getChannel();
        Assert.assertEquals("producer@factory(location):channel", channel.toString());
    }

    @Test
    public void testChannelWithUniverseLocationAndFrequencyFromString() throws Exception {
        final FeaturePackLocation parsedCoords = FeaturePackLocation.fromString("producer@factory(location):channel/frequency");
        Assert.assertNotNull(parsedCoords);
        Assert.assertEquals("factory", parsedCoords.getUniverse().getFactory());
        Assert.assertEquals("location", parsedCoords.getUniverse().getLocation());
        Assert.assertEquals("producer", parsedCoords.getProducerName());
        Assert.assertEquals("channel", parsedCoords.getChannelName());
        Assert.assertEquals("frequency", parsedCoords.getFrequency());
        Assert.assertNull(parsedCoords.getBuild());
    }

    @Test
    public void testChannelWithoutUniverseLocationToString() throws Exception {
        final ChannelSpec channel = new FeaturePackLocation(
                new UniverseSpec("factory", null),
                "producer", "channel", "frequency", "build")
        .getChannel();
        Assert.assertEquals("producer@factory:channel", channel.toString());
    }

    @Test
    public void testChannelWithoutUniverseLocationFromString() throws Exception {
        final FeaturePackLocation parsedCoords = FeaturePackLocation.fromString("producer@factory:channel/frequency");
        Assert.assertNotNull(parsedCoords);
        Assert.assertEquals("factory", parsedCoords.getUniverse().getFactory());
        Assert.assertNull(parsedCoords.getUniverse().getLocation());
        Assert.assertEquals("producer", parsedCoords.getProducerName());
        Assert.assertEquals("channel", parsedCoords.getChannelName());
        Assert.assertEquals("frequency", parsedCoords.getFrequency());
        Assert.assertNull(parsedCoords.getBuild());
    }

    @Test
    public void testChannelWithoutFrequencyToString() throws Exception {
        final ChannelSpec channel = new FeaturePackLocation(
                new UniverseSpec("factory", "location"),
                "producer", "channel", null, "build")
        .getChannel();
        Assert.assertEquals("producer@factory(location):channel", channel.toString());
    }

    @Test
    public void testChannelWithoutFrequencyFromString() throws Exception {
        final FeaturePackLocation parsedCoords = FeaturePackLocation.fromString("producer@factory(location):channel");
        Assert.assertNotNull(parsedCoords);
        Assert.assertEquals("factory", parsedCoords.getUniverse().getFactory());
        Assert.assertEquals("location", parsedCoords.getUniverse().getLocation());
        Assert.assertEquals("producer", parsedCoords.getProducerName());
        Assert.assertEquals("channel", parsedCoords.getChannelName());
        Assert.assertNull(parsedCoords.getFrequency());
        Assert.assertNull(parsedCoords.getBuild());
    }

    @Test
    public void testProducerWithUniverseLocationToString() throws Exception {
        final ProducerSpec producer = new FeaturePackLocation(
                new UniverseSpec("factory", "location"),
                "producer", "channel", "frequency", "build")
        .getProducer();
        Assert.assertEquals("producer@factory(location)", producer.toString());
    }

    @Test
    public void testProducerWithUniverseLocationFromString() throws Exception {
        final FeaturePackLocation parsedCoords = FeaturePackLocation.fromString("producer@factory(location)");
        Assert.assertNotNull(parsedCoords);
        Assert.assertEquals("factory", parsedCoords.getUniverse().getFactory());
        Assert.assertEquals("location", parsedCoords.getUniverse().getLocation());
        Assert.assertEquals("producer", parsedCoords.getProducerName());
        Assert.assertNull(parsedCoords.getChannelName());
        Assert.assertNull(parsedCoords.getFrequency());
        Assert.assertNull(parsedCoords.getBuild());
    }

    @Test
    public void testProducerWithoutUniverseToString() throws Exception {
        final ProducerSpec producer = new FeaturePackLocation(
                new UniverseSpec("factory", null),
                "producer", "channel", "frequency", "build")
        .getProducer();
        Assert.assertEquals("producer@factory", producer.toString());
    }

    @Test
    public void testProducerWithoutUniverseLocationFromString() throws Exception {
        final FeaturePackLocation parsedCoords = FeaturePackLocation.fromString("producer@factory");
        Assert.assertNotNull(parsedCoords);
        Assert.assertEquals("factory", parsedCoords.getUniverse().getFactory());
        Assert.assertNull(parsedCoords.getUniverse().getLocation());
        Assert.assertEquals("producer", parsedCoords.getProducerName());
        Assert.assertNull(parsedCoords.getChannelName());
        Assert.assertNull(parsedCoords.getFrequency());
        Assert.assertNull(parsedCoords.getBuild());
    }

    @Test
    public void testLegacyGalleon1ProducerFromString() throws Exception {
        final FeaturePackLocation parsedCoords = FeaturePackLocation.fromString("org.pm.test:fp3@galleon1");
        Assert.assertNotNull(parsedCoords);
        Assert.assertEquals("galleon1", parsedCoords.getUniverse().getFactory());
        Assert.assertNull(parsedCoords.getUniverse().getLocation());
        Assert.assertEquals("org.pm.test:fp3", parsedCoords.getProducerName());
        Assert.assertNull(parsedCoords.getChannelName());
        Assert.assertNull(parsedCoords.getFrequency());
        Assert.assertNull(parsedCoords.getBuild());
    }

    @Test
    public void testMavenProducerWithUniverseToString() throws Exception {
        final ProducerSpec producer = new FeaturePackLocation(
                new UniverseSpec("maven", "org.jboss.universe:jboss-universe:1.0.0.Final"),
                "producer", "channel", "frequency", "build")
        .getProducer();
        Assert.assertEquals("producer@maven(org.jboss.universe:jboss-universe:1.0.0.Final)", producer.toString());
    }

    @Test
    public void testMavenProducerWithUniverseLocationFromString() throws Exception {
        final FeaturePackLocation parsedCoords = FeaturePackLocation.fromString("producer@maven(org.jboss.universe:jboss-universe:1.0.0.Final)");
        Assert.assertNotNull(parsedCoords);
        Assert.assertEquals("maven", parsedCoords.getUniverse().getFactory());
        Assert.assertEquals("org.jboss.universe:jboss-universe:1.0.0.Final", parsedCoords.getUniverse().getLocation());
        Assert.assertEquals("producer", parsedCoords.getProducerName());
        Assert.assertNull(parsedCoords.getChannelName());
        Assert.assertNull(parsedCoords.getFrequency());
        Assert.assertNull(parsedCoords.getBuild());
    }

    @Test
    public void testFeaturePackIdWithUniverseLocationToString() throws Exception {
        final FeaturePackLocation.FPID fpid = new FeaturePackLocation(
                new UniverseSpec("factory", "location"),
                "producer", "channel", "frequency", "build").getFPID();
        Assert.assertEquals("producer@factory(location):channel#build", fpid.toString());
    }

    @Test
    public void testFeaturePackIdWithUniverseLocationFromString() throws Exception {
        final FeaturePackLocation parsedCoords = FeaturePackLocation.fromString("producer@factory(location):channel#build");
        Assert.assertNotNull(parsedCoords);
        Assert.assertEquals("factory", parsedCoords.getUniverse().getFactory());
        Assert.assertEquals("location", parsedCoords.getUniverse().getLocation());
        Assert.assertEquals("producer", parsedCoords.getProducerName());
        Assert.assertEquals("channel", parsedCoords.getChannelName());
        Assert.assertNull(parsedCoords.getFrequency());
        Assert.assertEquals("build", parsedCoords.getBuild());
    }

    @Test
    public void testFeaturePackIdWithoutUniverseLocationToString() throws Exception {
        final FeaturePackLocation.FPID fpid = new FeaturePackLocation(
                new UniverseSpec("factory", null), "producer", "channel", "frequency", "build").getFPID();
        Assert.assertEquals("producer@factory:channel#build", fpid.toString());
    }

    @Test
    public void testFeaturePackIdWithoutUniverseLocationFromString() throws Exception {
        final FeaturePackLocation parsedCoords = FeaturePackLocation.fromString("producer@factory:channel#build");
        Assert.assertNotNull(parsedCoords);
        Assert.assertEquals("factory", parsedCoords.getUniverse().getFactory());
        Assert.assertNull(parsedCoords.getUniverse().getLocation());
        Assert.assertEquals("producer", parsedCoords.getProducerName());
        Assert.assertEquals("channel", parsedCoords.getChannelName());
        Assert.assertNull(parsedCoords.getFrequency());
        Assert.assertEquals("build", parsedCoords.getBuild());
    }

    @Test
    public void testMavenUniverseLocationFromString() throws Exception {
        final FeaturePackLocation parsedCoords = FeaturePackLocation.fromString("wildfly@maven(org.jboss.galleon-universe:jboss-galleon-universe:1.0.0.Final):14/beta#14.0.0.Beta1-SNAPSHOT");
        Assert.assertNotNull(parsedCoords);
        Assert.assertEquals("maven", parsedCoords.getUniverse().getFactory());
        Assert.assertEquals("org.jboss.galleon-universe:jboss-galleon-universe:1.0.0.Final", parsedCoords.getUniverse().getLocation());
        Assert.assertEquals("wildfly", parsedCoords.getProducerName());
        Assert.assertEquals("14", parsedCoords.getChannelName());
        Assert.assertEquals("beta", parsedCoords.getFrequency());
        Assert.assertEquals("14.0.0.Beta1-SNAPSHOT", parsedCoords.getBuild());
    }
}
