/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.universe;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.impl.ProvisioningUtil;
import org.jboss.galleon.repo.RepositoryArtifactResolver;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class UniverseResolverBuilder<T extends UniverseResolverBuilder<?>> {

    protected UniverseFactoryLoader ufl;
    protected Map<FPID, Path> localFeaturePacks = new HashMap<>();

    @SuppressWarnings("unchecked")
    public T setUniverseFactoryLoader(UniverseFactoryLoader ufl) throws ProvisioningException {
        if(this.ufl != null) {
            throw new ProvisioningException("Universe factory loader has already been initialized");
        }
        this.ufl = ufl;
        return (T) this;
    }

    public T addArtifactResolver(RepositoryArtifactResolver artifactResolver) throws ProvisioningException {
        return addArtifactResolver(artifactResolver.getRepositoryId(), artifactResolver);
    }

    @SuppressWarnings("unchecked")
    public T addArtifactResolver(String repoId, RepositoryArtifactResolver artifactResolver) throws ProvisioningException {
        getUfl().addArtifactResolver(repoId, artifactResolver);
        return (T) this;
    }

    protected UniverseResolver buildUniverseResolver() throws ProvisioningException {
        return new UniverseResolver(this);
    }

    protected UniverseFactoryLoader getUfl() throws ProvisioningException {
        if(ufl == null) {
            ufl = UniverseFactoryLoader.getInstance();
        }
        return ufl;
    }

    @SuppressWarnings("unchecked")
    public T addLocalFeaturePack(Path path) throws ProvisioningException {
        if (path == null) {
            return (T) this;
        }
        try {
            getLocalFeaturePacks().put(ProvisioningUtil.getFeaturePackProducer(path), path);

            return (T) this;
        } catch (Exception e) {
            throw new ProvisioningException(e);
        }
    }

    protected Map<FPID, Path> getLocalFeaturePacks() {
        return localFeaturePacks;
    }
}
