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
package org.jboss.galleon.featurepack.pkg.test;

import java.util.Iterator;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.PmInstallFeaturePackTestBase;
import org.jboss.galleon.test.util.fs.state.DirState;
import org.junit.Assert;

/**
 *
 * @author Alexey Loubyansky
 */
public class OrderOfProvisionedPackagesTestCase extends PmInstallFeaturePackTestBase {

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(LegacyGalleon1Universe.newFPID("org.pm.test:fp-install", "1", "1.0.0.Beta1"))
            .newPackage("a")
                .writeContent("a.txt", "a")
                .addDependency("e")
                .getFeaturePack()
            .newPackage("b")
                .writeContent("b.txt", "b")
                .addDependency("a")
                .getFeaturePack()
            .newPackage("c", true)
                .writeContent("c.txt", "c")
                .addDependency("d")
                .getFeaturePack()
            .newPackage("d")
                .writeContent("d.txt", "d")
                .addDependency("b")
                .getFeaturePack()
            .newPackage("e")
                .writeContent("e.txt", "e")
                .getFeaturePack()
            .getCreator()
        .install();
    }

    @Override
    protected FeaturePackConfig featurePackConfig() {
        return FeaturePackConfig.forLocation(LegacyGalleon1Universe.newFPID("org.pm.test:fp-install", "1", "1.0.0.Beta1").getLocation());
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(LegacyGalleon1Universe.newFPID("org.pm.test:fp-install", "1", "1.0.0.Beta1"))
                        .addPackage("a")
                        .addPackage("b")
                        .addPackage("c")
                        .addPackage("d")
                        .addPackage("e")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("a.txt", "a")
                .addFile("b.txt", "b")
                .addFile("c.txt", "c")
                .addFile("d.txt", "d")
                .addFile("e.txt", "e")
                .build();
    }

    @Override
    protected void testPm(ProvisioningManager pm) throws ProvisioningException {
        super.testPm(pm);
        final ProvisionedState state = pm.getProvisionedState();
        final Iterator<String> packageNames = state.getFeaturePack(LegacyGalleon1Universe.newProducer("org.pm.test:fp-install"))
                .getPackageNames().iterator();
        Assert.assertTrue(packageNames.hasNext());
        Assert.assertEquals("e", packageNames.next());
        Assert.assertTrue(packageNames.hasNext());
        Assert.assertEquals("a", packageNames.next());
        Assert.assertTrue(packageNames.hasNext());
        Assert.assertEquals("b", packageNames.next());
        Assert.assertTrue(packageNames.hasNext());
        Assert.assertEquals("d", packageNames.next());
        Assert.assertTrue(packageNames.hasNext());
        Assert.assertEquals("c", packageNames.next());
        Assert.assertFalse(packageNames.hasNext());
    }
}
