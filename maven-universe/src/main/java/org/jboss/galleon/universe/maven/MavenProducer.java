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

package org.jboss.galleon.universe.maven;

import static org.jboss.galleon.universe.maven.MavenUniverseConstants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.model.Gaecvp;
import org.jboss.galleon.model.ResolvedGaecRange;
import org.jboss.galleon.universe.maven.repo.MavenRepoManager;
import org.jboss.galleon.universe.maven.xml.MavenChannelSpecXmlParser;
import org.jboss.galleon.universe.maven.xml.MavenParsedProducerCallbackHandler;
import org.jboss.galleon.universe.maven.xml.MavenProducerXmlParser;
import org.jboss.galleon.universe.maven.xml.ParsedCallbackHandler;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.ZipUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class MavenProducer extends MavenProducerBase<Gaecvp> {

    private final ParsedCallbackHandler<MavenProducerBase<?>, MavenChannel> parsedChannelHandler = new ParsedCallbackHandler<MavenProducerBase<?>, MavenChannel>() {
        public MavenProducerBase<?> getParent() {
            return MavenProducer.this;
        }

        public void parsed(MavenChannel channel) {
            channels = CollectionUtils.put(channels, channel.getName(), channel);
        }
    };

    private Set<String> frequencies = Collections.emptySet();
    private String defaultFrequency;
    private Map<String, MavenChannel> channels = Collections.emptyMap();
    private boolean fullyLoaded;

    public MavenProducer(String name, MavenRepoManager repoManager, ResolvedGaecRange<Gaecvp> artifact) throws MavenUniverseException {
        super(name, repoManager, artifact);
        try (FileSystem zipfs = ZipUtils.newFileSystem(artifact.getResolved().getPath())) {
            final Path producerXml = getProducerXml(zipfs, name);
            if(!Files.exists(producerXml)) {
                throw new MavenUniverseException("Failed to locate " + producerXml + " in " + artifact);
            }
            try(BufferedReader reader = Files.newBufferedReader(producerXml)) {
                MavenProducerXmlParser.getInstance().parse(reader, new MavenParsedProducerCallbackHandler() {
                    @Override
                    public void parsedName(String name) throws XMLStreamException {
                        if(!name.equals(name)) {
                            throw new XMLStreamException("Parsed producer name " + name + " does not match " + MavenProducer.this.name);
                        }
                    }

                    @Override
                    public void parsedFrequency(String frequency, boolean isDefault) throws XMLStreamException {
                        frequencies = CollectionUtils.add(frequencies, frequency);
                        if(isDefault) {
                            if(defaultFrequency != null) {
                                throw new XMLStreamException("Failed to set frequency " + frequency + " as the default one, the default frequency has already been set to " + defaultFrequency);
                            }
                            defaultFrequency = frequency;
                        }
                    }

                    @Override
                    public void parsedFpGroupId(String groupId) {
                        fpGroupId = groupId;
                    }

                    @Override
                    public void parsedFpArtifactId(String artifactId) {
                        fpArtifactId = artifactId;
                    }
                });
            } catch (XMLStreamException e) {
                throw new MavenUniverseException("Failed to parse " + producerXml, e);
            }
        } catch (IOException e) {
            throw new MavenUniverseException("Failed to read " + artifact.getResolved().getPath(), e);
        }
        if(defaultFrequency == null) {
            defaultFrequency = DEFAULT_FREQUENCY;
            if(!frequencies.contains(defaultFrequency)) {
                frequencies = CollectionUtils.add(frequencies, defaultFrequency);
            }
        }
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

    /**
     * This call is synchronized. The set of channels is built lazily and must be
     * thread safe.
     * @param name
     * @return true is the channel exists, false otherwise.
     * @throws MavenUniverseException
     */
    @Override
    public synchronized boolean hasChannel(String name) throws MavenUniverseException {
        if(channels.containsKey(name)) {
            return true;
        } if(fullyLoaded) {
            return false;
        }
        try (FileSystem zipfs = ZipUtils.newFileSystem(artifact.getResolved().getPath())) {
            final Path channelXml = getChannelXml(zipfs, this.name, name);
            if(!Files.exists(channelXml)) {
                return false;
            }
            try(BufferedReader reader = Files.newBufferedReader(channelXml)) {
                MavenChannelSpecXmlParser.getInstance().parse(reader, parsedChannelHandler);
            } catch(IOException | XMLStreamException e) {
                throw new MavenUniverseException("Failed to read " + channelXml, e);
            }
        } catch (IOException e) {
            throw new MavenUniverseException("Failed to read " + artifact.getResolved().getPath(), e);
        }
        return true;
    }

    @Override
    public MavenChannel getChannel(String channelName) throws MavenUniverseException {
        if(!hasChannel(channelName)) {
            throw MavenErrors.channelNotFound(name, channelName);
        }
        return channels.get(channelName);
    }

    /**
     * This call is synchronized. The set of channels is built lazily and must be
     * thread safe.
     * @return The set of channels.
     * @throws MavenUniverseException
     */
    @Override
    public synchronized Collection<MavenChannel> getChannels() throws MavenUniverseException {
        if(fullyLoaded) {
            return channels.values();
        }
        try (FileSystem zipfs = ZipUtils.newFileSystem(artifact.getResolved().getPath())) {
            try(DirectoryStream<Path> stream = Files.newDirectoryStream(getChannelsDir(zipfs, name))) {
                for(Path channelDir : stream) {
                    final Path channelXml = channelDir.resolve(MAVEN_CHANNEL_XML);
                    if(!Files.exists(channelXml)) {
                        throw new MavenUniverseException("Required path does not exist: " + channelXml);
                    }
                    try(BufferedReader reader = Files.newBufferedReader(channelXml)) {
                        MavenChannelSpecXmlParser.getInstance().parse(reader, parsedChannelHandler);
                    } catch(IOException | XMLStreamException e) {
                        throw new MavenUniverseException("Failed to read " + channelXml, e);
                    }
                }
            }
        } catch (IOException e) {
            throw new MavenUniverseException("Failed to read " + artifact.getResolved().getPath(), e);
        }
        fullyLoaded = true;
        channels = CollectionUtils.unmodifiable(channels);
        return channels.values();
    }
}
