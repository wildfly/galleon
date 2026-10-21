/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.featurepack.dependency.override.test;

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
public class OverrideDependencyCustomSetWithOriginalTestCase extends PmProvisionConfigTestBase {

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
            .newFeaturePack(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Alpha"))
                .addDependency(FeaturePackConfig.forLocation(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "2", "2.0.0.Final").getLocation()))
                .addDependency(FeaturePackConfig.forLocation(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp3", "2", "2.0.0.Final").getLocation()))
                .newPackage("p1", true)
                    .writeContent("fp1/p1.txt", "p1")
                    .getFeaturePack()
                .getCreator()
            .newFeaturePack(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "2", "2.0.0.Final"))
                .addDependency(FeaturePackConfig
                        .builder(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp3", "2", "2.0.0.Final").getLocation())
                        .excludePackage("a")
                        .excludePackage("b")
                        .includePackage("d")
                        .build())
                .newPackage("p1", true)
                    .writeContent("fp2/p1.txt", "p1")
                    .getFeaturePack()
                .getCreator()
            .newFeaturePack(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp3", "2", "2.0.0.Final"))
                .newPackage("a", true)
                    .writeContent("fp3/a.txt", "a")
                    .getFeaturePack()
                .newPackage("b", true)
                    .addDependency("b1")
                    .writeContent("fp3/b.txt", "b")
                    .getFeaturePack()
                .newPackage("b1")
                    .writeContent("fp3/b1.txt", "b1")
                    .getFeaturePack()
                .newPackage("c", true)
                    .addDependency("c1")
                    .writeContent("fp3/c.txt", "c")
                    .getFeaturePack()
                .newPackage("c1")
                    .writeContent("fp3/c1.txt", "c1")
                    .getFeaturePack()
                .newPackage("d")
                    .writeContent("fp3/d.txt", "d")
                    .getFeaturePack();
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(
                        FeaturePackConfig.forLocation(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Alpha").getLocation()))
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "2", "2.0.0.Final"))
                        .addPackage("p1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp3", "2", "2.0.0.Final"))
                        .addPackage("a")
                        .addPackage("b")
                        .addPackage("b1")
                        .addPackage("c")
                        .addPackage("c1")
                        .addPackage("d")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Alpha"))
                        .addPackage("p1")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1/p1.txt", "p1")
                .addFile("fp2/p1.txt", "p1")
                .addFile("fp3/a.txt", "a")
                .addFile("fp3/b.txt", "b")
                .addFile("fp3/b1.txt", "b1")
                .addFile("fp3/c.txt", "c")
                .addFile("fp3/c1.txt", "c1")
                .addFile("fp3/d.txt", "d")
                .build();
    }
}
