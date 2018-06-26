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

package org.jboss.galleon.universe.maven;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.repo.RepositoryArtifactResolver;
import org.jboss.galleon.universe.Universe;
import org.jboss.galleon.universe.UniverseFactory;
import org.jboss.galleon.universe.maven.repo.MavenRepoManager;
import org.jboss.galleon.universe.maven.repo.SimplisticMavenRepoManager;

/**
 *
 * @author Alexey Loubyansky
 */
public class MavenUniverseFactory implements UniverseFactory {

    public static final String DEFAULT_REPO_ID = RepositoryArtifactResolver.ID_PREFIX + MavenUniverseConstants.MAVEN;
    public static final String ID = MavenUniverseConstants.MAVEN;

    /* (non-Javadoc)
     * @see org.jboss.galleon.universe.UniverseFactory#getRepoType()
     */
    @Override
    public String getFactoryId() {
        return ID;
    }

    @Override
    public String getRepositoryId() {
        return DEFAULT_REPO_ID;
    }

    /* (non-Javadoc)
     * @see org.jboss.galleon.universe.UniverseFactory#getUniverse(java.lang.String)
     */
    @Override
    public Universe<?> getUniverse(RepositoryArtifactResolver artifactResolver, String location) throws ProvisioningException {
        final MavenRepoManager repo;
        if(artifactResolver != null) {
            if(!(artifactResolver instanceof MavenRepoManager)) {
                throw new MavenUniverseException(artifactResolver.getClass().getName() + " is not an instance of " + MavenRepoManager.class.getName());
            }
            repo = (MavenRepoManager) artifactResolver;
        } else {
            repo = SimplisticMavenRepoManager.getInstance();
        }
        return new MavenUniverse(repo, MavenArtifact.fromString(location));
    }
}
