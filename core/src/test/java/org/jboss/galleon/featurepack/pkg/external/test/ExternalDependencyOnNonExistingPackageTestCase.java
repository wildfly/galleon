/*
 * Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
public class ExternalDependencyOnNonExistingPackageTestCase extends PmProvisionConfigTestBase {

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(LegacyGalleon1Universe.newFPID("org.pm.test:fp1", "1", "1.0.0.Final"))
            .addDependency("fp2-dep", FeaturePackConfig.builder(LegacyGalleon1Universe.newFPID("org.pm.test:fp2", "1", "1.0.0.Final").getLocation())
                    .build())
            .newPackage("p1", true)
                .addDependency("fp2-dep", "p2")
                .writeContent("fp1/p1.txt", "p1")
                .getFeaturePack()
            .getCreator()
        .newFeaturePack(LegacyGalleon1Universe.newFPID("org.pm.test:fp2", "1", "1.0.0.Final"))
            .newPackage("p1", true)
                .writeContent("fp2/p1.txt", "p1")
                .getFeaturePack();
    }

    @Override
    protected void pmSuccess() {
        Assert.fail();
    }

    @Override
    protected void pmFailure(Throwable e) {
        Assert.assertEquals(Errors.resolvePackage(LegacyGalleon1Universe.newFPID("org.pm.test:fp1", "1", "1.0.0.Final"), "p1"), e.getLocalizedMessage());
        Assert.assertNotNull(e.getCause());
        Assert.assertEquals(Errors.packageNotFound(LegacyGalleon1Universe.newFPID("org.pm.test:fp2", "1", "1.0.0.Final"), "p2"), e.getCause().getLocalizedMessage());
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(LegacyGalleon1Universe.newFPID("org.pm.test:fp1", "1", "1.0.0.Final").getLocation())
                .build();
    }
}
