/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
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
public class ExcludeDefaultPackageTestCase extends PmInstallFeaturePackTestBase {

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
            .newFeaturePack(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Alpha"))
                .addDependency(FeaturePackConfig
                        .builder(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "2", "2.0.0.Final").getLocation())
                        .excludePackage("c")
                        .build())
                .newPackage("main", true)
                    .addDependency("d", true)
                    .writeContent("f/p1/c.txt", "c")
                    .getFeaturePack()
                .newPackage("d")
                    .writeContent("f/p1/d.txt", "d")
                    .getFeaturePack()
                .getCreator()
            .newFeaturePack(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "2", "2.0.0.Final"))
                .newPackage("main", true)
                    .addDependency("b", true)
                    .writeContent("f/p2/a.txt", "a")
                    .getFeaturePack()
                .newPackage("b")
                    .writeContent("f/p2/b.txt", "b")
                    .getFeaturePack()
                .newPackage("c", true)
                    .addDependency("d")
                    .writeContent("f/p2/c.txt", "c")
                    .getFeaturePack()
                .newPackage("d")
                    .writeContent("f/p2/d.txt", "d")
                    .getFeaturePack()
                .getCreator()
            .install();
    }

    @Override
    protected FeaturePackConfig featurePackConfig()
            throws ProvisioningDescriptionException {
        return FeaturePackConfig
                .builder(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Alpha").getLocation())
                .excludePackage("d")
                .build();
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
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("f/p1/c.txt", "c")
                .addFile("f/p2/a.txt", "a")
                .addFile("f/p2/b.txt", "b")
                .build();
    }

}
