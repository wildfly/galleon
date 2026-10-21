/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.universe.maven;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.universe.maven.repo.MavenRepoManager;
import org.jboss.galleon.universe.maven.xml.MavenProducerSpecXmlWriter;
import org.jboss.galleon.util.IoUtils;
import org.jboss.galleon.util.ZipUtils;

import static org.jboss.galleon.universe.maven.MavenUniverseConstants.*;

/**
 *
 * @author Alexey Loubyansky
 */
public class MavenUniverseInstaller extends MavenUniverseBase {

    private Map<String, MavenProducer> producers = new HashMap<>();
    private boolean installed;

    public MavenUniverseInstaller(MavenRepoManager repoManager, MavenArtifact artifact) {
        super(repoManager, artifact);
    }

    public MavenUniverseInstaller extendUniverse(MavenArtifact extendArtifact) throws MavenUniverseException {
        final MavenUniverse otherUniverse = new MavenUniverse(repo, extendArtifact);
        for(MavenProducer producer : otherUniverse.getProducers()) {
            addProducer(producer);
        }
        return this;
    }

    public MavenUniverseInstaller addProducer(String producer, String groupId, String artifactId, String versionRange) throws MavenUniverseException {
        return addProducer(producer, new MavenArtifact().setGroupId(groupId).setArtifactId(artifactId).setVersionRange(versionRange));
    }

    public MavenUniverseInstaller addProducer(String producer, MavenArtifact artifact) throws MavenUniverseException {
        if(artifact.getVersionRange() == null) {
            MavenErrors.missingVersionRange(artifact);
        }
        return addProducer(new MavenProducer(producer, repo, artifact));
    }

    public MavenUniverseInstaller addProducer(MavenProducer producer) throws MavenUniverseException {
        producers.put(producer.getName(), producer);
        return this;
    }

    public MavenUniverseInstaller removeProducer(String producerName) {
        if(!producers.isEmpty()) {
            producers.remove(producerName);
        }
        return this;
    }

    public void install() throws MavenUniverseException {
        if(installed) {
            throw new MavenUniverseException("The universe has already been installed");
        }
        Path tmpDir = null;
        try {
            tmpDir = Files.createTempDirectory("gln-mvn-universe");
            final Path zipRoot = tmpDir.resolve("root");
            final Path locations = getProducerLocations(zipRoot);
            Files.createDirectories(locations);
            for(MavenProducerBase producer : producers.values()) {
                final Path producerDir = locations.resolve(producer.getName());
                Files.createDirectory(producerDir);
                final Path producerXml = producerDir.resolve(MAVEN_PRODUCER_XML);
                MavenProducerSpecXmlWriter.getInstance().write(producer, producerXml);
            }
            final Path artifactFile = tmpDir.resolve(artifact.getArtifactFileName());
            Files.createDirectories(artifactFile.getParent());
            ZipUtils.zip(zipRoot, artifactFile);
            repo.install(artifact, artifactFile);
        } catch (IOException | XMLStreamException e) {
            throw new MavenUniverseException("Failed to create Maven universe artifact", e);
        } finally {
            if(tmpDir != null) {
                IoUtils.recursiveDelete(tmpDir);
            }
        }
        installed = true;
    }

    @Override
    public boolean hasProducer(String producerName) throws MavenUniverseException {
        return producers.containsKey(producerName);
    }

    @Override
    public MavenProducer getProducer(String producerName) throws MavenUniverseException {
        final MavenProducer producer = producers.get(producerName);
        if(producer == null) {
            throw MavenErrors.producerNotFound(producerName);
        }
        return producer;
    }

    @Override
    public Collection<MavenProducer> getProducers() throws MavenUniverseException {
        return producers.values();
    }

}
