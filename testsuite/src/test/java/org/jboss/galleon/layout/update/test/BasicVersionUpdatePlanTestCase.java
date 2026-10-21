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
import org.jboss.galleon.universe.MvnUniverse;

/**
 *
 * @author Alexey Loubyansky
 */
public class BasicVersionUpdatePlanTestCase extends FeaturePackUpdatePlanTestBase {

    private FeaturePackLocation fp100;
    private FeaturePackLocation fp101;
    private FeaturePackLocation fp102;
    private FeaturePackLocation fp200;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1", 2);
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningDescriptionException {
        fp100 = newFpl("prod1", "1", "1.0.0.Final");
        creator.newFeaturePack(fp100.getFPID());

        fp101 = newFpl("prod1", "1", "1.0.1.Final");
        creator.newFeaturePack(fp101.getFPID());

        fp102 = newFpl("prod1", "1", "1.0.2.Final");
        creator.newFeaturePack(fp102.getFPID());

        fp200 = newFpl("prod1", "2", "2.0.0.Final");
        creator.newFeaturePack(fp200.getFPID());
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(fp100)
                .build();
    }

    @Override
    protected FeaturePackUpdatePlan[] expectedUpdatePlans() {
        return new FeaturePackUpdatePlan[] {
                FeaturePackUpdatePlan.request(fp100).setNewLocation(fp102).buildPlan()
                };
    }
}
