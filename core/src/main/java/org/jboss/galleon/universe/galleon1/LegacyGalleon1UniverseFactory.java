/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.universe.galleon1;

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
    public static final String DEFAULT_REPO_ID = RepositoryArtifactResolver.ID_PREFIX + "maven";

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

    @Override
    public Universe<?> getUniverse(RepositoryArtifactResolver artifactResolver, String location, boolean absoluteLatest)
            throws ProvisioningException {
        return new LegacyGalleon1Universe(artifactResolver);
    }
}
