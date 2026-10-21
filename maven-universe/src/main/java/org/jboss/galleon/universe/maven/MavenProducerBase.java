/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
