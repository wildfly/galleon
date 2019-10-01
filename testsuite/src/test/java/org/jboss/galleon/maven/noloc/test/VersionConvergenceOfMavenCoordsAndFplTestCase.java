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
public class VersionConvergenceOfMavenCoordsAndFplTestCase extends ProvisionFromUniverseTestBase {

    private FeaturePackLocation prod100;
    private FeaturePackLocation prod200;
    private FeaturePackLocation prod201;
    private FeaturePackLocation prod300;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1");
        universe.createProducer("prod2");
        universe.createProducer("prod3");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {

        prod100 = newFpl("prod1", "1", "1.0.0");
        prod200 = newFpl("prod2", "1", "1.0.0");
        prod201 = newFpl("prod2", "1", "1.0.1");
        prod300 = newFpl("prod3", "1", "1.0.0");

        creator.newFeaturePack()
            .setFPID(prod100.getFPID())
            .addDependency(prod200)
            .newPackage("p1", true);

        creator.newFeaturePack()
            .setFPID(prod300.getFPID())
            .addDependency(FeaturePackLocation.fromString("org.jboss.galleon.universe.test.test-universe.prod2:prod2-feature-pack:1.0.1"))
            .newPackage("p1", true);

        creator.newFeaturePack()
            .setFPID(prod200.getFPID())
            .newPackage("p1", true);

        creator.newFeaturePack()
            .setFPID(prod201.getFPID())
            .newPackage("p1", true);

        creator.install();
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder().addFeaturePackDep(prod100).addFeaturePackDep(prod300).build();
    }

    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.forLocation(prod100))
                .addFeaturePackDep(FeaturePackConfig.forLocation(prod300))
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(prod200.getFPID())
                        .addPackage("p1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(prod100.getFPID())
                        .addPackage("p1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(prod300.getFPID())
                        .addPackage("p1")
                        .build())
                .build();
    }
}