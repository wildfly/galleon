/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.maven.noloc.test;

import org.jboss.galleon.ProvisioningException;
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
public class ProvisionUsingMavenCoordinatesInPlaceOfFplTestCase extends ProvisionFromUniverseTestBase {

    private FeaturePackLocation prod1;
    private FeaturePackLocation prod2;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1");
        universe.createProducer("prod2");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {

        prod1 = newFpl("prod1", "1", "1.0.0.Final");
        prod2 = newFpl("prod2", "1", "1.0.0.Final");

        creator.newFeaturePack()
            .setFPID(prod1.getFPID())
            .newPackage("p1", true);

        creator.newFeaturePack()
            .setFPID(prod2.getFPID())
            .newPackage("p1", true);

    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackLocation.fromString("org.jboss.galleon.universe.test.test-universe.prod1:prod1-feature-pack:1.0.0.Final"))
                .addFeaturePackDep(prod2)
                .build();
    }

    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(prod1)
                .addFeaturePackDep(prod2)
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(prod1.getFPID())
                        .addPackage("p1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(prod2.getFPID())
                        .addPackage("p1")
                        .build())
                .build();
    }
}