/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
