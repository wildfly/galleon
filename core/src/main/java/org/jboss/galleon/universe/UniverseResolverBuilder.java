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

package org.jboss.galleon.universe;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.layout.FeaturePackDescription;
import org.jboss.galleon.layout.FeaturePackDescriber;
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
            FeaturePackDescription featurePackLayout = FeaturePackDescriber.describeFeaturePackZip(path);
            getLocalFeaturePacks().put(featurePackLayout.getFPID(), path);

            return (T) this;
        } catch (IOException e) {
            throw new ProvisioningException(e);
        }
    }

    protected Map<FPID, Path> getLocalFeaturePacks() {
        return localFeaturePacks;
    }
}
