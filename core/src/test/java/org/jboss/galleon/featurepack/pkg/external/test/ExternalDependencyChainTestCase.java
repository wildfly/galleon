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
public class ExternalDependencyChainTestCase extends PmProvisionConfigTestBase {

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(LegacyGalleon1Universe.newFPID("org.pm.test:fp1", "1", "1.0.0.Final"))
            .addDependency("fp2-dep", FeaturePackConfig.builder(LegacyGalleon1Universe.newFPID("org.pm.test:fp2", "1", "1.0.0.Final").getLocation())
                    .setInheritPackages(false)
                    .build())
            .newPackage("p1", true)
                .addDependency("fp2-dep", "p2")
                .writeContent("fp1/p1.txt", "p1")
                .getFeaturePack()
            .newPackage("p2")
                .addDependency("p3")
                .writeContent("fp1/p2.txt", "p2")
                .getFeaturePack()
            .newPackage("p3")
                .addDependency("fp2-dep", "p2")
                .addDependency("fp2-dep", "p4")
                .writeContent("fp1/p3.txt", "p3")
                .getFeaturePack()
            .getCreator()
        .newFeaturePack(LegacyGalleon1Universe.newFPID("org.pm.test:fp2", "1", "1.0.0.Final"))
            .addDependency("fp1-dep", FeaturePackConfig.builder(LegacyGalleon1Universe.newFPID("org.pm.test:fp1", "1", "1.0.0.Final").getLocation())
                    .build())
            .addDependency("fp3-dep", FeaturePackConfig.builder(LegacyGalleon1Universe.newFPID("org.pm.test:fp3", "1", "1.0.0.Final").getLocation())
                    .setInheritPackages(false)
                    .build())
            .newPackage("p1", true)
                .writeContent("fp2/p1.txt", "p1")
                .getFeaturePack()
            .newPackage("p2")
                .addDependency("p3")
                .writeContent("fp2/p2.txt", "p2")
                .getFeaturePack()
            .newPackage("p3")
                .addDependency("fp1-dep", "p2")
                .writeContent("fp2/p3.txt", "p3")
                .getFeaturePack()
            .newPackage("p4")
                .addDependency("fp3-dep", "p2")
                .writeContent("fp2/p4.txt", "p4")
                .getFeaturePack()
            .getCreator()
        .newFeaturePack(LegacyGalleon1Universe.newFPID("org.pm.test:fp3", "1", "1.0.0.Final"))
            .addDependency("fp1-dep", FeaturePackConfig.builder(LegacyGalleon1Universe.newFPID("org.pm.test:fp1", "1", "1.0.0.Final").getLocation())
                    .setInheritPackages(false)
                    .build())
             .newPackage("p1", true)
                 .writeContent("fp3/p1.txt", "p1")
                 .getFeaturePack()
             .newPackage("p2")
                 .addDependency("fp1-dep", "p1")
                 .writeContent("fp3/p2.txt", "p2")
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
                .addFeaturePack(ProvisionedFeaturePack.builder(LegacyGalleon1Universe.newFPID("org.pm.test:fp3", "1", "1.0.0.Final"))
                        .addPackage("p2")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(LegacyGalleon1Universe.newFPID("org.pm.test:fp2", "1", "1.0.0.Final"))
                        .addPackage("p2")
                        .addPackage("p3")
                        .addPackage("p4")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(LegacyGalleon1Universe.newFPID("org.pm.test:fp1", "1", "1.0.0.Final"))
                        .addPackage("p1")
                        .addPackage("p2")
                        .addPackage("p3")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1/p1.txt", "p1")
                .addFile("fp1/p2.txt", "p2")
                .addFile("fp1/p3.txt", "p3")
                .addFile("fp2/p2.txt", "p2")
                .addFile("fp2/p3.txt", "p3")
                .addFile("fp2/p4.txt", "p4")
                .addFile("fp3/p2.txt", "p2")
                .build();
    }
}
