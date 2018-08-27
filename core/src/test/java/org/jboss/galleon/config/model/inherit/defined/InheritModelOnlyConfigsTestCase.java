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
package org.jboss.galleon.config.model.inherit.defined;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeatureGroup;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.runtime.ResolvedFeatureId;
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
public class InheritModelOnlyConfigsTestCase extends PmInstallFeaturePackTestBase {

    private static final FPID FP1_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");
    private static final FPID FP2_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "1", "1.0.0.Final");

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(FP1_GAV)
            .addFeatureSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addParam(FeatureParameterSpec.create("p1"))
                    .addParam(FeatureParameterSpec.create("p2"))
                    .addParam(FeatureParameterSpec.create("p3"))
                    .addParam(FeatureParameterSpec.create("p4", "spec"))
                    .build())
            .addFeatureSpec(FeatureSpec.builder("specB")
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addPackageDep("fc1.p1")
                    .build())
            .addFeatureGroup(FeatureGroup.builder("fg1")
                    .addFeature(new FeatureConfig("specB").setParam("name", "b"))
                    .addPackageDep("fg1.p1")
                    .build())
            .addConfig(ConfigModel.builder().setModel("model1")
                    .setProperty("prop1", "config1")
                    .setProperty("prop2", "config2")
                    .setProperty("prop3", "config2")
                    .addFeature(new FeatureConfig().setSpecName("specA")
                            .setParam("name", "a1")
                            .setParam("p1", "config1")
                            .setParam("p2", "config2")
                            .setParam("p3", "config2"))
                    .build())
            .addConfig(ConfigModel.builder().setModel("model1").setName("main")
                    .setProperty("prop3", "main")
                    .addFeature(new FeatureConfig().setSpecName("specA")
                            .setParam("name", "a1")
                            .setParam("p3", "main"))
                    .build())
            .addConfig(ConfigModel.builder().setModel("model2")
                    .setProperty("prop2", "config2")
                    .setProperty("prop3", "config2")
                    .addFeatureGroup(FeatureGroup.forGroup("fg1"))
                    .addFeature(new FeatureConfig().setSpecName("specA")
                            .setParam("name", "a1")
                            .setParam("p2", "config2")
                            .setParam("p3", "config2"))
                    .addPackageDep("model2.p1")
                    .build())
            .newPackage("model2.p1")
                    .writeContent("model2/p1.txt", "model2p1")
                    .getFeaturePack()
            .newPackage("fg1.p1")
                    .writeContent("fg1/p1.txt", "fg1p1")
                    .getFeaturePack()
            .newPackage("fc1.p1")
                    .writeContent("fc1/p1.txt", "fc1p1")
                    .getFeaturePack()
            .getCreator()
        .newFeaturePack(FP2_GAV)
            .addDependency("fp1", FeaturePackConfig.builder(FP1_GAV.getLocation())
                    .setInheritConfigs(false)
                    .build())
            .addConfig(ConfigModel.builder()
                    .setModel("model1")
                    .setName("fp2")
                    .addFeature(new FeatureConfig("specA")
                            .setOrigin("fp1")
                            .setParam("name", "a1")
                            .setParam("p3", "fp2"))
                    .build())
            .getCreator()
        .install();
    }

    @Override
    protected FeaturePackConfig featurePackConfig() throws ProvisioningDescriptionException {
        return FeaturePackConfig.forLocation(FP2_GAV.getLocation());
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.forFPID(FP1_GAV))
                .addFeaturePack(ProvisionedFeaturePack.forFPID(FP2_GAV))
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setModel("model1")
                        .setName("fp2")
                        .setProperty("prop1", "config1")
                        .setProperty("prop2", "config2")
                        .setProperty("prop3", "config2")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP1_GAV.getProducer(), "specA", "name", "a1"))
                                .setConfigParam("p1", "config1")
                                .setConfigParam("p2", "config2")
                                .setConfigParam("p3", "fp2")
                                .setConfigParam("p4", "spec")
                                .build())
                        .build())
                .build();
    }
}
