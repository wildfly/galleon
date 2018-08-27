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
package org.jboss.galleon.config.model.defined;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
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
public class ExcludeModelNamedOnlyFalseTestCase extends PmInstallFeaturePackTestBase {

    private static final FPID FP_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(FP_GAV)
            .addFeatureSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addParam(FeatureParameterSpec.create("p1", true))
                    .addParam(FeatureParameterSpec.create("p2", true))
                    .addParam(FeatureParameterSpec.create("p3", true))
                    .addParam(FeatureParameterSpec.create("p4", "spec"))
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
                    .addPackageDep("model1.p1")
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
                    .addFeature(new FeatureConfig().setSpecName("specA")
                            .setParam("name", "a1")
                            .setParam("p2", "config2")
                            .setParam("p3", "config2"))
                    .build())
            .newPackage("model1.p1")
                    .writeContent("model1/p1.txt", "model1 p1")
                    .getFeaturePack()
            .getCreator()
        .install();
    }

    @Override
    protected FeaturePackConfig featurePackConfig() throws ProvisioningDescriptionException {
        return FeaturePackConfig.builder(FP_GAV.getLocation())
                .excludeConfigModel("model1", false)
                .addConfig(ConfigModel.builder()
                        .setModel("model1")
                        .setName("custom1")
                        .setProperty("prop3", "custom1")
                        .addFeature(new FeatureConfig("specA")
                                .setParam("name", "a1")
                                .setParam("p3", "custom1"))
                        .build())
                .addConfig(ConfigModel.builder().setName("custom2").setModel("model2").build())
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.forFPID(FP_GAV))
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setModel("model1")
                        .setName("custom1")
                        .setProperty("prop3", "custom1")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specA", "name", "a1"))
                                .setConfigParam("p3", "custom1")
                                .setConfigParam("p4", "spec")
                                .build())
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setModel("model2")
                        .setName("custom2")
                        .setProperty("prop2", "config2")
                        .setProperty("prop3", "config2")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specA", "name", "a1"))
                                .setConfigParam("p2", "config2")
                                .setConfigParam("p3", "config2")
                                .setConfigParam("p4", "spec")
                                .build())
                        .build())
                .build();
    }
}
