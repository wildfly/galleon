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

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.Errors;
import org.jboss.galleon.universe.maven.repo.MavenRepoManager;
import org.jboss.galleon.universe.maven.xml.MavenProducerSpecXmlParser;
import org.jboss.galleon.universe.maven.xml.ParsedCallbackHandler;
import org.jboss.galleon.util.CollectionUtils;

import static org.jboss.galleon.universe.maven.MavenUniverseConstants.*;
import org.jboss.galleon.util.ZipUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class MavenUniverse extends MavenUniverseBase {

    private static final int DEFAULT_CAPACITY = 5;
    private static final String DEFAULT_RANGE = "[0.0,)";

    static boolean resolveUniverseArtifact(MavenRepoManager repoManager, MavenArtifact artifact, boolean locallyAvailablePreferred) throws MavenUniverseException {
        if (artifact.getVersionRange() == null) {
            if (artifact.hasVersion()) {
                repoManager.resolve(artifact);
                return false;
            }
            artifact.setVersionRange(DEFAULT_RANGE);
        }
        if(locallyAvailablePreferred) {
            try {
                repoManager.resolveLatestVersion(artifact, true);
                return true;
            } catch (MavenUniverseException e) {
            }
        }
        repoManager.resolveLatestVersion(artifact, false);
        return false;
    }

    private Map<String, MavenProducer> producers = new HashMap<>(DEFAULT_CAPACITY);
    private boolean fullyLoaded;
    private boolean resolvedLocally;

    private final ParsedCallbackHandler<MavenUniverse, MavenProducer> parsedProducerHandler = new ParsedCallbackHandler<MavenUniverse, MavenProducer>() {
        @Override
        public MavenUniverse getParent() {
            return MavenUniverse.this;
        }
        @Override
        public void parsed(MavenProducer producer) throws XMLStreamException {
            producers.put(producer.getName(), producer);
        }
    };

    /**
     * This method will be looking for the latest locally available version of the universe
     * artifact. If no version is available locally, it will fallback to resolving the latest
     * available in remote repositories.
     *
     * @param repoManager  maven repository manager
     * @param artifact  universe artifact
     * @throws MavenUniverseException  in case of a failure
     */
    public MavenUniverse(MavenRepoManager repoManager, MavenArtifact artifact) throws MavenUniverseException {
        this(repoManager, artifact, false);
    }

    public MavenUniverse(MavenRepoManager repoManager, MavenArtifact artifact, boolean absoluteLatest) throws MavenUniverseException {
        super(repoManager, artifact);
        if(artifact.isResolved()) {
            return;
        }
        resolvedLocally = resolveUniverseArtifact(repoManager, artifact, !absoluteLatest);
    }

    public void refresh() throws MavenUniverseException {
        if(fullyLoaded) {
            fullyLoaded = false;
            producers = new HashMap<>(DEFAULT_CAPACITY);
        } else {
            producers.clear();
        }
        artifact.setPath(null);
        if (artifact.getVersionRange() != null) {
            repo.resolveLatestVersion(artifact, false);
        } else if (artifact.hasVersion()) {
            repo.resolve(artifact);
        } else {
            throw new MavenUniverseException("Universe artifact is missing version and version range: " + artifact);
        }
        resolvedLocally = false;
    }

    /**
     * This call is synchronized. The set of producers is built lazily and must be
     * thread safe.
     * @param producerName
     * @return  true is the producer exists, false otherwise.
     * @throws MavenUniverseException
     */
    @Override
    public synchronized boolean hasProducer(String producerName) throws MavenUniverseException {
        if(producers.containsKey(producerName)) {
            return true;
        }
        if(fullyLoaded) {
            return false;
        }
        try (FileSystem zipfs = ZipUtils.newFileSystem(artifact.getPath())) {
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
            boolean found = false;
            if(resolvedLocally) {
                try {
                    refresh();
                } catch(MavenUniverseException e) {
                    throw new MavenUniverseException(MavenErrors.msgProducerNotFound(producerName), e);
                }
                found = hasProducer(producerName);
            }
            if(!found) {
                throw MavenErrors.producerNotFound(producerName);
            }
        }
        return producers.get(producerName);
    }

    /**
     * This call is synchronized. The set of producers is built lazily and must be
     * thread safe.
     * @return The set of producers.
     * @throws MavenUniverseException
     */
    @Override
    public synchronized Collection<MavenProducer> getProducers() throws MavenUniverseException {
        if(fullyLoaded) {
            return producers.values();
        }
        try (FileSystem zipfs = ZipUtils.newFileSystem(artifact.getPath())) {
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
