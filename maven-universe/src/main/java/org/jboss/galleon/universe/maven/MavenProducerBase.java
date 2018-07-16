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

import java.nio.file.FileSystem;
import java.nio.file.Path;
import org.jboss.galleon.universe.Producer;
import org.jboss.galleon.universe.maven.repo.MavenRepoManager;

import static org.jboss.galleon.universe.maven.MavenUniverseConstants.*;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class MavenProducerBase implements Producer<MavenChannel>, MavenProducerDescription<MavenChannel> {

    protected static final String DEFAULT_FREQUENCY = "final"; // for maven artifact versions that's the default one

    protected final String name;
    protected final MavenRepoManager repo;
    protected final MavenArtifact artifact;
    protected String fpGroupId;
    protected String fpArtifactId;

    protected MavenProducerBase(String name, MavenRepoManager repoManager, MavenArtifact artifact) throws MavenUniverseException {
        this.name = name;
        this.repo = repoManager;
        this.artifact = artifact;
    }

    @Override
    public String getName() {
        return name;
    }

    public MavenArtifact getArtifact() {
        return artifact;
    }

    public MavenRepoManager getRepo() {
        return repo;
    }

    @Override
    public String getFeaturePackGroupId() {
        return fpGroupId;
    }

    @Override
    public String getFeaturePackArtifactId() {
        return fpArtifactId;
    }

    protected static Path getProducerDir(FileSystem zipfs, String producerName) {
        return zipfs.getPath(GALLEON, UNIVERSE, PRODUCER, producerName);
    }

    static Path getProducerDir(Path root, String producerName) {
        return root.resolve(GALLEON).resolve(UNIVERSE).resolve(PRODUCER).resolve(producerName);
    }

    protected static Path getProducerXml(FileSystem zipfs, String producerName) {
        return getProducerDir(zipfs, producerName).resolve(MAVEN_PRODUCER_XML);
    }

    protected static Path getChannelsDir(FileSystem zipfs, String producerName) {
        return zipfs.getPath(GALLEON, UNIVERSE, PRODUCER, producerName, CHANNELS);
    }

    protected static Path getChannelXml(FileSystem zipfs, String producerName, String channelName) {
        return getProducerDir(zipfs, producerName).resolve(CHANNELS).resolve(channelName).resolve(MAVEN_CHANNEL_XML);
    }
}
