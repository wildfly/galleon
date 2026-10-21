/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.installation.universe.names;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.PmInstallFeaturePackTestBase;
import org.jboss.galleon.test.util.fs.state.DirState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;

/**
 *
 * @author Alexey Loubyansky
 */
public class InstallFpUsingDefaultUniverseTestCase extends PmInstallFeaturePackTestBase {

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator.newFeaturePack(FeaturePackLocation.fromString("galleon.test:fp1@galleon1:1#1.0.0.Final").getFPID())
            .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "fp1 p1")
            .getFeaturePack();
    }

    @Override
    protected ProvisioningManager getPm() throws ProvisioningException {
        ProvisioningManager pm = super.getPm();
        pm.setDefaultUniverse(LegacyGalleon1Universe.getUniverseSpec());
        return pm;
    }

    @Override
    protected FeaturePackConfig featurePackConfig() throws ProvisioningDescriptionException {
        return FeaturePackConfig.forLocation(FeaturePackLocation.fromString("galleon.test:fp1:1#1.0.0.Final"));
    }

    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder()
                .setDefaultUniverse("galleon1", null)
                .addFeaturePackDep(FeaturePackConfig.forLocation(FeaturePackLocation.fromString("galleon.test:fp1@galleon1:1#1.0.0.Final")))
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(FeaturePackLocation.fromString("galleon.test:fp1@galleon1:1#1.0.0.Final").getFPID())
                        .addPackage("p1")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1/p1.txt", "fp1 p1")
                .build();
    }
}
