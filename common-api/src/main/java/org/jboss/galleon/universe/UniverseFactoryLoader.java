/*
 * Copyright 2016-2023 Red Hat, Inc. and/or its affiliates
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.repo.RepositoryArtifactResolver;
import org.jboss.galleon.util.CollectionUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class UniverseFactoryLoader {
    private static final UniverseFactoryLoader INSTANCE = new UniverseFactoryLoader();

    public static UniverseFactoryLoader getInstance() {
        return INSTANCE;
    }

    private final Map<String, UniverseFactory> factories;
    private Map<String, RepositoryArtifactResolver> artifactResolvers = Collections.emptyMap();

    private UniverseFactoryLoader() {
        final ServiceLoader<UniverseFactory> loader = ServiceLoader.load(UniverseFactory.class);
        Map<String, UniverseFactory> factories = Collections.emptyMap();
        for(UniverseFactory factory : loader) {
            if(factories.isEmpty()) {
                factories = Collections.singletonMap(factory.getFactoryId(), factory);
                continue;
            }
            if(factories.containsKey(factory.getFactoryId())) {
                throw new IllegalStateException("Only one universe factory is allowed per repository type "
                        + factory.getFactoryId() + " but found " + factory + " and " + factories.get(factory.getFactoryId()));
            }
            if(factories.size() == 1) {
                final HashMap<String, UniverseFactory> tmp = new HashMap<>(2);
                tmp.putAll(factories);
                factories = tmp;
            }
            factories.put(factory.getFactoryId(), factory);
        }
        this.factories = CollectionUtils.unmodifiable(factories);
    }

    public Set<String> getFactories() {
        return this.factories.keySet();
    }

    public UniverseFactoryLoader addArtifactResolver(RepositoryArtifactResolver artifactResolver) {
        return addArtifactResolver(artifactResolver.getRepositoryId(), artifactResolver);
    }

    public UniverseFactoryLoader addArtifactResolver(String repoId, RepositoryArtifactResolver artifactResolver) {
        artifactResolvers = CollectionUtils.put(artifactResolvers, repoId, artifactResolver);
        return this;
    }

    public Universe<?> getUniverse(String factoryId, String location) throws ProvisioningException {
        final UniverseFactory factory = getUniverseFactory(factoryId);
        return factory.getUniverse(getArtifactResolver(factory.getRepositoryId()), location);
    }

    public Universe<?> getUniverse(String factoryId, String location, String repoId) throws ProvisioningException {
        return getUniverseFactory(factoryId).getUniverse(getArtifactResolver(repoId), location);
    }

    public Universe<?> getUniverse(String factoryId, String location, RepositoryArtifactResolver artifactResolver) throws ProvisioningException {
        final UniverseFactory factory = getUniverseFactory(factoryId);
        if(artifactResolver == null) {
            throw new ProvisioningException("artifactResolver is null");
        }
        return factory.getUniverse(artifactResolver, location);
    }

    /**
     * This method will be looking for the latest locally available version of the universe
     * artifact. If no version is available locally, it will fallback to resolving the latest
     * available in remote repositories.
     *
     * @param universeSpec  universe spec
     * @return  resolved universe
     * @throws ProvisioningException  in case of a failure
     */
    public Universe<?> getUniverse(UniverseSpec universeSpec) throws ProvisioningException {
        return getUniverse(universeSpec, false);
    }

    public Universe<?> getUniverse(UniverseSpec universeSpec, boolean absoluteLatest) throws ProvisioningException {
        final UniverseFactory factory = getUniverseFactory(universeSpec.getFactory());
        return factory.getUniverse(getArtifactResolver(factory.getRepositoryId()), universeSpec.getLocation(), absoluteLatest);
    }

    private UniverseFactory getUniverseFactory(String factoryId) throws ProvisioningException {
        final UniverseFactory factory = factories.get(factoryId);
        if(factory == null) {
            throw new ProvisioningException("Universe factory " + factoryId + " has not been installed");
        }
        return factory;
    }

    private RepositoryArtifactResolver getArtifactResolver(String repositoryId) throws ProvisioningException {
        final RepositoryArtifactResolver artifactResolver = artifactResolvers.get(repositoryId);
        if(artifactResolver == null) {
            throw new ProvisioningException("Repository artifact resolver " + repositoryId + " has not been configured");
        }
        return artifactResolver;
    }

    RepositoryArtifactResolver getArtifactResolverOrNull(String repoId) {
        return artifactResolvers.get(repoId);
    }
}
