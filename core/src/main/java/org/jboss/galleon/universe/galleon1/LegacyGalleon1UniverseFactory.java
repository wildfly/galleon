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

package org.jboss.galleon.universe.galleon1;

import org.jboss.galleon.ArtifactRepositoryManager;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.repo.RepositoryArtifactResolver;
import org.jboss.galleon.universe.Universe;
import org.jboss.galleon.universe.UniverseFactory;

/**
 *
 * @author Alexey Loubyansky
 */
public class LegacyGalleon1UniverseFactory implements UniverseFactory {

    public static final String ID = "galleon1";
    public static final String DEFAULT_REPO_ID = ArtifactRepositoryManager.REPOSITORY_ID;

    /* (non-Javadoc)
     * @see org.jboss.galleon.universe.UniverseFactory#getFactoryId()
     */
    @Override
    public String getFactoryId() {
        return ID;
    }

    /* (non-Javadoc)
     * @see org.jboss.galleon.universe.UniverseFactory#getRepositoryId()
     */
    @Override
    public String getRepositoryId() {
        return DEFAULT_REPO_ID;
    }

    /* (non-Javadoc)
     * @see org.jboss.galleon.universe.UniverseFactory#getUniverse(org.jboss.galleon.repomanager.RepositoryArtifactResolver, java.lang.String)
     */
    @Override
    public Universe<?> getUniverse(RepositoryArtifactResolver artifactResolver, String location) throws ProvisioningException {
        return new LegacyGalleon1Universe(artifactResolver);
    }
}
