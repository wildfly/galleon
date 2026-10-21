/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.pkg.passive.test;

import org.jboss.galleon.Constants;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningOption;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.spec.PackageDependencySpec;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.ProvisionFromUniverseTestBase;

/**
 *
 * @author Alexey Loubyansky
 */
public class PassiveDepWithNoDepsIncludedTestCase extends ProvisionFromUniverseTestBase {

    private FeaturePackLocation prod1;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {

        prod1 = newFpl("prod1", "1", "1.0.0.Final");

        creator.newFeaturePack()
            .setFPID(prod1.getFPID())
            .newPackage("p1", true)
                .addDependency(PackageDependencySpec.optional("p2"))
                .addDependency(PackageDependencySpec.passive("p5"))
                .getFeaturePack()
            .newPackage("p2")
                .addDependency("p3")
                .addDependency("p4")
                .getFeaturePack()
            .newPackage("p3")
                .getFeaturePack()
            .newPackage("p4")
                .getFeaturePack()
            .newPackage("p5");

    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addOption(ProvisioningOption.OPTIONAL_PACKAGES.getName(), Constants.PASSIVE)
                .addFeaturePackDep(FeaturePackConfig.builder(prod1)
                        .includePackage("p3")
                        .includePackage("p4")
                        .build())
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(prod1.getFPID())
                        .addPackage("p1")
                        .addPackage("p3")
                        .addPackage("p4")
                        .addPackage("p5")
                        .build())
                .build();
    }
}