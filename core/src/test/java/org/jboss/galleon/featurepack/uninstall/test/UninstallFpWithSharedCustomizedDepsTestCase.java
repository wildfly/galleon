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
package org.jboss.galleon.featurepack.uninstall.test;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.spec.PackageDependencySpec;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.PmUninstallFeaturePackTestBase;
import org.jboss.galleon.test.util.fs.state.DirState;

/**
 *
 * @author Alexey Loubyansky
 */
public class UninstallFpWithSharedCustomizedDepsTestCase extends PmUninstallFeaturePackTestBase {

    private static final FPID FP1_100_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");
    private static final FPID FP2_100_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "1", "1.0.0.Final");
    private static final ProducerSpec FP2_GA = LegacyGalleon1Universe.newProducer("org.jboss.pm.test:fp2");
    private static final FPID FP3_100_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp3", "1", "1.0.0.Final");
    private static final ProducerSpec FP3_GA = LegacyGalleon1Universe.newProducer("org.jboss.pm.test:fp3");
    private static final FPID FP4_100_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp4", "1", "1.0.0.Final");
    private static final ProducerSpec FP4_GA = LegacyGalleon1Universe.newProducer("org.jboss.pm.test:fp4");
    private static final FPID FP5_100_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp5", "1", "1.0.0.Final");
    private static final FPID FP6_100_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp6", "1", "1.0.0.Final");

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator.newFeaturePack(FP1_100_GAV)
                .addDependency(FP2_100_GAV.getLocation())
                .addDependency(FP4_100_GAV.getLocation())
                .addDependency(FP6_100_GAV.getLocation())
                .newPackage("p1", true)
                    .writeContent("fp1/p1.txt", "fp1 1.0.0.Final p1");

        creator.newFeaturePack(FP2_100_GAV)
                .addDependency(FP3_100_GAV.getLocation())
                .newPackage("p1", true)
                    .writeContent("fp2/p1.txt", "fp2 1.0.0.Final p1")
                    .getFeaturePack()
                .newPackage("p2")
                    .writeContent("fp2/p2.txt", "fp2 1.0.0.Final p2");


        creator.newFeaturePack(FP3_100_GAV)
                .newPackage("p1", true)
                    .addDependency(PackageDependencySpec.optional("p2"))
                    .writeContent("fp3/p1.txt", "fp3 1.0.0.Final p1")
                    .getFeaturePack()
                .newPackage("p2")
                    .writeContent("fp3/p2.txt", "fp3 1.0.0.Final p2");

        creator.newFeaturePack(FP4_100_GAV)
                .newPackage("p1", true)
                    .writeContent("fp4/p1.txt", "fp4 1.0.0.Final p1")
                    .getFeaturePack()
                .newPackage("p2", true)
                    .writeContent("fp4/p2.txt", "fp4 1.0.0.Final p2");

        creator.newFeaturePack(FP5_100_GAV)
                .addDependency(FP3_100_GAV.getLocation())
                .addDependency(FP4_100_GAV.getLocation())
                .newPackage("p1", true)
                    .writeContent("fp5/p1.txt", "fp5 1.0.0.Final p1");

        creator.newFeaturePack(FP6_100_GAV)
            .newPackage("p1", true)
                .writeContent("fp6/p1.txt", "fp6 1.0.0.Final p1");

        creator.install();
    }

    @Override
    protected ProvisioningConfig initialState() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.forLocation(FP1_100_GAV.getLocation()))
                .addFeaturePackDep(FeaturePackConfig.transitiveBuilder(FP2_GA.getLocation())
                        .includePackage("p2")
                        .build())
                .addFeaturePackDep(FeaturePackConfig.transitiveBuilder(FP6_100_GAV.getLocation())
                        .build())
                .addFeaturePackDep(FeaturePackConfig.transitiveBuilder(FP3_GA.getLocation())
                        .excludePackage("p2")
                        .build())
                .addFeaturePackDep(FeaturePackConfig.transitiveBuilder(FP4_GA.getLocation())
                        .excludePackage("p2")
                        .build())
                .addFeaturePackDep(FeaturePackConfig.forLocation(FP5_100_GAV.getLocation()))
                .build();
    }

    @Override
    protected FPID uninstallGav() throws ProvisioningDescriptionException {
        return FP1_100_GAV;
    }

    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.transitiveBuilder(FP3_GA.getLocation())
                        .excludePackage("p2")
                        .build())
                .addFeaturePackDep(FeaturePackConfig.transitiveBuilder(FP4_GA.getLocation())
                        .excludePackage("p2")
                        .build())
                .addFeaturePackDep(FeaturePackConfig.forLocation(FP5_100_GAV.getLocation()))
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(FP3_100_GAV)
                        .addPackage("p1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(FP4_100_GAV)
                        .addPackage("p1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(FP5_100_GAV)
                        .addPackage("p1")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp3/p1.txt", "fp3 1.0.0.Final p1")
                .addFile("fp4/p1.txt", "fp4 1.0.0.Final p1")
                .addFile("fp5/p1.txt", "fp5 1.0.0.Final p1")
                .build();
    }
}
