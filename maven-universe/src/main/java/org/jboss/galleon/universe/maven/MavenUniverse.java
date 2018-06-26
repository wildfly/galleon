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

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.Errors;
import org.jboss.galleon.universe.maven.repo.MavenRepoManager;
import org.jboss.galleon.universe.maven.xml.MavenProducerSpecXmlParser;
import org.jboss.galleon.universe.maven.xml.ParsedCallbackHandler;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.ZipUtils;

import static org.jboss.galleon.universe.maven.MavenUniverseConstants.*;

/**
 *
 * @author Alexey Loubyansky
 */
public class MavenUniverse extends MavenUniverseBase {

    private static final String DEFAULT_RANGE = "[0.0,)";

    private Map<String, MavenProducer> producers = Collections.emptyMap();
    private boolean fullyLoaded;

    private final ParsedCallbackHandler<MavenUniverseBase, MavenProducer> parsedProducerHandler = new ParsedCallbackHandler<MavenUniverseBase, MavenProducer>() {
        @Override
        public MavenUniverseBase getParent() {
            return MavenUniverse.this;
        }
        @Override
        public void parsed(MavenProducer producer) throws XMLStreamException {
            producers = CollectionUtils.put(producers, producer.getName(), producer);
        }
    };

    public MavenUniverse(MavenRepoManager repoManager, MavenArtifact artifact) throws MavenUniverseException {
        super(repoManager, artifact);
        if(!artifact.isResolved()) {
            if(artifact.getVersionRange() != null) {
                repoManager.resolveLatestVersion(artifact);
            } else if(artifact.getVersion() != null) {
                repoManager.resolve(artifact);
            } else {
                artifact.setVersionRange(DEFAULT_RANGE);
                repoManager.resolveLatestVersion(artifact);
            }
        }
    }

    public void resetCache() {
        fullyLoaded = false;
        producers = Collections.emptyMap();
    }

    @Override
    public boolean hasProducer(String producerName) throws MavenUniverseException {
        if(producers.containsKey(producerName)) {
            return true;
        } if(fullyLoaded) {
            return false;
        }
        try (FileSystem zipfs = FileSystems.newFileSystem(ZipUtils.toZipUri(artifact.getPath()), Collections.emptyMap())) {
            final Path producerXml = getProducerXml(zipfs, producerName);
            if(!Files.exists(producerXml)) {
                return false;
            }
            try(BufferedReader reader = Files.newBufferedReader(producerXml)) {
                MavenProducerSpecXmlParser.getInstance().parse(reader, parsedProducerHandler);
            } catch(IOException | XMLStreamException e) {
                throw new MavenUniverseException("Failed to read " + producerXml, e);
            }
        } catch (IOException e) {
            throw new MavenUniverseException("Failed to read " + artifact.getPath(), e);
        }
        return true;
    }

    @Override
    public MavenProducer getProducer(String producerName) throws MavenUniverseException {
        if(!hasProducer(producerName)) {
            throw MavenErrors.producerNotFound(producerName);
        }
        return producers.get(producerName);
    }

    @Override
    public Collection<MavenProducer> getProducers() throws MavenUniverseException {
        if(fullyLoaded) {
            return producers.values();
        }
        try (FileSystem zipfs = FileSystems.newFileSystem(ZipUtils.toZipUri(artifact.getPath()), Collections.emptyMap())) {
            try(DirectoryStream<Path> stream = Files.newDirectoryStream(getProducerLocations(zipfs))) {
                for(Path producerDir : stream) {
                    final Path producerXml = producerDir.resolve(MAVEN_PRODUCER_XML);
                    if(!Files.exists(producerXml)) {
                        throw new MavenUniverseException(Errors.pathDoesNotExist(producerXml));
                    }
                    try(BufferedReader reader = Files.newBufferedReader(producerXml)) {
                        MavenProducerSpecXmlParser.getInstance().parse(reader, parsedProducerHandler);
                    } catch(IOException | XMLStreamException e) {
                        throw new MavenUniverseException("Failed to read " + producerXml, e);
                    }
                }
            }
        } catch (IOException e) {
            throw new MavenUniverseException("Failed to read " + artifact.getPath(), e);
        }
        fullyLoaded = true;
        producers = CollectionUtils.unmodifiable(producers);
        return producers.values();
    }
}
