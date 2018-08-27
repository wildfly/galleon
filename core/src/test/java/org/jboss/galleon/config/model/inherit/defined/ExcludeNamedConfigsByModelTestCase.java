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
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.PmProvisionConfigTestBase;
import org.jboss.galleon.test.util.fs.state.DirState;
import org.jboss.galleon.xml.ProvisionedConfigBuilder;
import org.jboss.galleon.xml.ProvisionedFeatureBuilder;

/**
 *
 * @author Alexey Loubyansky
 */
public class ExcludeNamedConfigsByModelTestCase extends PmProvisionConfigTestBase {

    private static final FPID FP1_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");
    private static final FPID FP2_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "1", "1.0.0.Final");

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(FP1_GAV)
            .addFeatureSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addParam(FeatureParameterSpec.create("p1", true))
                    .build())
            .addConfig(ConfigModel.builder().setName("config1").setModel("model1")
                    .setProperty("prop1", "c1m1")
                    .addFeature(new FeatureConfig().setSpecName("specA")
                            .setParam("name", "a1")
                            .setParam("p1", "config1"))
                    .build())
            .addConfig(ConfigModel.builder().setName("config2").setModel("model1")
                    .setProperty("prop1", "c2m1")
                    .addFeature(new FeatureConfig().setSpecName("specA")
                            .setParam("name", "a1")
                            .setParam("p1", "config2"))
                    .build())
            .addConfig(ConfigModel.builder().setName("config1").setModel("model2")
                    .setProperty("prop1", "c1m2")
                    .addFeature(new FeatureConfig().setSpecName("specA")
                            .setParam("name", "a1")
                            .setParam("p1", "config1"))
                    .build())
            .addConfig(ConfigModel.builder().setName("config2").setModel("model2")
                    .setProperty("prop1", "c2m2")
                    .addFeature(new FeatureConfig().setSpecName("specA")
                            .setParam("name", "a1")
                            .setParam("p1", "config2"))
                    .addPackageDep("model2.p1")
                    .build())
            .newPackage("model2.p1")
                    .writeContent("model2/p1.txt", "model2 p1")
                    .getFeaturePack()
            .getCreator()
        .newFeaturePack(FP2_GAV)
            .addDependency(FeaturePackConfig.builder(FP1_GAV.getLocation())
                    .excludeConfigModel("model2")
                    .build())
            .newPackage("p1", true)
                .writeContent("fp2/p1.txt", "fp2 p1")
                .getFeaturePack()
            .getCreator()
        .install();
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.forLocation(FP2_GAV.getLocation()))
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.forFPID(FP1_GAV))
                .addFeaturePack(ProvisionedFeaturePack.builder(FP2_GAV)
                        .addPackage("p1")
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setName("config1")
                        .setModel("model1")
                        .setProperty("prop1", "c1m1")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP1_GAV.getProducer(), "specA", "name", "a1"))
                                .setConfigParam("p1", "config1")
                                .build())
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setName("config2")
                        .setModel("model1")
                        .setProperty("prop1", "c2m1")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP1_GAV.getProducer(), "specA", "name", "a1"))
                                .setConfigParam("p1", "config2")
                                .build())
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder().addFile("fp2/p1.txt", "fp2 p1").build();
    }
}
