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
public class SimpleTransitiveUpdatePlanForFpInstalledUsingGavInPlaceOfFplTestCase extends FeaturePackUpdatePlanTestBase {

    private FeaturePackLocation prodA100;
    private FeaturePackLocation prodA101;
    private FeaturePackLocation prodA200;

    private FeaturePackLocation prodB100;

    private FeaturePackLocation prodC100;
    private FeaturePackLocation prodC101;

    private FeaturePackLocation prodD100;
    private FeaturePackLocation prodD101;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prodA", 2);
        universe.createProducer("prodB");
        universe.createProducer("prodC");
        universe.createProducer("prodD");
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
        creator.newFeaturePack(prodB100.getFPID()).addDependency(newMavenCoordsFpl("prodA", "1.0.0.Final"));

        prodC100 = newFpl("prodC", "1", "1.0.0.Final");
        creator.newFeaturePack(prodC100.getFPID()).addDependency(newMavenCoordsFpl("prodB", "1.0.0.Final"));
        prodC101 = newFpl("prodC", "1", "1.0.1.Final");
        creator.newFeaturePack(prodC101.getFPID());

        prodD100 = newFpl("prodD", "1", "1.0.0.Final");
        creator.newFeaturePack(prodD100.getFPID());
        prodD101 = newFpl("prodD", "1", "1.0.1.Final");
        creator.newFeaturePack(prodD101.getFPID());
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(newMavenCoordsFpl("prodC", "1.0.0.Final"))
                .addFeaturePackDep(newMavenCoordsFpl("prodD", "1.0.0.Final"))
                .build();
    }

    @Override
    protected boolean checkTransitive() {
        return true;
    }

    @Override
    protected FeaturePackUpdatePlan[] expectedUpdatePlans() {
        return new FeaturePackUpdatePlan[] {
                FeaturePackUpdatePlan.request(prodC100).setNewLocation(prodC101).buildPlan(),
                FeaturePackUpdatePlan.request(prodD100).setNewLocation(prodD101).buildPlan(),
                FeaturePackUpdatePlan.request(prodA100, true).setNewLocation(prodA101).buildPlan()
        };
    }
}
