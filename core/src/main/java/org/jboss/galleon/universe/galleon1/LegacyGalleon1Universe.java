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

package org.jboss.galleon.universe.galleon1;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.repo.RepositoryArtifactResolver;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;
import org.jboss.galleon.universe.Universe;
import org.jboss.galleon.universe.UniverseSpec;
import org.jboss.galleon.util.CollectionUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class LegacyGalleon1Universe implements Universe<LegacyGalleon1Producer> {

    private static final String ZIP = "zip";

    private static UniverseSpec universeSource;

    public static UniverseSpec getUniverseSpec() {
        if(universeSource == null) {
            universeSource = new UniverseSpec(LegacyGalleon1UniverseFactory.ID, null);
        }
        return universeSource;
    }

    public static String toMavenCoords(FeaturePackLocation fpl) throws ProvisioningException {
        final String producer = fpl.getProducerName();
        final int colon = producer.indexOf(':');
        if(colon <= 0) {
            throw new ProvisioningException("Failed to determine group and artifact IDs for " + fpl);
        }
        final StringBuilder buf = new StringBuilder();
        buf.append(producer.substring(0, colon)).append(':').append(producer.substring(colon + 1)).append(':').append(ZIP);
        buf.append(':').append(fpl.getBuild());
        return buf.toString();
    }

    public static FeaturePackLocation toFpl(String groupId, String artifactId, String version) {
        if(version == null) {
            return new FeaturePackLocation(
                    new UniverseSpec(LegacyGalleon1UniverseFactory.ID, null),
                    groupId + ':' + artifactId,
                    null, null, version);
        }
        final int i = version.indexOf('.');
        return new FeaturePackLocation(
                new UniverseSpec(LegacyGalleon1UniverseFactory.ID, null),
                groupId + ':' + artifactId,
                i > 0 ? version.substring(0, i) : version, null, version);
    }

    public static FPID newFPID(String producer, String channel, String build) {
        return new FeaturePackLocation(getUniverseSpec(), producer, channel, null, build).getFPID();
    }

    public static ProducerSpec newProducer(String producer) {
        return new FeaturePackLocation(new UniverseSpec(LegacyGalleon1UniverseFactory.ID, null), producer, null, null, null).getProducer();
    }

    final RepositoryArtifactResolver artifactResolver;
    private Map<String, LegacyGalleon1Producer> producers = Collections.emptyMap();

    public LegacyGalleon1Universe(RepositoryArtifactResolver artifactResolver) {
        this.artifactResolver = artifactResolver;
    }

    @Override
    public String getFactoryId() {
        return LegacyGalleon1UniverseFactory.ID;
    }

    @Override
    public String getLocation() {
        return null;
    }

    @Override
    public boolean hasProducer(String producerName) throws ProvisioningException {
        return true;
    }

    @Override
    public LegacyGalleon1Producer getProducer(String producerName) throws ProvisioningException {
        LegacyGalleon1Producer producer = producers.get(producerName);
        if(producer == null) {
            producer = new LegacyGalleon1Producer(this, producerName);
            producers = CollectionUtils.put(producers, producerName, producer);
        }
        return producer;
    }

    @Override
    public Collection<LegacyGalleon1Producer> getProducers() throws ProvisioningException {
        return producers.values();
    }
}
