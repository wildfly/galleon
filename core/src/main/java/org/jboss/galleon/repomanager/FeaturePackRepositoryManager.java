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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.ArtifactException;
import org.jboss.galleon.ArtifactRepositoryManager;
import org.jboss.galleon.util.ZipUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeaturePackRepositoryManager implements ArtifactRepositoryManager {

    public static FeaturePackRepositoryManager newInstance(Path repoHome) {
        return new FeaturePackRepositoryManager(repoHome);
    }

    @Override
    public void install(ArtifactCoords coords, Path artifact) throws ArtifactException {
        try {
            final Path path = getArtifactPath(coords);
            Files.createDirectories(path.getParent());
            if(Files.isDirectory(artifact)) {
                 ZipUtils.zip(artifact, path);
            }else {
                Files.copy(artifact, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            Logger.getLogger(FeaturePackRepositoryManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void deploy(ArtifactCoords coords, Path artifact) throws ArtifactException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    final Path repoHome;

    private FeaturePackRepositoryManager(Path repoHome) {
        this.repoHome = repoHome;
    }

    public FeaturePackInstaller installer() {
        return new FeaturePackInstaller(this);
    }

    @Override
    public Path resolve(ArtifactCoords coords) throws ArtifactException {
        final Path path = getArtifactPath(coords);
        if(!Files.exists(path)) {
            throw new ArtifactException("Artifact " + coords + " not found in the repository at " + path);
        }
        return path;
    }

    Path getArtifactPath(final ArtifactCoords coords) {
        Path p = repoHome;
        final String[] groupParts = coords.getGroupId().split("\\.");
        for (String part : groupParts) {
            p = p.resolve(part);
        }
        p = p.resolve(coords.getArtifactId());
        p = p.resolve(coords.getVersion());
        final StringBuilder fileName = new StringBuilder();
        fileName.append(coords.getArtifactId()).append('-').append(coords.getVersion());
        final String classifier = coords.getClassifier();
        if(classifier != null && !classifier.isEmpty()) {
            fileName.append('-').append(classifier);
        }
        fileName.append('.').append(coords.getExtension());
        return p.resolve(fileName.toString());
    }

    @Override
    public String getHighestVersion(ArtifactCoords coords, String range) throws ArtifactException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
