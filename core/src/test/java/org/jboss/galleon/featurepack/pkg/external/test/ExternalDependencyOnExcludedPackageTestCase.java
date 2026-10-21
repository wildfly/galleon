/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.featurepack.pkg.external.test;


import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.test.PmProvisionConfigTestBase;
import org.junit.Assert;

/**
 *
 * @author Alexey Loubyansky
 */
public class ExternalDependencyOnExcludedPackageTestCase extends PmProvisionConfigTestBase {

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(LegacyGalleon1Universe.newFPID("org.pm.test:fp1", "1", "1.0.0.Final"))
            .addDependency("fp2-dep", FeaturePackConfig.builder(LegacyGalleon1Universe.newFPID("org.pm.test:fp2", "1", "1.0.0.Final").getLocation())
                    .excludePackage("p2")
                    .build())
            .newPackage("p1", true)
                .addDependency("fp2-dep", "p2")
                .writeContent("fp1/p1.txt", "p1")
                .getFeaturePack()
            .getCreator()
        .newFeaturePack(LegacyGalleon1Universe.newFPID("org.pm.test:fp2", "1", "1.0.0.Final"))
            .newPackage("p1", true)
                //.addDependency("p2")
                .writeContent("fp2/p1.txt", "p1")
                .getFeaturePack()
            .newPackage("p2")
                .writeContent("fp2/p2.txt", "p2")
                .getFeaturePack();
    }

    @Override
    protected void pmSuccess() {
        Assert.fail();
    }

    @Override
    protected void pmFailure(Throwable e) {
        Assert.assertEquals(Errors.unsatisfiedPackageDependency(LegacyGalleon1Universe.newFPID("org.pm.test:fp1", "1", "1.0.0.Final"), "p1"), e.getLocalizedMessage());
        Assert.assertNotNull(e.getCause());
        Assert.assertEquals(Errors.unsatisfiedPackageDependency(LegacyGalleon1Universe.newFPID("org.pm.test:fp2", "1", "1.0.0.Final"), "p2"), e.getCause().getLocalizedMessage());
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(LegacyGalleon1Universe.newFPID("org.pm.test:fp1", "1", "1.0.0.Final").getLocation())
                .build();
    }
}
