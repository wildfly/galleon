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
package org.jboss.galleon.featurepack.dependency.simple.test;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
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
public class ExcludeOptionalDependencyOfPickedTestCase extends PmProvisionConfigTestBase {

    private static final FPID FP1_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Alpha");
    private static final FPID FP2_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "2", "2.0.0.Final");

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
            .newFeaturePack(FP1_GAV)
                .addDependency("fp2", FeaturePackConfig.builder(FP2_GAV.getLocation())
                        .setInheritPackages(false)
                        .includePackage("b")
                        .excludePackage("c")
                        .build())
                .newPackage("main", true)
                    .addDependency("d")
                    .writeContent("f/p1/c.txt", "c")
                    .getFeaturePack()
                .newPackage("d")
                    .addDependency("fp2", "b")
                    .writeContent("f/p1/d.txt", "d")
                    .getFeaturePack()
                .getCreator()
            .newFeaturePack(FP2_GAV)
                .newPackage("main", true)
                    .addDependency("b")
                    .writeContent("f/p2/a.txt", "a")
                    .getFeaturePack()
                .newPackage("b")
                    .addDependency("c", true)
                    .writeContent("f/p2/b.txt", "b")
                    .getFeaturePack()
                .newPackage("c")
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
    protected ProvisioningConfig provisioningConfig()
            throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(FP1_GAV.getLocation())
                        .setInheritPackages(false)
                        .includePackage("d")
                        .build())
                .addFeaturePackDep(FeaturePackConfig.builder(FP2_GAV.getLocation())
                        .setInheritPackages(false)
                        .excludePackage("c")
                        .build())
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(FP1_GAV)
                        .addPackage("d")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(FP2_GAV)
                        .addPackage("b")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("f/p1/d.txt", "d")
                .addFile("f/p2/b.txt", "b")
                .build();
    }

}
