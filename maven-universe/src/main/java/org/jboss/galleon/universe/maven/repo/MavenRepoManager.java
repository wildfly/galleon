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

package org.jboss.galleon.universe.maven.repo;

import org.jboss.galleon.model.GaecRange;
import org.jboss.galleon.model.Gaecvp;
import org.jboss.galleon.repo.RepositoryArtifactResolver;
import org.jboss.galleon.universe.maven.MavenUniverseConstants;
import org.jboss.galleon.universe.maven.MavenUniverseException;

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

//    @Override
//    default Path resolve(String location) throws ProvisioningException {
//        final MavenArtifact artifact = MavenArtifact.fromString(location);
//        resolve(artifact);
//        return artifact.getPath();
//    }

//    boolean isResolved(MavenArtifact artifact) throws MavenUniverseException;
//
//    boolean isLatestVersionResolved(MavenArtifact artifact, String lowestQualifier) throws MavenUniverseException;

    default Gaecvp resolveLatestVersion(GaecRange artifact) throws MavenUniverseException {
        return resolveLatestVersion(artifact, null);
    }

    Gaecvp resolveLatestVersion(GaecRange artifact, String lowestQualifier) throws MavenUniverseException;

    default String getLatestFinalVersion(GaecRange artifact) throws MavenUniverseException {
        return getLatestVersion(artifact, null);
    }

    String getLatestVersion(GaecRange artifact) throws MavenUniverseException;

    String getLatestVersion(GaecRange artifact, String lowestQualifier) throws MavenUniverseException;

}
