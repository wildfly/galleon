/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.install.test;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.api.config.GalleonFeaturePackConfig;
import org.jboss.galleon.api.config.GalleonProvisioningConfig;
import org.jboss.galleon.api.test.util.fs.state.DirState;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
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
    protected GalleonProvisioningConfig initialState() throws ProvisioningException {
        return GalleonProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackLocation.fromString("prod1@" + getUniverseSpec() + ":1"))
                .build();
    }

    @Override
    protected GalleonFeaturePackConfig featurePackConfig() throws ProvisioningDescriptionException {
        return GalleonFeaturePackConfig.forLocation(FeaturePackLocation.fromString("prod2@" + getUniverseSpec() + ":1"));
    }

    @Override
    protected GalleonProvisioningConfig provisionedConfig() throws ProvisioningDescriptionException {
        return GalleonProvisioningConfig.builder()
                .addFeaturePackDep(GalleonFeaturePackConfig.builder(fp1)
                        .build())
                .addFeaturePackDep(GalleonFeaturePackConfig.builder(fp2)
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
