/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
