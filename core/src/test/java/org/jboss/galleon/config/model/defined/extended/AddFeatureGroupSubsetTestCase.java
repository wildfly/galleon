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
package org.jboss.galleon.config.model.defined.extended;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ArtifactCoords.Gav;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeatureGroup;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.repomanager.FeaturePackRepositoryManager;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.spec.FeatureId;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.PmProvisionConfigTestBase;
import org.jboss.galleon.xml.ProvisionedConfigBuilder;
import org.jboss.galleon.xml.ProvisionedFeatureBuilder;

/**
 *
 * @author Alexey Loubyansky
 */
public class AddFeatureGroupSubsetTestCase extends PmProvisionConfigTestBase {

    private static final Gav FP_GAV = ArtifactCoords.newGav("org.jboss.pm.test", "fp1", "1.0.0.Final");

    @Override
    protected void setupRepo(FeaturePackRepositoryManager repoManager) throws ProvisioningDescriptionException {
        repoManager.installer()
        .newFeaturePack(FP_GAV)
        .addSpec(FeatureSpec.builder("specA")
                .addParam(FeatureParameterSpec.createId("name"))
                .addParam(FeatureParameterSpec.create("p1", true))
                .build())
        .addSpec(FeatureSpec.builder("specB")
                .addParam(FeatureParameterSpec.createId("name"))
                .addParam(FeatureParameterSpec.create("p1", true))
                .build())
        .addSpec(FeatureSpec.builder("specC")
                .addParam(FeatureParameterSpec.createId("name"))
                .addParam(FeatureParameterSpec.create("p1", true))
                .build())
        .addFeatureGroup(FeatureGroup.builder("group1")
                .addFeature(new FeatureConfig().setSpecName("specA")
                        .setParam("name", "a2")
                        .setParam("p1", "group1"))
                .addFeature(new FeatureConfig().setSpecName("specA")
                        .setParam("name", "a3")
                        .setParam("p1", "group1"))
                .addFeature(new FeatureConfig().setSpecName("specB")
                        .setParam("name", "b2")
                        .setParam("p1", "group1"))
                .addFeature(new FeatureConfig().setSpecName("specB")
                        .setParam("name", "b3")
                        .setParam("p1", "group1"))
                .addFeature(new FeatureConfig().setSpecName("specC")
                        .setParam("name", "c2")
                        .setParam("p1", "group1"))
                .addFeature(new FeatureConfig().setSpecName("specC")
                        .setParam("name", "c3")
                        .setParam("p1", "group1"))
                .build())
        .addConfig(ConfigModel.builder().setName("config1").setModel("model1")
                .addFeature(new FeatureConfig().setSpecName("specA")
                        .setParam("name", "a1")
                        .setParam("p1", "config1"))
                .addFeature(new FeatureConfig().setSpecName("specB")
                        .setParam("name", "b1")
                        .setParam("p1", "config1"))
                .addFeature(new FeatureConfig().setSpecName("specC")
                        .setParam("name", "c1")
                        .setParam("p1", "config1"))
                .build())
        .getInstaller()
        .install();
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(FP_GAV)
                        .addConfig(ConfigModel.builder("model1", "config1")
                                .addFeatureGroup(FeatureGroup.builder("group1")
                                        .excludeSpec("specB")
                                        .excludeFeature(FeatureId.create("specC", "name", "c2"))
                                        .includeFeature(FeatureId.create("specA", "name", "a2"), new FeatureConfig().setParam("p1", "custom"))
                                        .build())
                                .build())
                        .build())
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.forGav(FP_GAV))
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setName("config1")
                        .setModel("model1")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specA", "name", "a1")).setConfigParam("p1", "config1"))
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specA", "name", "a2")).setConfigParam("p1", "custom"))
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specA", "name", "a3")).setConfigParam("p1", "group1"))
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specB", "name", "b1")).setConfigParam("p1", "config1"))
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specC", "name", "c1")).setConfigParam("p1", "config1"))
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specC", "name", "c3")).setConfigParam("p1", "group1"))
                        .build())
                .build();
    }
}
