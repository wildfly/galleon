/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.universe.maven.repo;

import org.jboss.galleon.universe.maven.MavenUniverseConstants;
import org.jboss.galleon.universe.maven.MavenUniverseException;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.repo.RepositoryArtifactResolver;
import org.jboss.galleon.universe.maven.MavenArtifact;

/**
 *
 * @author Alexey Loubyansky
 */
public interface MavenRepoManager extends RepositoryArtifactResolver {

    String REPOSITORY_ID = RepositoryArtifactResolver.ID_PREFIX + MavenUniverseConstants.MAVEN;

    @Override
    default String getRepositoryId() {
        return REPOSITORY_ID;
    }

    @Override
    default Path resolve(String location) throws ProvisioningException {
        final MavenArtifact artifact = MavenArtifact.fromString(location);
        resolve(artifact);
        return artifact.getPath();
    }

    void resolve(MavenArtifact artifact) throws MavenUniverseException;

    default void resolveAll(Collection<MavenArtifact> artifacts) throws MavenUniverseException {
        for (MavenArtifact artifact : artifacts) {
            resolve(artifact);
        }
    }

    boolean isResolved(MavenArtifact artifact) throws MavenUniverseException;

    boolean isLatestVersionResolved(MavenArtifact artifact, String lowestQualifier) throws MavenUniverseException;

    default void resolveLatestVersion(MavenArtifact artifact) throws MavenUniverseException {
        resolveLatestVersion(artifact, false);
    }

    default void resolveLatestVersion(MavenArtifact artifact, boolean locallyAvailable) throws MavenUniverseException {
        resolveLatestVersion(artifact, null, locallyAvailable);
    }

    default void resolveLatestVersion(MavenArtifact artifact, String lowestQualifier) throws MavenUniverseException {
        resolveLatestVersion(artifact, lowestQualifier, false);
    }

    void resolveLatestVersion(MavenArtifact artifact, String lowestQualifier, Pattern includeVersion, Pattern excludeVersion) throws MavenUniverseException;

    void resolveLatestVersion(MavenArtifact artifact, String lowestQualifier, boolean locallyAvailable) throws MavenUniverseException;

    default String getLatestFinalVersion(MavenArtifact artifact) throws MavenUniverseException {
        return getLatestVersion(artifact, null);
    }

    String getLatestVersion(MavenArtifact artifact) throws MavenUniverseException;

    String getLatestVersion(MavenArtifact artifact, String lowestQualifier) throws MavenUniverseException;

    String getLatestVersion(MavenArtifact artifact, String lowestQualifier, Pattern includeVersion, Pattern excludeVersion) throws MavenUniverseException;

    List<String> getAllVersions(MavenArtifact artifact) throws MavenUniverseException;

    List<String> getAllVersions(MavenArtifact artifact, Pattern includeVersion, Pattern excludeVersion) throws MavenUniverseException;

    void install(MavenArtifact artifact, Path path) throws MavenUniverseException;
}
