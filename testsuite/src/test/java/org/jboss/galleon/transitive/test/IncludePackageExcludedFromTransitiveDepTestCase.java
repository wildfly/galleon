/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.transitive.test;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.util.fs.state.DirState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.ProvisionFromUniverseTestBase;

/**
 *
 * @author Alexey Loubyansky
 */
public class IncludePackageExcludedFromTransitiveDepTestCase extends ProvisionFromUniverseTestBase {

    private FeaturePackLocation fp1;
    private FeaturePackLocation fp2;
    private FeaturePackLocation fp3;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1");
        universe.createProducer("prod2");
        universe.createProducer("prod3");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {

        fp1 = newFpl("prod1", "1", "1.0.0.Final");
        fp2 = newFpl("prod2", "1", "1.0.0.Final");
        fp3 = newFpl("prod3", "1", "1.0.0.Final");

        creator.newFeaturePack()
        .setFPID(fp1.getFPID())
        .addDependency(FeaturePackConfig.builder(fp2)
                .setInheritPackages(false)
                .build())
        .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "fp1");

        creator.newFeaturePack()
        .setFPID(fp2.getFPID())
        .addDependency(fp3)
        .newPackage("p1", true)
                .writeContent("fp2/p1.txt", "fp2");

        creator.newFeaturePack()
        .setFPID(fp3.getFPID());

    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.transitiveBuilder(fp2)
                        .includePackage("p1")
                        .build())
                .addFeaturePackDep(fp1)
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(fp3.getFPID())
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(fp2.getFPID())
                        .addPackage("p1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(fp1.getFPID())
                        .addPackage("p1")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1/p1.txt", "fp1")
                .addFile("fp2/p1.txt", "fp2")
                .build();
    }
}