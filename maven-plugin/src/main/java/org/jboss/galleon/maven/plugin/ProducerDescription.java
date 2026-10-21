/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.maven.plugin;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.maven.plugins.annotations.Parameter;
import org.jboss.galleon.universe.maven.MavenUniverseException;
import org.jboss.galleon.universe.maven.MavenProducerDescription;

/**
 *
 * @author Alexey Loubyansky
 */
public class ProducerDescription implements MavenProducerDescription<ChannelDescription> {

    /**
     * Producer name
     */
    @Parameter(required = true)
    String name;

    /**
     * Producer groupId
     */
    @Parameter(required = true)
    String groupId;

    /**
     * Producer artifactId
     */
    @Parameter(required = true)
    String artifactId;

    /**
     * Producer version
     */
    @Parameter(required = true)
    String version;

    /**
     * Feature-pack groupId
     */
    @Parameter(required = true, alias="feature-pack-groupId")
    String featurePackGroupId;

    /**
     * Feature-pack artifactId
     */
    @Parameter(required = true, alias="feature-pack-artifactId")
    String featurePackArtifactId;

    /**
     * Channel frequencies
     */
    @Parameter(required = true)
    List<String> frequencies = Collections.emptyList();

    /**
     * Default frequency
     */
    @Parameter(required = false)
    String defaultFrequency;

    /**
     * Channels
     */
    @Parameter(required = true)
    List<ChannelDescription> channels = Collections.emptyList();

    /**
     * Default channel
     */
    @Parameter(required = false)
    String defaultChannel;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFeaturePackGroupId() {
        return featurePackGroupId;
    }

    @Override
    public String getFeaturePackArtifactId() {
        return featurePackArtifactId;
    }

    @Override
    public Collection<String> getFrequencies() {
        return frequencies;
    }

    @Override
    public String getDefaultFrequency() {
        return defaultFrequency;
    }

    @Override
    public Collection<ChannelDescription> getChannels() throws MavenUniverseException {
        return channels;
    }

    @Override
    public boolean hasDefaultChannel() {
        return defaultChannel != null;
    }

    @Override
    public String getDefaultChannelName() {
        return defaultChannel;
    }
}
