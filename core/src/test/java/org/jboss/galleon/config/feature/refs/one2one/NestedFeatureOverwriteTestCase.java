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
package org.jboss.galleon.config.feature.refs.one2one;

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
import org.jboss.galleon.spec.FeatureReferenceSpec;
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
public class NestedFeatureOverwriteTestCase extends PmInstallFeaturePackTestBase {

    private static final Gav FP_GAV = ArtifactCoords.newGav("org.jboss.pm.test", "fp1", "1.0.0.Final");

    @Override
    protected void setupRepo(FeaturePackRepositoryManager repoManager) throws ProvisioningDescriptionException {
        repoManager.installer()
        .newFeaturePack(FP_GAV)
            .addSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addParam(FeatureParameterSpec.create("p1", "def1"))
                    .addParam(FeatureParameterSpec.create("p2", "def2"))
                    .addParam(FeatureParameterSpec.create("p3", "def3"))
                    .addParam(FeatureParameterSpec.create("p4", "def4"))
                    .build())
            .addSpec(FeatureSpec.builder("specB")
                    .addFeatureRef(FeatureReferenceSpec.builder("specA").mapParam("specA", "name").build())
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addParam(FeatureParameterSpec.create("p1", "def1"))
                    .addParam(FeatureParameterSpec.create("p2", "def2"))
                    .addParam(FeatureParameterSpec.create("p3", "def3"))
                    .addParam(FeatureParameterSpec.create("specA"))
                    .build())
            .addSpec(FeatureSpec.builder("specC")
                    .addFeatureRef(FeatureReferenceSpec.builder("specB").mapParam("specB", "name").build())
                    .addFeatureRef(FeatureReferenceSpec.builder("specA").mapParam("specA", "name").build())
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addParam(FeatureParameterSpec.create("p1", "def1"))
                    .addParam(FeatureParameterSpec.create("p2", "def2"))
                    .addParam(FeatureParameterSpec.create("p3", "def3"))
                    .addParam(FeatureParameterSpec.create("p4", "def4"))
                    .addParam(FeatureParameterSpec.create("p5", "def5"))
                    .addParam(FeatureParameterSpec.create("specB"))
                    .addParam(FeatureParameterSpec.create("specA"))
                    .build())
            .addFeatureGroup(FeatureGroup.builder("group1")
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("name", "a1")
                            .setParam("p1", "group1")
                            .setParam("p2", "group2")
                            .setParam("p3", "group3")
                            .addFeature(
                                    new FeatureConfig("specC")
                                    .setParam("name", "c1")
                                    .setParam("p1", "group1a")
                                    .setParam("p2", "group1a")
                                    .setParam("p3", "group1a")))
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("name", "b1")
                            .setParam("p1", "group1")
                            .setParam("p2", "group2")
                            .setParam("specA", "a1")
                            .addFeature(
                                    new FeatureConfig("specC")
                                    .setParam("name", "c1")
                                    .setParam("p1", "group1b")
                                    .setParam("p2", "group1b")))
                    .build())
            .addConfig(ConfigModel.builder()
                    .addFeatureGroup(FeatureGroup.builder("group1")
                            .includeFeature(FeatureId.create("specA", "name", "a1"),
                                    new FeatureConfig("specA")
                                    .setParam("name", "a1")
                                    .setParam("p1", "groupConfig1")
                                    .setParam("p2", "groupConfig2")
                                    .addFeature(
                                            new FeatureConfig("specB")
                                            .setParam("name", "b1")
                                            .setParam("p2", "config1")))
                            .includeFeature(FeatureId.create("specC", "name", "c1"),
                                    new FeatureConfig("specC")
                                    .setParam("name", "c1")
                                    .setParam("p1", "config1"))
                            .build())
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("name", "a1")
                            .setParam("p1", "config1"))
                    .addFeature(
                            new FeatureConfig("specC")
                            .setParam("name", "c1")
                            .setParam("p5", "config1"))
                    .build())
            .newPackage("p1", true)
                .getFeaturePack()
            .getInstaller()
        .install();
    }

    @Override
    protected FeaturePackConfig featurePackConfig() {
        return FeaturePackConfig.forGav(FP_GAV);
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(FP_GAV)
                        .addPackage("p1")
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specA", "name", "a1"))
                                .setConfigParam("p1", "config1")
                                .setConfigParam("p2", "groupConfig2")
                                .setConfigParam("p3", "group3")
                                .setConfigParam("p4", "def4")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specB", "name", "b1"))
                                .setConfigParam("p1", "group1")
                                .setConfigParam("p2", "config1")
                                .setConfigParam("p3", "def3")
                                .setConfigParam("specA", "a1")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specC", "name", "c1"))
                                .setConfigParam("p1", "config1")
                                .setConfigParam("p2", "group1b")
                                .setConfigParam("p3", "group1a")
                                .setConfigParam("p4", "def4")
                                .setConfigParam("p5", "config1")
                                .setConfigParam("specA", "a1")
                                .setConfigParam("specB", "b1")
                                .build())
                        .build())
                .build();
    }
}
