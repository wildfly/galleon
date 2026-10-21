/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.installation.pkgs;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.PmProvisionConfigTestBase;
import org.jboss.galleon.test.util.fs.state.DirState;

/**
 *
 * @author Alexey Loubyansky
 */
public class UserChosenPackagesOverrideDependencyOnOriginalSetTestCase extends PmProvisionConfigTestBase {

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
            .newFeaturePack(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Alpha"))
                .addDependency(FeaturePackConfig.forLocation(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "2", "2.0.0.Final").getLocation()))
                .newPackage("p1", true)
                    .writeContent("fp1/p1.txt", "p1")
                    .getFeaturePack()
                .getCreator()
            .newFeaturePack(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "2", "2.0.0.Final"))
                .newPackage("p1", true)
                    .addDependency("p2", true)
                    .writeContent("fp2/p1.txt", "p1")
                    .getFeaturePack()
                .newPackage("p2")
                    .writeContent("fp2/p2.txt", "p2")
                    .getFeaturePack()
                .newPackage("p3", true)
                    .writeContent("fp2/p3.txt", "p3")
                    .getFeaturePack();
    }

    @Override
    protected ProvisioningConfig provisioningConfig()
            throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(
                        FeaturePackConfig.forLocation(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Alpha").getLocation()))
                .addFeaturePackDep(
                        FeaturePackConfig
                                .builder(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "2", "2.0.0.Final").getLocation())
                                .excludePackage("p1")
                                .build())
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() {
        return ProvisionedState.builder()
                .addFeaturePack(
                        ProvisionedFeaturePack.builder(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Alpha"))
                        .addPackage("p1")
                        .build())
                .addFeaturePack(
                        ProvisionedFeaturePack.builder(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "2", "2.0.0.Final"))
                        .addPackage("p3")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1/p1.txt", "p1")
                .addFile("fp2/p3.txt", "p3")
                .build();
    }
}
