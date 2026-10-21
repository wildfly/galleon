/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.maven.noloc.test;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.ProvisionFromUniverseTestBase;

/**
 *
 * @author Alexey Loubyansky
 */
public class OverrideFplVersionOfTransitiveDepWithGavTestCase extends ProvisionFromUniverseTestBase {

    private FeaturePackLocation prod1;
    private FeaturePackLocation prod2;
    private FeaturePackLocation prod21;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1");
        universe.createProducer("prod2");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {

        prod1 = newFpl("prod1", "1", "1.0.0.Final");
        prod2 = newFpl("prod2", "1", "1.0.0.Final");
        prod21 = newFpl("prod2", "1", "1.0.1.Final");

        creator.newFeaturePack()
            .setFPID(prod1.getFPID())
            .addDependency(prod2)
            .newPackage("p1", true);

        creator.newFeaturePack()
            .setFPID(prod2.getFPID())
            .newPackage("p1", true);

        creator.newFeaturePack()
            .setFPID(prod21.getFPID())
            .newPackage("p1", true);
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.forTransitiveDep(toMavenCoordsFpl(prod21)))
                .addFeaturePackDep(FeaturePackConfig.forLocation(toMavenCoordsFpl(prod1)))
                .build();
    }

    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.forTransitiveDep(prod21))
                .addFeaturePackDep(FeaturePackConfig.forLocation(prod1))
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(prod21.getFPID())
                        .addPackage("p1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(prod1.getFPID())
                        .addPackage("p1")
                        .build())
                .build();
    }
}