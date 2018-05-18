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
package org.jboss.galleon.cli;

import org.jboss.galleon.cli.config.mvn.MavenSettings;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.aether.RepositoryListener;

import org.eclipse.aether.RepositorySystem;
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
import org.jboss.galleon.cli.config.mvn.MavenConfig;
import org.jboss.galleon.maven.plugin.FpMavenErrors;

/**
 *
 * @author Alexey Loubyansky
 */
public class MavenArtifactRepositoryManager implements ArtifactRepositoryManager {

    public static final String DEFAULT_REPOSITORY_TYPE = "default";
    private final RepositorySystem repoSystem;
    private final MavenConfig config;
    private final RepositoryListener listener;

    private MavenSettings mavenSettings;
    private boolean commandStarted;

    MavenArtifactRepositoryManager(MavenConfig config, RepositoryListener listener) {
        repoSystem = Util.newRepositorySystem();
        this.config = config;
        this.listener = listener;
    }

    void commandStart() {
        commandStarted = true;
        mavenSettings = null;
    }

    void commandEnd() {
        commandStarted = false;
    }

    private MavenSettings getSettings() throws ArtifactException {
        if (commandStarted) { // reuse settings.
            if (mavenSettings == null) {
                mavenSettings = config.buildSettings(repoSystem, listener);
            }
        } else {
            mavenSettings = config.buildSettings(repoSystem, listener);
        }
        return mavenSettings;
    }

    @Override
    public Path resolve(ArtifactCoords coords) throws ArtifactException {
        final ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(new DefaultArtifact(coords.getGroupId(), coords.getArtifactId(), coords.getClassifier(),
                coords.getExtension(), coords.getVersion()));
        request.setRepositories(getSettings().getRepositories());

        final ArtifactResult result;
        try {
            result = repoSystem.resolveArtifact(getSettings().getSession(), request);
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
            repoSystem.install(getSettings().getSession(), request);
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
            repoSystem.deploy(getSettings().getSession(), request);
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
        rangeRequest.setRepositories(getSettings().getRepositories());
        VersionRangeResult rangeResult;
        try {
            rangeResult = repoSystem.resolveVersionRange(getSettings().getSession(), rangeRequest);
        } catch (VersionRangeResolutionException ex) {
            throw new ArtifactException(ex.getLocalizedMessage(), ex);
        }
        String version = null;
        if (rangeResult != null && rangeResult.getHighestVersion() != null) {
            version = rangeResult.getHighestVersion().toString();
        }
        if (version == null) {
            throw new ArtifactException("No version retrieved for " + coords);
        }
        return version;
    }

}
