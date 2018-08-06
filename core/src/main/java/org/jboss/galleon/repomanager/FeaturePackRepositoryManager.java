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
package org.jboss.galleon.repomanager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.ArtifactException;
import org.jboss.galleon.ArtifactRepositoryManager;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.model.Gaec;
import org.jboss.galleon.model.Gaecv;
import org.jboss.galleon.model.Gaecvp;
import org.jboss.galleon.util.ZipUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeaturePackRepositoryManager implements ArtifactRepositoryManager {

    public static FeaturePackRepositoryManager newInstance(Path repoHome) {
        return new FeaturePackRepositoryManager(repoHome);
    }

    final Path repoHome;

    private FeaturePackRepositoryManager(Path repoHome) {
        this.repoHome = repoHome;
    }

    @Override
    public Gaecvp install(Gaecv artifact, Path path) throws ProvisioningException {
        try {
            final Path repoPath = getArtifactPath(artifact);
            Files.createDirectories(repoPath.getParent());
            if(Files.isDirectory(path)) {
                 ZipUtils.zip(path, repoPath);
            }else {
                Files.copy(path, repoPath, StandardCopyOption.REPLACE_EXISTING);
            }
            return new Gaecvp(artifact, repoPath);
        } catch (IOException ex) {
            throw new ProvisioningException("Could not install "+ artifact +" "+ path, ex);
        }
    }

    @Override
    public void deploy(ArtifactCoords coords, Path artifact) throws ArtifactException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Gaecvp resolve(Gaecv artifact) throws ProvisioningException {
        final Path path = getArtifactPath(artifact);
        if(!Files.exists(path)) {
            throw new ArtifactException("Artifact " + artifact + " not found in the repository at " + path);
        }
        return new Gaecvp(artifact, path);
    }

    private Path getArtifactPath(final Gaecv coords) {
        final Gaec gaec = coords.getGaec();
        Path p = repoHome;
        final String[] groupParts = gaec.getGroupId().split("\\.");
        for (String part : groupParts) {
            p = p.resolve(part);
        }
        p = p.resolve(gaec.getArtifactId());
        p = p.resolve(coords.getVersion());
        final StringBuilder fileName = new StringBuilder();
        fileName.append(gaec.getArtifactId()).append('-').append(coords.getVersion());
        if(gaec.hasClassifier()) {
            fileName.append('-').append(gaec.getClassifier());
        }
        fileName.append('.').append(gaec.getExtension());
        return p.resolve(fileName.toString());
    }

    @Override
    public String getHighestVersion(ArtifactCoords coords, String range) throws ArtifactException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
