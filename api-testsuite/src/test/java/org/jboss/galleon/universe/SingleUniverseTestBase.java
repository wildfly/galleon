/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.universe;

import java.nio.file.Path;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.repo.RepositoryArtifactResolver;
import org.jboss.galleon.api.test.PmTestBase;
import org.jboss.galleon.universe.maven.MavenArtifact;
import org.jboss.galleon.universe.maven.MavenUniverseFactory;
import org.jboss.galleon.universe.maven.repo.MavenRepoManager;
import org.jboss.galleon.universe.maven.repo.SimplisticMavenRepoManager;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class SingleUniverseTestBase extends PmTestBase {

    protected MvnUniverse mvnUniverse;
    protected String universeName = "test-universe";
    protected MavenArtifact universeArtifact;
    private UniverseSpec universeSpec;

    @Override
    protected RepositoryArtifactResolver initRepoManager(Path repoHome) {
        return SimplisticMavenRepoManager.getInstance(repoHome);
    }

    protected UniverseSpec getUniverseSpec() {
        if(universeSpec == null) {
            universeSpec = new UniverseSpec(MavenUniverseFactory.ID, universeArtifact.getGroupId() + ':' + universeArtifact.getArtifactId());
        }
        return universeSpec;
    }

    protected FeaturePackLocation newProducerFpl(String producer) {
        return new FeaturePackLocation(getUniverseSpec(), producer, null, null, null);
    }

    protected FeaturePackLocation newProducerBuildFpl(String producer, String build) {
        return new FeaturePackLocation(getUniverseSpec(), producer, null, null, build);
    }

    protected FeaturePackLocation newFpl(String producer, String channel) {
        return new FeaturePackLocation(getUniverseSpec(), producer, channel, null, null);
    }

    protected FeaturePackLocation newFpl(String producer, String channel, String build) {
        return new FeaturePackLocation(getUniverseSpec(), producer, channel, null, build);
    }

    protected FeaturePackLocation newFpl(String producer, String channel, String frequency, String build) {
        return new FeaturePackLocation(getUniverseSpec(), producer, channel, frequency, build);
    }

    protected FeaturePackLocation newFpl(String producer, String universe, String channel, String frequency, String build) {
        return new FeaturePackLocation(new UniverseSpec(universe, null), producer, channel, frequency, build);
    }

    protected FeaturePackLocation toMavenCoordsFpl(FeaturePackLocation fpl) {
        return fpl.isMavenCoordinates() ? fpl : newMavenCoordsFpl(fpl.getProducerName(), fpl.getBuild());
    }

    protected FeaturePackLocation newMavenCoordsFpl(String producer, String build) {
        return FeaturePackLocation.fromString(universeArtifact.getGroupId() + '.' + universeName + '.' + producer + ":" + producer + "-feature-pack" + ":" + build);
    }

    protected abstract void createProducers(MvnUniverse universe) throws ProvisioningException;

    @Override
    protected void doBefore() throws Exception {
        mvnUniverse = MvnUniverse.getInstance(universeName, (MavenRepoManager) repo);
        createProducers(mvnUniverse);
        universeArtifact = mvnUniverse.install();
        super.doBefore();
    }
}
