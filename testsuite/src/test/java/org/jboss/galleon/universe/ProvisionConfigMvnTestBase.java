/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.universe;

import java.nio.file.Path;

import org.jboss.galleon.repo.RepositoryArtifactResolver;
import org.jboss.galleon.test.PmProvisionConfigTestBase;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.maven.MavenArtifact;
import org.jboss.galleon.universe.maven.MavenUniverseFactory;
import org.jboss.galleon.universe.maven.repo.MavenRepoManager;
import org.jboss.galleon.universe.maven.repo.SimplisticMavenRepoManager;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class ProvisionConfigMvnTestBase extends PmProvisionConfigTestBase {

    @Override
    protected RepositoryArtifactResolver initRepoManager(Path repoHome) {
        return SimplisticMavenRepoManager.getInstance(repoHome);
    }

    protected MvnUniverse newMvnUniverse(String name) {
        return MvnUniverse.getInstance(name, (MavenRepoManager) repo);
    }

    protected FPID mvnFPID(FeaturePackLocation fpl, MavenArtifact universeArtifact) {
        return new FeaturePackLocation(
                universeArtifact == null ? null
                        : new UniverseSpec(MavenUniverseFactory.ID, universeArtifact.getCoordsAsString()),
                fpl.getProducerName(), fpl.getChannelName(), fpl.getFrequency(), fpl.getBuild()).getFPID();
    }
}
