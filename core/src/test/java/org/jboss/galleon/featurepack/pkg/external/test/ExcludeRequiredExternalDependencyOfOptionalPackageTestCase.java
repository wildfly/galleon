/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.featurepack.pkg.external.test;

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
public class ExcludeRequiredExternalDependencyOfOptionalPackageTestCase extends PmInstallFeaturePackTestBase {

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
            .newFeaturePack(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Alpha"))
                .addDependency("fp2-dep", FeaturePackConfig.builder(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "1", "1.0.0.Final").getLocation())
                    .excludePackage("c")
                    .build())
                .newPackage("a", true)
                    .addDependency("fp2-dep", "b", true)
                    .writeContent("a.txt", "a")
                    .getFeaturePack()
                .getCreator()
            .newFeaturePack(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "1", "1.0.0.Final"))
                .newPackage("b")
                    .addDependency("c")
                    .writeContent("b.txt", "b")
                    .getFeaturePack()
                .newPackage("c")
                    .writeContent("c.txt", "c")
                    .getFeaturePack();
    }

    @Override
    protected FeaturePackConfig featurePackConfig()
            throws ProvisioningDescriptionException {
        return FeaturePackConfig
                .builder(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Alpha").getLocation())
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "1", "1.0.0.Final")).build())
                .addFeaturePack(ProvisionedFeaturePack.builder(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Alpha"))
                        .addPackage("a")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("a.txt", "a")
                .build();
    }
}
