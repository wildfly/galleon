/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.layout.update.test;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.layout.FeaturePackUpdatePlan;
import org.jboss.galleon.layout.FeaturePackUpdatePlanTestBase;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;
import org.jboss.galleon.universe.MvnUniverse;

/**
 *
 * @author Alexey Loubyansky
 */
public class SpecificTransitiveUpdatePlanTestCase extends FeaturePackUpdatePlanTestBase {

    private FeaturePackLocation prodA100;
    private FeaturePackLocation prodA101;
    private FeaturePackLocation prodA200;

    private FeaturePackLocation prodB100;
    private FeaturePackLocation prodB101;

    private FeaturePackLocation prodC100;
    private FeaturePackLocation prodC101;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prodA", 2);
        universe.createProducer("prodB");
        universe.createProducer("prodC");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningDescriptionException {
        prodA100 = newFpl("prodA", "1", "1.0.0.Final");
        creator.newFeaturePack(prodA100.getFPID());

        prodA101 = newFpl("prodA", "1", "1.0.1.Final");
        creator.newFeaturePack(prodA101.getFPID());

        prodA200 = newFpl("prodA", "2", "2.0.0.Final");
        creator.newFeaturePack(prodA200.getFPID());

        prodB100 = newFpl("prodB", "1", "1.0.0.Final");
        creator.newFeaturePack(prodB100.getFPID()).addDependency(prodA100);

        prodB101 = newFpl("prodB", "1", "1.0.1.Final");
        creator.newFeaturePack(prodB101.getFPID());

        prodC100 = newFpl("prodC", "1", "1.0.0.Final");
        creator.newFeaturePack(prodC100.getFPID()).addDependency(prodB100);

        prodC101 = newFpl("prodC", "1", "1.0.1.Final");
        creator.newFeaturePack(prodC101.getFPID());
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(prodC100)
                .build();
    }

    @Override
    protected ProducerSpec[] checkUpdates() {
        return new ProducerSpec[] {prodA100.getProducer()};
    }

    @Override
    protected FeaturePackUpdatePlan[] expectedUpdatePlans() {
        return new FeaturePackUpdatePlan[] {
                FeaturePackUpdatePlan.request(prodA100, true).setNewLocation(prodA101).buildPlan()
        };
    }
}
