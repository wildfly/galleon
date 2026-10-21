/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.featurepack.pkg.test;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.api.config.GalleonFeaturePackConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.api.test.PmInstallFeaturePackTestBase;
import org.jboss.galleon.api.test.util.fs.state.DirState;

/**
 *
 * @author Alexey Loubyansky
 */
public class IncludeNonDefaultPackageTestCase extends PmInstallFeaturePackTestBase {

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(LegacyGalleon1Universe.newFPID("org.pm.test:fp-install", "1", "1.0.0.Beta1"))
            .newPackage("a", true)
                .addDependency("d")
                .writeContent("a.txt", "a")
                .getFeaturePack()
            .newPackage("b")
                .addDependency("c")
                .writeContent("b/b.txt", "b")
                .getFeaturePack()
            .newPackage("c", true)
                .addDependency("d")
                .writeContent("c/c/c.txt", "c")
                .getFeaturePack()
            .newPackage("d")
                .writeContent("c/d.txt", "d")
                .getFeaturePack();
    }

    @Override
    protected GalleonFeaturePackConfig featurePackConfig() throws ProvisioningDescriptionException {
        return GalleonFeaturePackConfig
                .builder(LegacyGalleon1Universe.newFPID("org.pm.test:fp-install", "1", "1.0.0.Beta1").getLocation())
                .includePackage("b")
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(LegacyGalleon1Universe.newFPID("org.pm.test:fp-install", "1", "1.0.0.Beta1"))
                        .addPackage("a")
                        .addPackage("b")
                        .addPackage("c")
                        .addPackage("d")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("a.txt", "a")
                .addFile("b/b.txt", "b")
                .addFile("c/c/c.txt", "c")
                .addFile("c/d.txt", "d")
                .build();
    }
}
