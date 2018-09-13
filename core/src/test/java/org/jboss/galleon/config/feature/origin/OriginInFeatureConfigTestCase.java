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
package org.jboss.galleon.config.feature.origin;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureReferenceSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.PmInstallFeaturePackTestBase;
import org.jboss.galleon.test.util.fs.state.DirState;
import org.jboss.galleon.xml.ProvisionedConfigBuilder;
import org.jboss.galleon.xml.ProvisionedFeatureBuilder;

/**
 *
 * @author Alexey Loubyansky
 */
public class OriginInFeatureConfigTestCase extends PmInstallFeaturePackTestBase {

    private static final FPID FP1_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");
    private static final FPID FP2_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "1", "1.0.0.Final");
    private static final FPID FP3_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp3", "1", "1.0.0.Final");

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(FP1_GAV)
            .addFeatureSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("id"))
                    .addParam(FeatureParameterSpec.create("p1", "fp1"))
                    .addPackageDep("p1")
                    .build())
            .addFeatureSpec(FeatureSpec.builder("specD")
                    .addParam(FeatureParameterSpec.createId("id"))
                    .addParam(FeatureParameterSpec.createId("a"))
                    .addFeatureRef(FeatureReferenceSpec.builder("specA")
                            .mapParam("a", "id")
                            .build())
                    .addPackageDep("p2")
                    .build())
            .newPackage("p1")
                    .writeContent("fp1/p1.txt", "fp1 p1")
                    .getFeaturePack()
            .newPackage("p2")
                    .writeContent("fp1/p2.txt", "fp1 p2")
                    .getFeaturePack()
            .getCreator()
        .newFeaturePack(FP2_GAV)
            .addDependency(FP1_GAV.getLocation())
            .addFeatureSpec(FeatureSpec.builder("specB")
                    .addParam(FeatureParameterSpec.createId("id"))
                    .addParam(FeatureParameterSpec.createId("a"))
                    .addFeatureRef(FeatureReferenceSpec.builder("specA")
                            .mapParam("a", "id")
                            .build())
                    .addPackageDep("p1")
                    .build())
            .newPackage("p1")
                    .writeContent("fp2/p1.txt", "fp2 p1")
                    .getFeaturePack()
            .getCreator()
        .newFeaturePack(FP3_GAV)
            .addDependency(FP2_GAV.getLocation())
            .addFeatureSpec(FeatureSpec.builder("specC")
                    .addParam(FeatureParameterSpec.createId("id"))
                    .addParam(FeatureParameterSpec.createId("a"))
                    .addParam(FeatureParameterSpec.createId("b"))
                    .addFeatureRef(FeatureReferenceSpec.builder("specB")
                            .mapParam("a", "a")
                            .mapParam("b", "id")
                            .build())
                    .addPackageDep("p1")
                    .build())
            .newPackage("p1")
                    .writeContent("fp3/p1.txt", "fp3 p1")
                    .getFeaturePack()
            .addConfig(ConfigModel.builder()
                    .setName("main")
                    .addFeature(new FeatureConfig("specA")
                            .setParam("id", "1")
                            .addFeature(new FeatureConfig("specB")
                                    .setParam("id", "2")
                                    .addFeature(new FeatureConfig("specC")
                                            .setParam("id", "3")))
                            .addFeature(new FeatureConfig("specD")
                                    .setParam("id", "4")))
                    .build())
            .getCreator()
        .install();
    }

    @Override
    protected FeaturePackConfig featurePackConfig() {
        return FeaturePackConfig.forLocation(FP3_GAV.getLocation());
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(FP1_GAV)
                        .addPackage("p1")
                        .addPackage("p2")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(FP2_GAV)
                        .addPackage("p1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(FP3_GAV)
                        .addPackage("p1")
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setName("main")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP1_GAV.getProducer(), "specA", "id", "1"))
                                .setConfigParam("p1", "fp1")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.builder(FP2_GAV.getProducer(), "specB")
                                .setParam("id", "2")
                                .setParam("a", "1")
                                .build())
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.builder(FP3_GAV.getProducer(), "specC")
                                .setParam("id", "3")
                                .setParam("a", "1")
                                .setParam("b", "2")
                                .build())
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.builder(FP1_GAV.getProducer(), "specD")
                                .setParam("id", "4")
                                .setParam("a", "1")
                                .build())
                                .build())
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1/p1.txt", "fp1 p1")
                .addFile("fp1/p2.txt", "fp1 p2")
                .addFile("fp2/p1.txt", "fp2 p1")
                .addFile("fp3/p1.txt", "fp3 p1")
                .build();
    }
}
