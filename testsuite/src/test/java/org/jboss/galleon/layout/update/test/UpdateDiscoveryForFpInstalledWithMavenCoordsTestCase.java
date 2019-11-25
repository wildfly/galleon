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
public class UpdateDiscoveryForFpInstalledWithMavenCoordsTestCase extends FeaturePackUpdatePlanTestBase {

    private FeaturePackLocation prodA100;
    private FeaturePackLocation prodA101;
    private FeaturePackLocation prodA102;
    private FeaturePackLocation prodA200;

    private FeaturePackLocation prodB100;
    private FeaturePackLocation prodB101;
    private FeaturePackLocation prodB102;
    private FeaturePackLocation prodB200;

    private FeaturePackLocation prodC100;
    private FeaturePackLocation prodC101;
    private FeaturePackLocation prodC102;
    private FeaturePackLocation prodC200;

    private FeaturePackLocation prodD100;
    private FeaturePackLocation prodD101;
    private FeaturePackLocation prodD102;
    private FeaturePackLocation prodD200;

    @Override
    protected boolean checkTransitive() {
        return true;
    }

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prodA", 2);
        universe.createProducer("prodB", 2);
        universe.createProducer("prodC", 2);
        universe.createProducer("prodD", 2);
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningDescriptionException {

        // prodA
        prodA100 = newFpl("prodA", "1", "1.0.0.Final");
        creator.newFeaturePack(prodA100.getFPID());

        prodA101 = newFpl("prodA", "1", "1.0.1.Final");
        creator.newFeaturePack(prodA101.getFPID());

        prodA102 = newFpl("prodA", "1", "1.0.2.Final");
        creator.newFeaturePack(prodA102.getFPID());

        prodA200 = newFpl("prodA", "2", "2.0.0.Final");
        creator.newFeaturePack(prodA200.getFPID());

        // prodB
        prodB100 = newFpl("prodB", "1", "1.0.0.Final");
        creator.newFeaturePack(prodB100.getFPID())
        .addDependency(toMavenCoordsFpl(prodA100));

        prodB101 = newFpl("prodB", "1", "1.0.1.Final");
        creator.newFeaturePack(prodB101.getFPID())
        .addDependency(toMavenCoordsFpl(prodA101));

        prodB102 = newFpl("prodB", "1", "1.0.2.Final");
        creator.newFeaturePack(prodB102.getFPID())
        .addDependency(toMavenCoordsFpl(prodA102));

        prodB200 = newFpl("prodB", "2", "2.0.0.Final");
        creator.newFeaturePack(prodB200.getFPID())
        .addDependency(toMavenCoordsFpl(prodA200));

        // prodC
        prodC100 = newFpl("prodC", "1", "1.0.0.Final");
        creator.newFeaturePack(prodC100.getFPID())
        .addDependency(toMavenCoordsFpl(prodB100));

        prodC101 = newFpl("prodC", "1", "1.0.1.Final");
        creator.newFeaturePack(prodC101.getFPID())
        .addDependency(toMavenCoordsFpl(prodB101));

        prodC102 = newFpl("prodC", "1", "1.0.2.Final");
        creator.newFeaturePack(prodC102.getFPID())
        .addDependency(toMavenCoordsFpl(prodB102));

        prodC200 = newFpl("prodC", "2", "2.0.0.Final");
        creator.newFeaturePack(prodC200.getFPID())
        .addDependency(toMavenCoordsFpl(prodB200));

        // prodD
        prodD100 = newFpl("prodD", "1", "1.0.0.Final");
        creator.newFeaturePack(prodD100.getFPID())
        .addDependency(toMavenCoordsFpl(prodC100));

        prodD101 = newFpl("prodD", "1", "1.0.1.Final");
        creator.newFeaturePack(prodD101.getFPID())
        .addDependency(toMavenCoordsFpl(prodC101));

        prodD102 = newFpl("prodD", "1", "1.0.2.Final");
        creator.newFeaturePack(prodD102.getFPID())
        .addDependency(toMavenCoordsFpl(prodC102));

        prodD200 = newFpl("prodD", "2", "2.0.0.Final");
        creator.newFeaturePack(prodD200.getFPID())
        .addDependency(toMavenCoordsFpl(prodC200));
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(toMavenCoordsFpl(prodC101))
                .addFeaturePackDep(toMavenCoordsFpl(prodD100))
                .build();
    }

    @Override
    protected FeaturePackUpdatePlan[] expectedUpdatePlans() {
        return new FeaturePackUpdatePlan[] {
                FeaturePackUpdatePlan.request(prodC101).setNewLocation(prodC102).buildPlan(),
                FeaturePackUpdatePlan.request(prodD100).setNewLocation(prodD102).buildPlan(),
                FeaturePackUpdatePlan.request(prodA101, true).setNewLocation(prodA102).buildPlan(),
                FeaturePackUpdatePlan.request(prodB101, true).setNewLocation(prodB102).buildPlan()
                };
    }
}
