/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.maven.noloc.test;

import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.InstallFromUniverseTestBase;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.util.fs.state.DirState;

/**
 *
 * @author Alexey Loubyansky
 */
public class BasicMavenCoordinatesAsFplTestCase extends InstallFromUniverseTestBase  {

    private static final FeaturePackLocation fpl = FeaturePackLocation.fromString("org.jboss.galleon.test:galleon-test:1.0.0");

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(fpl.getFPID())
            .newPackage("ab", true)
                .writeContent("a.txt", "a")
                .writeContent("b/b.txt", "b")
                .getFeaturePack();
    }

    @Override
    protected FeaturePackConfig featurePackConfig() {
        return FeaturePackConfig.forLocation(fpl);
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(fpl.getFPID())
                        .addPackage("ab")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("a.txt", "a")
                .addFile("b/b.txt", "b")
                .build();
    }
}
