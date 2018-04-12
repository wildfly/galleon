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

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ArtifactCoords.Gav;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeatureGroup;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.repomanager.FeaturePackRepositoryManager;
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
public class PreservedConfigOrderOfFeaturesTestCase extends PmInstallFeaturePackTestBase {

    private static final Gav FP1_GAV = ArtifactCoords.newGav("org.jboss.pm.test", "fp1", "1.0.0.Final");
    private static final Gav FP2_GAV = ArtifactCoords.newGav("org.jboss.pm.test", "fp2", "1.0.0.Final");

    @Override
    protected void setupRepo(FeaturePackRepositoryManager repoManager) throws ProvisioningDescriptionException {
        repoManager.installer()
        .newFeaturePack(FP1_GAV)
            .addSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addParam(FeatureParameterSpec.create("a", true))
                    .build())
            .addSpec(FeatureSpec.builder("specB")
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addParam(FeatureParameterSpec.create("b", true))
                    .build())
            .addFeatureGroup(FeatureGroup.builder("fg1")
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("name", "fg1"))
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("name", "fg1"))
                    .build())
            .getInstaller()
        .newFeaturePack(FP2_GAV)
            .addDependency("fp1", FP1_GAV)
            .addSpec(FeatureSpec.builder("specC")
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addParam(FeatureParameterSpec.create("c", true))
                    .build())
            .addSpec(FeatureSpec.builder("specD")
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addParam(FeatureParameterSpec.create("d", true))
                    .build())
            .addFeatureGroup(FeatureGroup.builder("fg2")
                    .addFeature(
                            new FeatureConfig("specC")
                            .setParam("name", "fg2"))
                    .addFeature(
                            new FeatureConfig("specD")
                            .setParam("name", "fg2"))
                    .build())
            .addConfig(ConfigModel.builder()
                    .addFeature(new FeatureConfig("specA").setOrigin("fp1").setParam("name", "config1"))
                    .addFeature(new FeatureConfig("specC").setParam("name", "config1"))
                    .addFeature(new FeatureConfig("specB").setOrigin("fp1").setParam("name", "config1"))
                    .addFeature(new FeatureConfig("specD").setParam("name", "config1"))
                    .addFeatureGroup(FeatureGroup.builder("fg2")
                            .setInheritFeatures(false)
                            .includeSpec("specD")
                            .build())
                    .addFeatureGroup(FeatureGroup.builder("fg1")
                            .setOrigin("fp1")
                            .excludeSpec("specA")
                            .build())
                    .addFeatureGroup(FeatureGroup.builder("fg2")
                            .excludeFeature(FeatureId.create("specD", "name", "fg2"))
                            .build())
                    .addFeatureGroup(FeatureGroup.builder("fg1")
                            .setOrigin("fp1")
                            .setInheritFeatures(false)
                            .includeFeature(FeatureId.create("specA", "name", "fg1"))
                            .build())
            .build())
            .getInstaller()
        .install();
    }

    @Override
    protected FeaturePackConfig featurePackConfig() {
        return FeaturePackConfig.forGav(FP2_GAV);
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.forGav(FP1_GAV))
                .addFeaturePack(ProvisionedFeaturePack.forGav(FP2_GAV))
                .addConfig(ProvisionedConfigBuilder.builder()
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP1_GAV, "specA", "name", "config1")).build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP1_GAV, "specA", "name", "fg1")).build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP2_GAV, "specC", "name", "config1")).build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP2_GAV, "specC", "name", "fg2")).build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP1_GAV, "specB", "name", "config1")).build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP1_GAV, "specB", "name", "fg1")).build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP2_GAV, "specD", "name", "config1")).build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP2_GAV, "specD", "name", "fg2")).build())
                        .build())
                .build();
    }
}
