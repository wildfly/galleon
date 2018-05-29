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
package org.jboss.galleon;

import java.nio.file.Path;

import org.jboss.galleon.repo.RepositoryArtifactResolver;

/**
 *
 * @author Alexey Loubyansky
 */
public interface ArtifactRepositoryManager extends RepositoryArtifactResolver {

    String REPOSITORY_ID = RepositoryArtifactResolver.ID_PREFIX + "maven";

    @Override
    default String getRepositoryId() {
        return REPOSITORY_ID;
    }

    @Override
    default Path resolve(String location) throws ProvisioningException {
        return resolve(ArtifactCoords.fromString(location));
    }

    Path resolve(ArtifactCoords coords) throws ArtifactException;
    void install(ArtifactCoords coords, Path artifact) throws ArtifactException;
    void deploy(ArtifactCoords coords, Path artifact) throws ArtifactException;
    String getHighestVersion(ArtifactCoords coords, String range) throws ArtifactException;
}
