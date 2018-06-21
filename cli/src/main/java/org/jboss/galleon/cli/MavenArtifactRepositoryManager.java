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

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import org.jboss.galleon.cli.config.mvn.MavenSettings;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import org.eclipse.aether.RepositoryListener;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.installation.InstallRequest;
import org.eclipse.aether.installation.InstallationException;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.jboss.galleon.ArtifactException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.config.mvn.MavenConfig;
import org.jboss.galleon.maven.plugin.FpMavenErrors;
import org.jboss.galleon.universe.maven.MavenArtifact;
import org.jboss.galleon.universe.maven.MavenErrors;
import org.jboss.galleon.universe.maven.MavenUniverseException;
import org.jboss.galleon.universe.maven.repo.MavenArtifactVersion;
import org.jboss.galleon.universe.maven.repo.MavenArtifactVersionRange;
import org.jboss.galleon.universe.maven.repo.MavenArtifactVersionRangeParser;
import org.jboss.galleon.universe.maven.repo.MavenRepoManager;

/**
 *
 * @author Alexey Loubyansky
 */
public class MavenArtifactRepositoryManager implements MavenRepoManager {

    private static final MavenArtifactVersionRangeParser versionRangeParser = new MavenArtifactVersionRangeParser();
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

    private Path doResolve(ArtifactRequest request, String coords) throws ArtifactException {
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

    private void doInstall(InstallRequest request) throws InstallationException, ArtifactException {
        repoSystem.install(getSettings().getSession(), request);
    }

    private String dogetHighestVersion(Artifact artifact, String coords) throws ArtifactException {
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

    @Override
    public String getRepositoryId() {
        return MavenRepoManager.REPOSITORY_ID;
    }

    @Override
    public Path resolve(String location) throws ProvisioningException {
        return MavenRepoManager.super.resolve(location);
    }

    @Override
    public void resolve(MavenArtifact artifact) throws MavenUniverseException {
        if (artifact.isResolved()) {
            throw new MavenUniverseException("Artifact is already resolved");
        }
        final ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getClassifier(),
                artifact.getExtension(), artifact.getVersion()));
        try {
            Path path = doResolve(request, artifact.getCoordsAsString());
            artifact.setPath(path);
        } catch (ArtifactException ex) {
            throw new MavenUniverseException(ex.getLocalizedMessage());
        }
    }

    @Override
    public void resolveLatestVersion(MavenArtifact mavenArtifact, String lowestQualifier) throws MavenUniverseException {
        Artifact artifact = new DefaultArtifact(mavenArtifact.getGroupId(),
                mavenArtifact.getArtifactId(), mavenArtifact.getExtension(), mavenArtifact.getVersionRange());
        try {
            String version = dogetHighestVersion(artifact, mavenArtifact.getCoordsAsString());
            mavenArtifact.setVersion(version);
            resolve(mavenArtifact);
        } catch (ArtifactException ex) {
            throw new MavenUniverseException(ex.getLocalizedMessage());
        }
    }

    @Override
    public String getLatestVersion(MavenArtifact artifact, String lowestQualifier) throws MavenUniverseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void install(MavenArtifact artifact, Path path) throws MavenUniverseException {
        if (artifact.isResolved()) {
            throw new MavenUniverseException("Artifact is already associated with a path " + path);
        }
        final InstallRequest request = new InstallRequest();
        request.addArtifact(new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getClassifier(),
                artifact.getExtension(), artifact.getVersion(), Collections.emptyMap(), path.toFile()));
        try {
            doInstall(request);
            artifact.setPath(getArtifactPath(artifact));
        } catch (ArtifactException | InstallationException ex) {
            throw new MavenUniverseException(ex.getLocalizedMessage());
        }
    }

    private Path getArtifactPath(MavenArtifact artifact) throws MavenUniverseException, ArtifactException {
        if (artifact.getGroupId() == null) {
            MavenErrors.missingGroupId();
        }
        Path p = getSettings().getSession().getLocalRepository().getBasedir().toPath();
        final String[] groupParts = artifact.getGroupId().split("\\.");
        for (String part : groupParts) {
            p = p.resolve(part);
        }
        final String artifactFileName = artifact.getArtifactFileName();
        return p.resolve(artifact.getArtifactId()).resolve(artifact.getVersion()).resolve(artifactFileName);
    }

    @Override
    public boolean isResolved(MavenArtifact artifact) throws MavenUniverseException {
        if (artifact.isResolved()) {
            return true;
        }
        try {
            Path path = getArtifactPath(artifact);
            return Files.exists(path);
        } catch (ArtifactException e) {
            throw new MavenUniverseException(e.getLocalizedMessage());
        }
    }

    @Override
    public boolean isLatestVersionResolved(MavenArtifact artifact, String lowestQualifier) throws MavenUniverseException {
        if (artifact.isResolved()) {
            return true;
        }
        try {
            Path path = resolveLatestVersionDir(artifact, lowestQualifier);
            return Files.exists(path);
        } catch (ArtifactException e) {
            throw new MavenUniverseException(e.getLocalizedMessage(), e);
        }
    }

    private Path resolveLatestVersionDir(MavenArtifact artifact, String lowestQualifier) throws MavenUniverseException, ArtifactException {
        if (artifact.getGroupId() == null) {
            MavenErrors.missingGroupId();
        }
        if (artifact.getArtifactId() == null) {
            MavenErrors.missingArtifactId();
        }
        if (artifact.getVersionRange() == null) {
            throw new MavenUniverseException("Version range is missing for " + artifact.getCoordsAsString());
        }
        Path repoHome = getSettings().getSession().getLocalRepository().getBasedir().toPath();
        Path artifactDir = repoHome;
        final String[] groupParts = artifact.getGroupId().split("\\.");
        for (String part : groupParts) {
            artifactDir = artifactDir.resolve(part);
        }
        artifactDir = artifactDir.resolve(artifact.getArtifactId());
        if (!Files.exists(artifactDir)) {
            throw MavenErrors.artifactNotFound(artifact, repoHome);
        }
        final MavenArtifactVersionRange range = versionRangeParser.parseRange(artifact.getVersionRange());
        if (lowestQualifier == null) {
            lowestQualifier = "";
        }
        Path latestDir = null;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(artifactDir)) {
            MavenArtifactVersion latest = null;
            for (Path versionDir : stream) {
                final MavenArtifactVersion next = new MavenArtifactVersion(versionDir.getFileName().toString());
                if (!range.includesVersion(next) || !next.isQualifierHigher(lowestQualifier, true)) {
                    continue;
                }
                if (latest == null || latest.compareTo(next) <= 0) {
                    latest = next;
                    latestDir = versionDir;
                }
            }
        } catch (Exception e) {
            throw new MavenUniverseException("Failed to determine the latest version of " + artifact.getCoordsAsString(), e);
        }
        if (latestDir == null) {
            throw new MavenUniverseException("Failed to determine the latest version of " + artifact.getCoordsAsString());
        }
        return latestDir;
    }

}
