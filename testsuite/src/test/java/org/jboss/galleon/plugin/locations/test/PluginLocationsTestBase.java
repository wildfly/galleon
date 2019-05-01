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
package org.jboss.galleon.plugin.locations.test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.UUID;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.creator.FeaturePackBuilder;
import org.jboss.galleon.plugin.InstallPlugin;
import org.jboss.galleon.repo.RepositoryArtifactResolver;
import org.jboss.galleon.universe.ProvisionFromUniverseTestBase;
import org.jboss.galleon.universe.maven.MavenArtifact;
import org.jboss.galleon.universe.maven.repo.SimplisticMavenRepoManager;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class PluginLocationsTestBase extends ProvisionFromUniverseTestBase {

    private SimplisticMavenRepoManager repo;

    @Override
    protected RepositoryArtifactResolver initRepoManager(Path repoHome) {
        repo = SimplisticMavenRepoManager.getInstance(repoHome);
        return repo;
    }

    protected MavenArtifact installPluginArtifact(final MavenArtifact pluginArtifact, Class<?> pluginCls)
            throws ProvisioningException {
        final Path pluginJar = getTmpDir().resolve(UUID.randomUUID().toString());
        try {
            FeaturePackBuilder.createPluginJar(
                    Collections.singleton(pluginCls),
                    Collections.singletonMap(InstallPlugin.class.getName(),
                            Collections.singleton(pluginCls.getName())),
                    pluginJar);
        } catch (IOException e) {
            throw new ProvisioningException("Failed to create a plugin jar", e);
        }
        repo.install(pluginArtifact, pluginJar);
        return pluginArtifact;
    }

}
