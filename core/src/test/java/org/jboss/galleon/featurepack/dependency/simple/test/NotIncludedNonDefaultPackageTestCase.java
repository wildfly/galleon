/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.featurepack.dependency.simple.test;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.PmInstallFeaturePackTestBase;
import org.jboss.galleon.test.util.fs.state.DirState;

/**
 *
 * @author Alexey Loubyansky
 */
public class NotIncludedNonDefaultPackageTestCase extends PmInstallFeaturePackTestBase {

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
            .newFeaturePack(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Alpha"))
                .addDependency(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "2", "2.0.0.Final").getLocation())
                .newPackage("main", true)
                    .addDependency("d")
                    .writeContent("f/p1/c.txt", "c")
                    .getFeaturePack()
                .newPackage("d")
                    .writeContent("f/p1/d.txt", "d")
                    .getFeaturePack()
                .getCreator()
            .newFeaturePack(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "2", "2.0.0.Final"))
                .newPackage("main", true)
                    .addDependency("b")
                    .writeContent("f/p2/a.txt", "a")
                    .getFeaturePack()
                .newPackage("b")
                    .writeContent("f/p2/b.txt", "b")
                    .getFeaturePack()
                .newPackage("c")
                    .writeContent("f/p2/c.txt", "c")
                    .getFeaturePack();
    }

    @Override
    protected FeaturePackConfig featurePackConfig()
            throws ProvisioningDescriptionException {
        return FeaturePackConfig.forLocation(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Alpha").getLocation());
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "2", "2.0.0.Final"))
                        .addPackage("main")
                        .addPackage("b")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Alpha"))
                        .addPackage("main")
                        .addPackage("d")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("f/p1/c.txt", "c")
                .addFile("f/p1/d.txt", "d")
                .addFile("f/p2/a.txt", "a")
                .addFile("f/p2/b.txt", "b")
                .build();
    }
}
