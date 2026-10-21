/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.install.test;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.util.fs.state.DirState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.InstallFromUniverseTestBase;
import org.jboss.galleon.universe.MvnUniverse;

/**
 *
 * @author Alexey Loubyansky
 */
public class InstallLatestOverConfigTestCase extends InstallFromUniverseTestBase {

    private FeaturePackLocation fp1;
    private FeaturePackLocation fp2;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1");
        universe.createProducer("prod2");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        fp1 = newFpl("prod1", "1", "1.0.0.Final");
        creator.newFeaturePack(fp1.getFPID())
            .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "fp1 p1");

        fp2 = newFpl("prod2", "1", "1.0.0.Patch1.Final");
        creator.newFeaturePack(fp2.getFPID())
            .newPackage("p1", true)
                .writeContent("fp2/p1.txt", "fp2 p1");

    }

    @Override
    protected ProvisioningConfig initialState() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackLocation.fromString("prod1@" + getUniverseSpec() + ":1"))
                .build();
    }

    @Override
    protected FeaturePackConfig featurePackConfig() throws ProvisioningDescriptionException {
        return FeaturePackConfig.forLocation(FeaturePackLocation.fromString("prod2@" + getUniverseSpec() + ":1"));
    }

    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(fp1)
                        .build())
                .addFeaturePackDep(FeaturePackConfig.builder(fp2)
                        .build())
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(fp1.getFPID())
                        .addPackage("p1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(fp2.getFPID())
                        .addPackage("p1")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1/p1.txt", "fp1 p1")
                .addFile("fp2/p1.txt", "fp2 p1")
                .build();
    }
}
