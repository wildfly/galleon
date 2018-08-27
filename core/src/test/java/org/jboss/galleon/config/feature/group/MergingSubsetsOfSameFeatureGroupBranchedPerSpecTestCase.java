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
package org.jboss.galleon.config.feature.group;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeatureGroup;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.spec.FeatureId;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.PmInstallFeaturePackTestBase;
import org.jboss.galleon.xml.ProvisionedConfigBuilder;
import org.jboss.galleon.xml.ProvisionedFeatureBuilder;

/**
 *
 * @author Alexey Loubyansky
 */
public class MergingSubsetsOfSameFeatureGroupBranchedPerSpecTestCase extends PmInstallFeaturePackTestBase {

    private static final FPID FP_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(FP_GAV)
            .addFeatureSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addParam(FeatureParameterSpec.create("a", true))
                    .build())
            .addFeatureSpec(FeatureSpec.builder("specB")
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addParam(FeatureParameterSpec.create("p1", true))
                    .addParam(FeatureParameterSpec.create("p2", true))
                    .addParam(FeatureParameterSpec.create("p3", true))
                    .addParam(FeatureParameterSpec.create("p4", "spec"))
                    .build())
            .addFeatureSpec(FeatureSpec.builder("specC")
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addParam(FeatureParameterSpec.create("p1", true))
                    .addParam(FeatureParameterSpec.create("p2", true))
                    .addParam(FeatureParameterSpec.create("p3", true))
                    .addParam(FeatureParameterSpec.create("p4", true))
                    .addParam(FeatureParameterSpec.create("p5", "spec"))
                    .build())
            .addFeatureGroup(FeatureGroup.builder("fg1")
                    .addFeatureGroup(FeatureGroup.forGroup("fg2"))
                    .addFeatureGroup(FeatureGroup.builder("fg3")
                            .setInheritFeatures(true)
                            .excludeSpec("specA")
                            .excludeSpec("specC")
                            .includeFeature(FeatureId.create("specB", "name", "fg3b1"),
                                    new FeatureConfig("specB").setParam("name", "fg3b1").setParam("p1", "fg1")
                            )
                            .includeFeature(FeatureId.create("specC", "name", "fg3c1"),
                                    new FeatureConfig("specC").setParam("name", "fg3c1").setParam("p1", "fg1").setParam("p2", "fg1")
                            )
                            .includeFeature(FeatureId.create("specC", "name", "fg3c3"))
                            .build())
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("name", "fg1a1")
                            .setParam("a", "a1"))
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("name", "fg1b1"))
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("name", "fg3b2")
                            .setParam("p2", "fg1"))
                    .addFeature(
                            new FeatureConfig("specC")
                            .setParam("name", "fg1c1"))
                    .build())
            .addFeatureGroup(FeatureGroup.builder("fg2")
                    .addFeatureGroup(FeatureGroup.builder("fg3")
                            .setInheritFeatures(false)
                            .includeSpec("specB")
                            .includeFeature(FeatureId.create("specB", "name", "fg3b1"),
                                    new FeatureConfig("specB").setParam("name", "fg3b1").setParam("p2", "fg2").setParam("p1", "fg2")
                            )
                            .includeSpec("specC")
                            .includeFeature(FeatureId.create("specC", "name", "fg3c1"),
                                    new FeatureConfig("specC").setParam("name", "fg3c1")
                                    .setParam("p3", "fg2")
                                    .setParam("p2", "fg2")
                                    .setParam("p1", "fg2")
                            )
                            .excludeFeature(FeatureId.create("specC", "name", "fg3c2"))
                            .excludeFeature(FeatureId.create("specC", "name", "fg3c3"))
                            .excludeFeature(FeatureId.create("specC", "name", "fg3c4"))
                            .build())
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("name", "fg2a1")
                            .setParam("a", "a2"))
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("name", "fg2b1"))
                    .addFeature(
                            new FeatureConfig("specC")
                            .setParam("name", "fg2c1"))
                    .build())
            .addFeatureGroup(FeatureGroup.builder("fg3")
                    .addFeatureGroup(FeatureGroup.forGroup("fg1"))
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("name", "fg3a1")
                            .setParam("a", "a3"))
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("name", "fg3b1")
                            .setParam("p1", "fg3")
                            .setParam("p2", "fg3")
                            .setParam("p3", "fg3"))
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("name", "fg3b2")
                            .setParam("p1", "fg3")
                            .setParam("p2", "fg3"))
                    .addFeature(
                            new FeatureConfig("specC")
                            .setParam("name", "fg3c1")
                            .setParam("p1", "fg3")
                            .setParam("p2", "fg3")
                            .setParam("p3", "fg3")
                            .setParam("p4", "fg3"))
                    .addFeature(
                            new FeatureConfig("specC")
                            .setParam("name", "fg3c2")
                            .setParam("p5", "fg3"))
                    .addFeature(
                            new FeatureConfig("specC")
                            .setParam("name", "fg3c3")
                            .setParam("p5", "fg3"))
                    .addFeature(
                            new FeatureConfig("specC")
                            .setParam("name", "fg3c4")
                            .setParam("p5", "fg3"))
                    .addFeature(
                            new FeatureConfig("specC")
                            .setParam("name", "fg3c5")
                            .setParam("p5", "fg3"))
                    .build())
            .addConfig(ConfigModel.builder()
                    .setName("main")
                    .setProperty("prop1", "value1")
                    .setProperty("prop2", "value2")
                    .setProperty(ConfigModel.BRANCH_PER_SPEC, "true")
                    .addFeatureGroup(FeatureGroup.forGroup("fg1"))
                    .addFeature(new FeatureConfig("specC").setParam("name", "fg3c1").setParam("p1", "config"))
                    .build())
//            .newPackage("p1", true)
//                .getFeaturePack()
            .getCreator()
        .install();
    }

    @Override
    protected FeaturePackConfig featurePackConfig() {
        return FeaturePackConfig.forLocation(FP_GAV.getLocation());
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(FP_GAV)
//                        .addPackage("p1")
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setName("main")
                        .setProperty("prop1", "value1")
                        .setProperty("prop2", "value2")
                        .setProperty(ConfigModel.BRANCH_PER_SPEC, "true")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specB", "name", "fg3b1"))
                                .setConfigParam("p1", "fg1")
                                .setConfigParam("p2", "fg3")
                                .setConfigParam("p3", "fg3")
                                .setConfigParam("p4", "spec")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specB", "name", "fg3b2"))
                                .setConfigParam("p1", "fg3")
                                .setConfigParam("p2", "fg1")
                                .setConfigParam("p4", "spec")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specB", "name", "fg2b1"))
                                .setConfigParam("p4", "spec")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specB", "name", "fg1b1"))
                                .setConfigParam("p4", "spec")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specC", "name", "fg3c1"))
                                .setConfigParam("p1", "config")
                                .setConfigParam("p2", "fg1")
                                .setConfigParam("p3", "fg3")
                                .setConfigParam("p4", "fg3")
                                .setConfigParam("p5", "spec")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specC", "name", "fg3c5"))
                                .setConfigParam("p5", "fg3")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specC", "name", "fg2c1"))
                                .setConfigParam("p5", "spec")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specC", "name", "fg3c3"))
                                .setConfigParam("p5", "fg3")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specC", "name", "fg1c1"))
                                .setConfigParam("p5", "spec")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specA", "name", "fg2a1"))
                                .setConfigParam("a", "a2")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specA", "name", "fg1a1"))
                                .setConfigParam("a", "a1")
                                .build())
                        .build())
                .build();
    }
}
