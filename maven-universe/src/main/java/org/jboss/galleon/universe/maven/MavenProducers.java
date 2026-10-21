/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.universe.maven;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.universe.maven.repo.MavenRepoManager;
import org.jboss.galleon.util.IoUtils;
import org.jboss.galleon.util.ZipUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class MavenProducers {

    public static MavenProducers getInstance(MavenRepoManager repoManager, MavenArtifact artifact) {
        return new MavenProducers(repoManager, artifact);
    }

    private final MavenRepoManager repoManager;
    private final MavenArtifact artifact;
    private final List<MavenProducerDescription<?>> producers = new ArrayList<>();

    private MavenProducers(MavenRepoManager repoManager, MavenArtifact artifact) {
        this.repoManager = repoManager;
        this.artifact = artifact;
    }

    public void addProducer(MavenProducerDescription<?> producer) {
        producers.add(producer);
    }

    public void install() throws MavenUniverseException {
        Path tmpDir = null;
        try {
            tmpDir = Files.createTempDirectory("gln-mvn-channel");
            final Path zipRoot = tmpDir.resolve("root");
            for (MavenProducerDescription<?> producer : producers) {
                MavenProducerInstaller.addProducerDescription(producer, zipRoot);
            }
            final Path artifactFile = tmpDir.resolve(artifact.getArtifactFileName());
            Files.createDirectories(artifactFile.getParent());
            ZipUtils.zip(zipRoot, artifactFile);
            repoManager.install(artifact, artifactFile);
        } catch (IOException | XMLStreamException e) {
            throw new MavenUniverseException("Failed to create Maven universe producer artifact", e);
        } finally {
            if(tmpDir != null) {
                IoUtils.recursiveDelete(tmpDir);
            }
        }
    }
}
