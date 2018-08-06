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
import java.util.Collections;
import java.util.List;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.installation.InstallRequest;
import org.eclipse.aether.installation.InstallResult;
import org.eclipse.aether.installation.InstallationException;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.version.Version;
import org.jboss.galleon.maven.plugin.FpMavenErrors;
import org.jboss.galleon.model.Gaec;
import org.jboss.galleon.model.GaecRange;
import org.jboss.galleon.model.Gaecv;
import org.jboss.galleon.model.Gaecvp;
import org.jboss.galleon.universe.maven.MavenErrors;
import org.jboss.galleon.universe.maven.MavenLatestVersionNotAvailableException;
import org.jboss.galleon.universe.maven.MavenUniverseException;
import org.jboss.galleon.universe.maven.repo.MavenArtifactVersion;
import org.jboss.galleon.universe.maven.repo.MavenRepoManager;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractMavenArtifactRepositoryManager implements MavenRepoManager {

    //private static final MavenArtifactVersionRangeParser versionRangeParser = new MavenArtifactVersionRangeParser();

    private final RepositorySystem repoSystem;

    public AbstractMavenArtifactRepositoryManager(final RepositorySystem repoSystem) {
        this.repoSystem = repoSystem;
    }

    protected abstract RepositorySystemSession getSession() throws MavenUniverseException;

    protected abstract List<RemoteRepository> getRepositories() throws MavenUniverseException;

    protected RepositorySystem getRepositorySystem() {
        return repoSystem;
    }

    @Override
    public Gaecvp resolve(Gaecv artifact) throws MavenUniverseException {
        final Gaec gaec = artifact.getGaec();

        final ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(new DefaultArtifact(gaec.getGroupId(), gaec.getArtifactId(), gaec.getClassifier(),
                gaec.getExtension(), artifact.getVersion()));
        request.setRepositories(getRepositories());

        final ArtifactResult result;
        try {
            result = repoSystem.resolveArtifact(getSession(), request);
        } catch (Exception e) {
            throw new MavenUniverseException(FpMavenErrors.artifactResolution(artifact.toString()), e);
        }
        if (!result.isResolved()) {
            throw new MavenUniverseException(FpMavenErrors.artifactResolution(artifact.toString()));
        }
        if (result.isMissing()) {
            throw new MavenUniverseException(FpMavenErrors.artifactMissing(artifact.toString()));
        }
        return new Gaecvp(artifact, result.getArtifact().getFile().toPath());
    }

    @Override
    public Gaecvp resolveLatestVersion(GaecRange gaecRange, String lowestQualifier) throws MavenUniverseException {
        final String version = getLatestVersion(gaecRange, lowestQualifier);
        return resolve(new Gaecv(gaecRange.getGaec(), version));
    }

    private VersionRangeResult getVersionRange(Artifact artifact) throws MavenUniverseException {
        VersionRangeRequest rangeRequest = new VersionRangeRequest();
        rangeRequest.setArtifact(artifact);
        rangeRequest.setRepositories(getRepositories());

        VersionRangeResult rangeResult;
        try {
            rangeResult = repoSystem.resolveVersionRange(getSession(), rangeRequest);
        } catch (VersionRangeResolutionException ex) {
            throw new MavenUniverseException(ex.getLocalizedMessage(), ex);
        }
        return rangeResult;
    }

    private static MavenArtifactVersion resolveLatest(VersionRangeResult rangeResult, String lowestQualifier) throws MavenUniverseException {
        if (lowestQualifier != null) {
            return MavenArtifactVersion.getLatest(rangeResult.getVersions(), lowestQualifier);
        } else {
            MavenArtifactVersion latestRelease = null;
            for (Version version : rangeResult.getVersions()) {
                MavenArtifactVersion next = new MavenArtifactVersion(version.toString());
                if (latestRelease == null || next.compareTo(latestRelease) > 0) {
                    latestRelease = next;
                }
            }
            return latestRelease;
        }
    }


    @Override
    public String getLatestVersion(GaecRange gaecRange, String lowestQualifier) throws MavenUniverseException {
        final Gaec gaec = gaecRange.getGaec();
        Artifact artifact = new DefaultArtifact(gaec.getGroupId(),
                gaec.getArtifactId(), gaec.getClassifier(), gaec.getExtension(), gaecRange.getVersionRange());
        VersionRangeResult rangeResult = getVersionRange(artifact);
        final MavenArtifactVersion latest = rangeResult == null ? null : resolveLatest(rangeResult, lowestQualifier);
        if (latest == null) {
            throw new MavenLatestVersionNotAvailableException(MavenErrors.failedToResolveLatestVersion(gaecRange.toString()));
        }
        return latest.toString();
    }

    @Override
    public String getLatestVersion(GaecRange gaecRange) throws MavenUniverseException {
        return getLatestVersion(gaecRange, null);
    }

    @Override
    public Gaecvp install(Gaecv coords, Path path) throws MavenUniverseException {
        final Gaec gaec = coords.getGaec();
        final InstallRequest request = new InstallRequest();
        request.addArtifact(new DefaultArtifact(gaec.getGroupId(), gaec.getArtifactId(), gaec.getClassifier(),
                gaec.getExtension(), coords.getVersion(), Collections.emptyMap(), path.toFile()));
        try {
            InstallResult result = repoSystem.install(getSession(), request);
            final Path installedPath = result.getArtifacts().iterator().next().getFile().toPath();
            return new Gaecvp(coords, installedPath);
        } catch (InstallationException ex) {
            throw new MavenUniverseException("Failed to install " + coords.toString(), ex);
        }
    }

//    @Override
//    public boolean isResolved(MavenArtifact artifact) throws MavenUniverseException {
//        if (artifact.isResolved()) {
//            return true;
//        }
//        Path path = getArtifactPath(artifact);
//        return Files.exists(path);
//    }
//
//    private Path getArtifactPath(MavenArtifact artifact) throws MavenUniverseException {
//        if (artifact.getGroupId() == null) {
//            MavenErrors.missingGroupId();
//        }
//        Path p = getSession().getLocalRepository().getBasedir().toPath();
//        final String[] groupParts = artifact.getGroupId().split("\\.");
//        for (String part : groupParts) {
//            p = p.resolve(part);
//        }
//        final String artifactFileName = artifact.getArtifactFileName();
//        return p.resolve(artifact.getArtifactId()).resolve(artifact.getVersion()).resolve(artifactFileName);
//    }

//    @Override
//    public boolean isLatestVersionResolved(MavenArtifact artifact, String lowestQualifier) throws MavenUniverseException {
//        if (artifact.isResolved()) {
//            return true;
//        }
//        Path path = resolveLatestVersionDir(artifact, lowestQualifier);
//        return Files.exists(path);
//    }
//
//    private Path resolveLatestVersionDir(MavenArtifact artifact, String lowestQualifier) throws MavenUniverseException {
//        if (artifact.getGroupId() == null) {
//            MavenErrors.missingGroupId();
//        }
//        if (artifact.getArtifactId() == null) {
//            MavenErrors.missingArtifactId();
//        }
//        if (artifact.getVersionRange() == null) {
//            throw new MavenUniverseException("Version range is missing for " + artifact.getCoordsAsString());
//        }
//        Path repoHome = getSession().getLocalRepository().getBasedir().toPath();
//        Path artifactDir = repoHome;
//        final String[] groupParts = artifact.getGroupId().split("\\.");
//        for (String part : groupParts) {
//            artifactDir = artifactDir.resolve(part);
//        }
//        artifactDir = artifactDir.resolve(artifact.getArtifactId());
//        if (!Files.exists(artifactDir)) {
//            throw MavenErrors.artifactNotFound(artifact, repoHome);
//        }
//        final MavenArtifactVersionRange range = versionRangeParser.parseRange(artifact.getVersionRange());
//        if (lowestQualifier == null) {
//            lowestQualifier = "";
//        }
//        Path latestDir = null;
//        try (DirectoryStream<Path> stream = Files.newDirectoryStream(artifactDir)) {
//            MavenArtifactVersion latest = null;
//            for (Path versionDir : stream) {
//                final MavenArtifactVersion next = new MavenArtifactVersion(versionDir.getFileName().toString());
//                if (!range.includesVersion(next) || !next.isQualifierHigher(lowestQualifier, true)) {
//                    continue;
//                }
//                if (latest == null || latest.compareTo(next) <= 0) {
//                    latest = next;
//                    latestDir = versionDir;
//                }
//            }
//        } catch (Exception e) {
//            throw new MavenUniverseException("Failed to determine the latest version of " + artifact.getCoordsAsString(), e);
//        }
//        if (latestDir == null) {
//            throw new MavenUniverseException("Failed to determine the latest version of " + artifact.getCoordsAsString());
//        }
//        return latestDir;
//    }
}
