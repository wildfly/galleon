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

import java.nio.file.FileSystem;
import java.nio.file.Path;
import org.jboss.galleon.universe.Universe;
import org.jboss.galleon.universe.maven.repo.MavenRepoManager;

import static org.jboss.galleon.universe.maven.MavenUniverseConstants.*;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class MavenUniverseBase implements Universe<MavenProducer> {

    protected final MavenRepoManager repo;
    protected final MavenArtifact artifact;

    protected MavenUniverseBase(MavenRepoManager repoManager, MavenArtifact artifact) {
        this.repo = repoManager;
        this.artifact = artifact;
    }

    @Override
    public String getFactoryId() {
        return MavenUniverseFactory.ID;
    }

    @Override
    public String getLocation() {
        return artifact.getCoordsAsString();
    }

    public MavenRepoManager getRepo() {
        return repo;
    }

    public MavenArtifact getArtifact() {
        return artifact;
    }

    protected static Path getProducerLocations(FileSystem zipfs) {
        return zipfs.getPath(GALLEON, UNIVERSE, PRODUCER, LOCATIONS);
    }

    protected static Path getProducerLocations(Path root) {
        return root.resolve(GALLEON).resolve(UNIVERSE).resolve(PRODUCER).resolve(LOCATIONS);
    }

    protected static Path getProducerXml(FileSystem zipfs, String producerName) {
        return getProducerXml(getProducerLocations(zipfs), producerName);
    }

    protected static Path getProducerXml(Path producersDir, String producerName) {
        return producersDir.resolve(producerName).resolve(MAVEN_PRODUCER_XML);
    }
}
