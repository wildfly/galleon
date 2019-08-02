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
package org.jboss.galleon.universe.maven.repo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.universe.maven.MavenUniverseException;
import org.jboss.galleon.universe.maven.MavenArtifact;
import org.jboss.galleon.universe.maven.MavenErrors;
import org.jboss.galleon.util.IoUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class SimplisticMavenRepoManager extends LocalArtifactVersionRangeResolver implements MavenRepoManager {

    public static final String REPOSITORY_ID = MavenRepoManager.REPOSITORY_ID;
    public static final String SIMPLISTIC_MAVEN_REPO_HOME = "simplistic.maven.repo.home";

    public static SimplisticMavenRepoManager getInstance() {
        final String prop = System.getProperty(SIMPLISTIC_MAVEN_REPO_HOME);
        return getInstance(prop == null ? Paths.get(System.getProperty("user.home")).resolve(".m2") : Paths.get(prop));
    }

    public static SimplisticMavenRepoManager getInstance(Path repoHome) {
        return new SimplisticMavenRepoManager(repoHome, null);
    }

    public static SimplisticMavenRepoManager getInstance(Path repoHome, MavenRepoManager fallback) {
        return new SimplisticMavenRepoManager(repoHome, fallback);
    }

    private final MavenRepoManager fallback;
    private boolean locallyAvailableVersionRangesPreferred = true;

    private SimplisticMavenRepoManager(Path repoHome, MavenRepoManager fallback) {
        super(repoHome);
        this.fallback = fallback;
    }

    public boolean isLocallyAvailableVersionRangesPreferred() {
        return locallyAvailableVersionRangesPreferred;
    }

    public void setLocallyAvailableVersionRangesPreferred(boolean locallyAvailableVersionRangesPreferred) {
        this.locallyAvailableVersionRangesPreferred = locallyAvailableVersionRangesPreferred;
    }

    @Override
    public String getRepositoryId() {
        return REPOSITORY_ID;
    }

    @Override
    public Path resolve(String location) throws ProvisioningException {
        return MavenRepoManager.super.resolve(location);
    }

    @Override
    public void resolve(MavenArtifact artifact) throws MavenUniverseException {
        if(artifact.isResolved()) {
            throw new MavenUniverseException("Artifact is already resolved");
        }
        Path path = null;
        try {
            path = getArtifactPath(artifact);
            if (!Files.exists(path)) {
                throw new MavenUniverseException(pathDoesNotExist(artifact, path));
            }
            artifact.setPath(path);
            return;
        } catch (MavenUniverseException e) {
            if (fallback == null) {
                throw e;
            }
        }
        try {
            fallback.resolve(artifact);
        } catch(MavenUniverseException e) {
            throw new MavenUniverseException(pathDoesNotExist(artifact, path), e);
        }
    }

    @Override
    public void resolveLatestVersion(MavenArtifact artifact, String lowestQualifier, boolean locally) throws MavenUniverseException {
        if(locallyAvailableVersionRangesPreferred) {
            try {
                super.resolveLatestVersion(artifact, lowestQualifier);
                return;
            } catch (MavenUniverseException e) {
                if (fallback == null) {
                    throw e;
                }
            }
            fallback.resolveLatestVersion(artifact, lowestQualifier, locally);
            return;
        }
        if(locally) {
            super.resolveLatestVersion(artifact, lowestQualifier);
            return;
        }
        if(fallback == null) {
            throw new MavenUniverseException(MavenErrors.failedToResolveLatestVersion(artifact.getCoordsAsString()));
        }

        fallback.resolveLatestVersion(artifact, lowestQualifier);
    }

    @Override
    public void resolveLatestVersion(MavenArtifact artifact, String lowestQualifier) throws MavenUniverseException {
        resolveLatestVersion(artifact, lowestQualifier, null, null);
    }

    @Override
    public void resolveLatestVersion(MavenArtifact artifact, String lowestQualifier, Pattern includeVersion, Pattern excludeVersion) throws MavenUniverseException {
        try {
            super.resolveLatestVersion(artifact, lowestQualifier, includeVersion, excludeVersion);
            return;
        } catch(MavenUniverseException e) {
            if(fallback == null) {
                throw e;
            }
        }
        fallback.resolveLatestVersion(artifact, lowestQualifier, includeVersion, excludeVersion);
    }

    @Override
    public String getLatestVersion(MavenArtifact artifact, String lowestQualifier) throws MavenUniverseException {
        try {
            return super.getLatestVersion(artifact, lowestQualifier);
        } catch(MavenUniverseException e) {
            if(fallback == null) {
                throw e;
            }
        }
        return fallback.getLatestVersion(artifact, lowestQualifier);
    }

    @Override
    public void install(MavenArtifact artifact, Path path) throws MavenUniverseException {
        if(artifact.isResolved()) {
            throw new MavenUniverseException("Artifact is already associated with a path " + path);
        }
        final Path targetPath = getArtifactPath(artifact);
        try {
            IoUtils.copy(path, targetPath);
        } catch (IOException e) {
            throw new MavenUniverseException("Failed to install " + artifact.getCoordsAsString(), e);
        }
        artifact.setPath(targetPath);
    }

    private Path getArtifactPath(MavenArtifact artifact) throws MavenUniverseException {
        if(artifact.getGroupId() == null) {
            MavenErrors.missingGroupId();
        }
        Path p = repoHome;
        final String[] groupParts = artifact.getGroupId().split("\\.");
        for (String part : groupParts) {
            p = p.resolve(part);
        }
        final String artifactFileName = artifact.getArtifactFileName();
        return p.resolve(artifact.getArtifactId()).resolve(artifact.getVersion()).resolve(artifactFileName);
    }

    @Override
    public boolean isResolved(MavenArtifact artifact) throws MavenUniverseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isLatestVersionResolved(MavenArtifact artifact, String lowestQualifier) throws MavenUniverseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getLatestVersion(MavenArtifact artifact) throws MavenUniverseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<String> getAllVersions(MavenArtifact artifact) throws MavenUniverseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<String> getAllVersions(MavenArtifact artifact, Pattern includeVersion, Pattern excludeVersion) throws MavenUniverseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
