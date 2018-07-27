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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.model.Gaecv;
import org.jboss.galleon.model.Gaecvp;
import org.jboss.galleon.universe.maven.repo.MavenRepoManager;
import org.jboss.galleon.util.IoUtils;
import org.jboss.galleon.util.ZipUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class MavenProducers {


    private final MavenRepoManager repoManager;
    private final List<MavenProducerDescription<?>> producers = new ArrayList<>();

    public MavenProducers(MavenRepoManager repoManager) {
        this.repoManager = repoManager;
    }

    public void addProducer(MavenProducerDescription<?> producer) {
        producers.add(producer);
    }

    public Gaecvp install(Gaecv artifact) throws MavenUniverseException {
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
            return repoManager.install(artifact, artifactFile);
        } catch (IOException | XMLStreamException | ProvisioningException e) {
            throw new MavenUniverseException("Failed to create Maven universe producer artifact", e);
        } finally {
            if(tmpDir != null) {
                IoUtils.recursiveDelete(tmpDir);
            }
        }
    }
}
