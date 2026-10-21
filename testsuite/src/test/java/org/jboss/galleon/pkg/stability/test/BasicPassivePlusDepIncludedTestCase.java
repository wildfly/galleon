/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.pkg.stability.test;

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
 * Stability on dependency shouldn't break passive logic.
 * @author jfdenise
 */
public class BasicPassivePlusDepIncludedTestCase extends ProvisionFromUniverseTestBase {

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
                // This dependency should be provisioned
                .addDependency(PackageDependencySpec.passive("p5", Constants.STABILITY_COMMUNITY))
                .getFeaturePack()
            .newPackage("p5")
                .addDependency("p6")
                .addDependency("p7")
                // Optional are not taken into account the optional deps. Validate that one with stability doesn't break this rule.
                .addDependency(PackageDependencySpec.optional("foo", Constants.STABILITY_EXPERIMENTAL))
                .getFeaturePack()
            .newPackage("p6")
                .getFeaturePack()
            .newPackage("p7")
                .getFeaturePack();
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addOption(ProvisioningOption.OPTIONAL_PACKAGES.getName(), Constants.PASSIVE_PLUS)
                .addOption(Constants.STABILITY_LEVEL, Constants.STABILITY_COMMUNITY)
                .addFeaturePackDep(FeaturePackConfig.builder(prod1)
                        .includePackage("p6")
                        .includePackage("p7")
                        .build())
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(prod1.getFPID())
                        .addPackage("p1")
                        .addPackage("p5")
                        .addPackage("p6")
                        .addPackage("p7")
                        .build())
                .build();
    }
}