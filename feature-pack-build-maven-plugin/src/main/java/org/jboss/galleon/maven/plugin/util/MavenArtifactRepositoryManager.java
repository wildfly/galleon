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
package org.jboss.galleon.maven.plugin.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.deployment.DeploymentException;
import org.eclipse.aether.installation.InstallRequest;
import org.eclipse.aether.installation.InstallationException;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.ArtifactException;
import org.jboss.galleon.ArtifactRepositoryManager;
import org.jboss.galleon.maven.plugin.FpMavenErrors;

/**
 *
 * @author Emmanuel Hugonnet (c) 2017 Red Hat, inc.
 */
public class MavenArtifactRepositoryManager implements ArtifactRepositoryManager {

    private final RepositorySystem repoSystem;
    private final RepositorySystemSession session;

    public MavenArtifactRepositoryManager(final RepositorySystem repoSystem, final RepositorySystemSession repoSession){
        this.repoSystem = repoSystem;
        this.session = repoSession;
    }

    @Override
    public Path resolve(ArtifactCoords coords) throws ArtifactException {
        final ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(new DefaultArtifact(coords.getGroupId(), coords.getArtifactId(), coords.getClassifier(),
                coords.getExtension(), coords.getVersion()));
        final ArtifactResult result;
        try {
            result = repoSystem.resolveArtifact(session, request);
        } catch (org.eclipse.aether.resolution.ArtifactResolutionException e) {
            throw new ArtifactException(FpMavenErrors.artifactResolution(coords), e);
        }
        if (!result.isResolved()) {
            throw new ArtifactException(FpMavenErrors.artifactResolution(coords));
        }
        if (result.isMissing()) {
            throw new ArtifactException(FpMavenErrors.artifactMissing(coords));
        }
        return Paths.get(result.getArtifact().getFile().toURI());
    }

    @Override
    public void install(ArtifactCoords coords, Path file) throws ArtifactException {
        final InstallRequest request = new InstallRequest();
        request.addArtifact(new DefaultArtifact(coords.getGroupId(), coords.getArtifactId(), coords.getClassifier(),
                coords.getExtension(), coords.getVersion(), Collections.emptyMap(), file.toFile()));
        try {
            repoSystem.install(session, request);
        } catch (InstallationException ex) {
            Logger.getLogger(MavenArtifactRepositoryManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void deploy(ArtifactCoords coords, Path file) throws ArtifactException {
        final DeployRequest request = new DeployRequest();
        request.addArtifact(new DefaultArtifact(coords.getGroupId(), coords.getArtifactId(), coords.getClassifier(),
                coords.getExtension(), coords.getVersion(), Collections.emptyMap(), file.toFile()));
        try {
            repoSystem.deploy(session, request);
        } catch (DeploymentException ex) {
            Logger.getLogger(MavenArtifactRepositoryManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getHighestVersion(ArtifactCoords coords, String range) throws ArtifactException {
        Artifact artifact = new DefaultArtifact(coords.getGroupId(),
                coords.getArtifactId(), coords.getExtension(), range);
        VersionRangeRequest rangeRequest = new VersionRangeRequest();
        rangeRequest.setArtifact(artifact);
        VersionRangeResult rangeResult;
        try {
            rangeResult = repoSystem.resolveVersionRange(session, rangeRequest);
        } catch (VersionRangeResolutionException ex) {
            throw new ArtifactException(ex.getLocalizedMessage(), ex);
        }
        String version = null;
        if (rangeResult != null && rangeResult.getHighestVersion() != null) {
            version = rangeResult.getHighestVersion().toString();
        }
        return version;
    }

}
