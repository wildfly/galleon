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
