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
package org.jboss.galleon.universe.maven;

import static org.jboss.galleon.universe.maven.MavenUniverseConstants.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.universe.maven.repo.MavenRepoManager;
import org.jboss.galleon.universe.maven.xml.MavenChannelSpecXmlWriter;
import org.jboss.galleon.universe.maven.xml.MavenProducerXmlWriter;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.IoUtils;
import org.jboss.galleon.util.ZipUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class MavenProducerInstaller extends MavenProducerBase {

    private Set<String> frequencies = Collections.emptySet();
    private String defaultFrequency;
    private MavenChannel defaultChannel;
    private Map<String, MavenChannel> channels = new HashMap<>();
    private boolean installed;

    public MavenProducerInstaller(String name, MavenRepoManager repoManager, MavenArtifact artifact) throws MavenUniverseException {
        this(name, repoManager, artifact, null, null);
    }

    public MavenProducerInstaller(String name, MavenRepoManager repoManager, MavenArtifact artifact, String fpGroupId, String fpArtifactId) throws MavenUniverseException {
        super(name, repoManager, artifact);
        this.fpGroupId = fpGroupId;
        this.fpArtifactId = fpArtifactId;
    }

    public MavenProducerInstaller(String name, MavenRepoManager repoManager, MavenArtifact artifact, MavenArtifact extendArtifact) throws MavenUniverseException {
        super(name, repoManager, artifact);
        extendProducer(name, extendArtifact);
    }

    public MavenProducerInstaller extendProducer(String name, MavenArtifact extendArtifact) throws MavenUniverseException {
        final MavenProducer otherProducer = new MavenProducer(name, repo, extendArtifact);
        if(fpGroupId == null) {
            fpGroupId = otherProducer.getFeaturePackGroupId();
        }
        if(fpArtifactId == null) {
            fpArtifactId = otherProducer.getFeaturePackArtifactId();
        }
        if (frequencies.isEmpty()) {
            frequencies = new HashSet<>(otherProducer.getFrequencies());
        } else {
            frequencies.addAll(otherProducer.getFrequencies());
        }
        if(defaultFrequency == null) {
            defaultFrequency = otherProducer.getDefaultFrequency();
        }
        for(MavenChannel channel : otherProducer.getChannels()) {
            addChannel(channel);
        }
        if(defaultChannel == null) {
            defaultChannel = otherProducer.getDefaultChannel();
        }
        return this;
    }

    public MavenProducerInstaller addFrequency(String name) throws MavenUniverseException {
        return addFrequency(name, false);
    }

    public MavenProducerInstaller addFrequency(String name, boolean isDefault) throws MavenUniverseException {
        if(frequencies.isEmpty()) {
            frequencies = new HashSet<>();
        }
        frequencies.add(name);
        if(isDefault) {
            defaultFrequency = name;
        }
        return this;
    }

    public MavenProducerInstaller addFrequencies(String... names) {
        if(frequencies.isEmpty()) {
            frequencies = new HashSet<>(names.length);
        }
        for(String frequency : names) {
            frequencies.add(frequency);
        }
        return this;
    }

    @Override
    public boolean hasFrequencies() {
        return !frequencies.isEmpty();
    }

    @Override
    public Collection<String> getFrequencies() {
        return frequencies;
    }

    @Override
    public boolean hasDefaultFrequency() {
        return defaultFrequency != null;
    }

    @Override
    public String getDefaultFrequency() {
        return defaultFrequency;
    }

    public MavenProducerInstaller removeFrequency(String frequency) {
        frequencies.remove(frequency);
        return this;
    }

    public MavenProducerInstaller addChannel(String channelName, String versionRange) throws MavenUniverseException {
        return addChannel(channelName, versionRange, false);
    }

    public MavenProducerInstaller addChannel(String channelName, String versionRange, boolean isDefault) throws MavenUniverseException {
        return addChannel(new MavenChannel(this, channelName, versionRange), isDefault);
    }

    public MavenProducerInstaller addChannel(String channelName, String versionRange, boolean isDefault,
                                             String includeVersionRegex, String excludeVersionRegex) throws MavenUniverseException {
        return addChannel(new MavenChannel(this, channelName, versionRange, includeVersionRegex, excludeVersionRegex), isDefault);
    }

    public MavenProducerInstaller addChannel(MavenChannel channel) throws MavenUniverseException {
        channels.put(channel.getName(), channel);
        return this;
    }

    public MavenProducerInstaller addChannel(MavenChannel channel, boolean isDefault) throws MavenUniverseException {
        channels.put(channel.getName(), channel);
        if(isDefault) {
            defaultChannel = channel;
        }
        return this;
    }

    public MavenProducerInstaller removeChannel(String channelName) {
        if(!channels.isEmpty()) {
            if(channels.remove(channelName) == defaultChannel) {
                defaultChannel = null;
            }
        }
        return this;
    }

    @Override
    public boolean hasChannel(String name) throws MavenUniverseException {
        return channels.containsKey(name);
    }

    @Override
    public MavenChannel getChannel(String channelName) throws MavenUniverseException {
        final MavenChannel channel = channels.get(channelName);
        if(channel == null) {
            throw MavenErrors.channelNotFound(name, channelName);
        }
        return channel;
    }

    @Override
    public Collection<MavenChannel> getChannels() throws MavenUniverseException {
        return channels.values();
    }

    @Override
    public boolean hasDefaultChannel() {
        return defaultChannel != null;
    }

    @Override
    public String getDefaultChannelName() {
        return defaultChannel == null ? null : defaultChannel.getName();
    }

    @Override
    public MavenChannel getDefaultChannel() {
        return defaultChannel;
    }

    public MavenProducerInstaller install() throws MavenUniverseException {
        if(installed) {
            throw new MavenUniverseException("The universe has already been installed");
        }
        if(defaultFrequency == null) {
            defaultFrequency = DEFAULT_FREQUENCY;
            if(!frequencies.contains(defaultFrequency)) {
                frequencies = CollectionUtils.add(frequencies, defaultFrequency);
            }
        }
        Path tmpDir = null;
        try {
            tmpDir = Files.createTempDirectory("gln-mvn-producer");
            final Path zipRoot = tmpDir.resolve("root");
            addProducerDescription(this, zipRoot);
            final Path artifactFile = tmpDir.resolve(artifact.getArtifactFileName());
            Files.createDirectories(artifactFile.getParent());
            ZipUtils.zip(zipRoot, artifactFile);
            repo.install(artifact, artifactFile);
            installed = true;
            return this;
        } catch (IOException | XMLStreamException e) {
            throw new MavenUniverseException("Failed to create Maven universe producer artifact", e);
        } finally {
            if(tmpDir != null) {
                IoUtils.recursiveDelete(tmpDir);
            }
        }
    }

    static void addProducerDescription(MavenProducerDescription<?> producer, final Path zipRoot) throws XMLStreamException, IOException, MavenUniverseException {
        final Path producerDir = getProducerDir(zipRoot, producer.getName());
        MavenProducerXmlWriter.getInstance().write(producer, producerDir.resolve(MAVEN_PRODUCER_XML));
        final Path channelsDir = producerDir.resolve(CHANNELS);
        Files.createDirectory(channelsDir);
        for(MavenChannelDescription channel : producer.getChannels()) {
            final Path channelDir = channelsDir.resolve(channel.getName());
            Files.createDirectory(channelDir);
            final Path channelXml = channelDir.resolve(MAVEN_CHANNEL_XML);
            MavenChannelSpecXmlWriter.getInstance().write(channel, channelXml);
        }
    }
}
