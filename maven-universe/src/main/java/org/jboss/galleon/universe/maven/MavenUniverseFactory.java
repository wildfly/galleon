/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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

    @Override
    public Universe<?> getUniverse(RepositoryArtifactResolver artifactResolver, String location, boolean absoluteLatest) throws ProvisioningException {
        final MavenRepoManager repo;
        if(artifactResolver != null) {
            if(!(artifactResolver instanceof MavenRepoManager)) {
                throw new MavenUniverseException(artifactResolver.getClass().getName() + " is not an instance of " + MavenRepoManager.class.getName());
            }
            repo = (MavenRepoManager) artifactResolver;
        } else {
            repo = SimplisticMavenRepoManager.getInstance();
        }
        if(location == null) {
            return new MvnNoLocUniverse(repo);
        }
        try {
            return new MavenUniverse(repo, MavenArtifact.fromString(location), absoluteLatest);
        } catch (MavenUniverseException e) {
            throw new MavenUniverseException(MavenErrors.msgUniverseNotFound(location), e);
        }
    }
}
