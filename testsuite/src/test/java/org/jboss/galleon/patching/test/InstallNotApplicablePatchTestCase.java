/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.patching.test;

import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.InstallFromUniverseTestBase;
import org.jboss.galleon.universe.MvnUniverse;

/**
 *
 * @author Alexey Loubyansky
 */
public class InstallNotApplicablePatchTestCase extends InstallFromUniverseTestBase {

    private FeaturePackLocation fp1;
    private FeaturePackLocation fp101;
    private FeaturePackLocation fp1Patch1;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        fp1 = newFpl("prod1", "1", "1.0.0.Final");
        creator.newFeaturePack(fp1.getFPID())
            .newPackage("p1", true)
                .addDependency("p2")
                .addDependency("p3", true)
                .writeContent("fp1/p1.txt", "fp1 p1")
                .getFeaturePack()
            .newPackage("p2")
                .writeContent("fp1/p2.txt", "fp1 p2")
                .getFeaturePack()
            .newPackage("p3")
                .writeContent("fp1/p3.txt", "fp1 p3")
                .getFeaturePack()
            .newPackage("p4")
                .writeContent("fp1/p4.txt", "fp1 p4")
                .getFeaturePack()
            .newPackage("p5")
                .writeContent("fp1/p5.txt", "fp1 p5");

        fp101 = newFpl("prod1", "1", "1.0.1.Final");
        fp1Patch1 = newFpl("prod1", "1", "1.0.0.Patch1.Final");
        creator.newFeaturePack(fp1Patch1.getFPID())
            .setPatchFor(newFpl("prod1", "1", "1.0.1.Final").getFPID())
            .newPackage("p1", true)
                .addDependency("p2")
                .addDependency("p3")
                .addDependency("p4", true)
                .getFeaturePack()
            .newPackage("p2")
                .writeContent("fp1/p2.txt", "fp1 p2 patch1")
                .getFeaturePack()
            .newPackage("p3")
                .writeContent("fp1/p3.txt", "fp1 p3 patch1");

    }

    @Override
    protected ProvisioningConfig initialState() throws ProvisioningException {
        return ProvisioningConfig.builder().addFeaturePackDep(fp1).build();
    }

    @Override
    protected FeaturePackConfig featurePackConfig() throws ProvisioningDescriptionException {
        return FeaturePackConfig.forLocation(fp1Patch1);
    }

    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(fp1)
                        .addPatch(fp1Patch1.getFPID())
                        .build())
                .build();
    }

    @Override
    protected String[] pmErrors() {
        return new String[] {Errors.patchNotApplicable(fp1Patch1.getFPID(), fp101.getFPID())};
    }
}
