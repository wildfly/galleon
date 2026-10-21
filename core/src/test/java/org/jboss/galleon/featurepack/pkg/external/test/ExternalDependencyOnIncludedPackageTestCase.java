/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.featurepack.pkg.external.test;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
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
public class ExternalDependencyOnIncludedPackageTestCase extends PmProvisionConfigTestBase {

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(LegacyGalleon1Universe.newFPID("org.pm.test:fp1", "1", "1.0.0.Final"))
            .addDependency("fp2-dep", FeaturePackConfig.builder(LegacyGalleon1Universe.newFPID("org.pm.test:fp2", "1", "1.0.0.Final").getLocation())
                    .build())
            .newPackage("p1", true)
                .addDependency("fp2-dep", "p1")
                .writeContent("fp1/p1.txt", "p1")
                .getFeaturePack()
            .getCreator()
        .newFeaturePack(LegacyGalleon1Universe.newFPID("org.pm.test:fp2", "1", "1.0.0.Final"))
            .newPackage("p1", true)
                .writeContent("fp2/p1.txt", "p1")
                .getFeaturePack();
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(LegacyGalleon1Universe.newFPID("org.pm.test:fp1", "1", "1.0.0.Final").getLocation())
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(LegacyGalleon1Universe.newFPID("org.pm.test:fp2", "1", "1.0.0.Final"))
                        .addPackage("p1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(LegacyGalleon1Universe.newFPID("org.pm.test:fp1", "1", "1.0.0.Final"))
                        .addPackage("p1")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1/p1.txt", "p1")
                .addFile("fp2/p1.txt", "p1")
                .build();
    }
}
