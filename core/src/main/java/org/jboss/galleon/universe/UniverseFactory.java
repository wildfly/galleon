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

package org.jboss.galleon.universe;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.repo.RepositoryArtifactResolver;

/**
 *
 * @author Alexey Loubyansky
 */
public interface UniverseFactory {

    String getFactoryId();

    String getRepositoryId();

    /**
     * This method will be looking for the latest locally available version of the universe
     * artifact. If no version is available locally, it will fallback to resolving the latest
     * available in remote repositories.
     *
     * @param artifactResolver  artifact resolver
     * @param location  location of the universe artifact
     * @return  instance of the universe
     * @throws ProvisioningException  in case of a failure
     */
    default Universe<?> getUniverse(RepositoryArtifactResolver artifactResolver, String location) throws ProvisioningException {
        return getUniverse(artifactResolver, location, false);
    }

    Universe<?> getUniverse(RepositoryArtifactResolver artifactResolver, String location, boolean absoluteLatest) throws ProvisioningException;
}
