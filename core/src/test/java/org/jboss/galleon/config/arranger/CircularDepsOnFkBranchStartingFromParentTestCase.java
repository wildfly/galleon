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
package org.jboss.galleon.config.arranger;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ArtifactCoords.Gav;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.plugin.ProvisionedConfigHandler;
import org.jboss.galleon.repomanager.FeaturePackRepositoryManager;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.spec.FeatureAnnotation;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureReferenceSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.PmInstallFeaturePackTestBase;
import org.jboss.galleon.test.util.TestConfigHandlersProvisioningPlugin;
import org.jboss.galleon.test.util.TestProvisionedConfigHandler;
import org.jboss.galleon.xml.ProvisionedConfigBuilder;
import org.jboss.galleon.xml.ProvisionedFeatureBuilder;

/**
 *
 * @author Alexey Loubyansky
 */
public class CircularDepsOnFkBranchStartingFromParentTestCase extends PmInstallFeaturePackTestBase {

    private static final Gav FP1_GAV = ArtifactCoords.newGav("org.jboss.pm.test", "fp1", "1.0.0.Final");

    public static class ConfigHandler extends TestProvisionedConfigHandler {
        @Override
        protected boolean loggingEnabled() {
            return false;
        }
        @Override
        protected boolean branchesEnabled() {
            return true;
        }
        @Override
        protected String[] initEvents() throws Exception {
            return new String[] {
                    branchStartEvent(),
                    batchStartEvent(),
                    featurePackEvent(FP1_GAV),
                    specEvent("specA2"),
                    featureEvent(ResolvedFeatureId.builder(FP1_GAV, "specA2").setParam("a", "1").setParam("a2", "1").build()),
                    specEvent("specA"),
                    featureEvent(ResolvedFeatureId.create(FP1_GAV, "specA", "a", "1")),
                    specEvent("specA1"),
                    featureEvent(ResolvedFeatureId.builder(FP1_GAV, "specA1").setParam("a", "1").setParam("a1", "1").build()),
                    specEvent("specA3"),
                    featureEvent(ResolvedFeatureId.builder(FP1_GAV, "specA3").setParam("a", "1").setParam("a3", "1").build()),
                    specEvent("specA4"),
                    featureEvent(ResolvedFeatureId.builder(FP1_GAV, "specA4").setParam("a", "1").setParam("a4", "1").build()),
                    batchEndEvent(),
                    branchEndEvent(),

                    branchStartEvent(),
                    batchStartEvent(),
                    specEvent("specA2"),
                    featureEvent(ResolvedFeatureId.builder(FP1_GAV, "specA2").setParam("a", "2").setParam("a2", "1").build()),
                    specEvent("specA"),
                    featureEvent(ResolvedFeatureId.create(FP1_GAV, "specA", "a", "2")),
                    specEvent("specA1"),
                    featureEvent(ResolvedFeatureId.builder(FP1_GAV, "specA1").setParam("a", "2").setParam("a1", "1").build()),
                    specEvent("specA3"),
                    featureEvent(ResolvedFeatureId.builder(FP1_GAV, "specA3").setParam("a", "2").setParam("a3", "1").build()),
                    specEvent("specA4"),
                    featureEvent(ResolvedFeatureId.builder(FP1_GAV, "specA4").setParam("a", "2").setParam("a4", "1").build()),
                    batchEndEvent(),
                    branchEndEvent()
            };
        }
    }

    @Override
    protected void setupRepo(FeaturePackRepositoryManager repoManager) throws ProvisioningDescriptionException {
        repoManager.installer()
        .newFeaturePack(FP1_GAV)

            .addSpec(FeatureSpec.builder("specA")
                    .requiresCapability("a1.$a.$a1")
                    .requiresCapability("a2.$a.$a2")
                    .addAnnotation(FeatureAnnotation.parentChildrenBranch())
                    .addParam(FeatureParameterSpec.createId("a"))
                    .addParam(FeatureParameterSpec.create("a1"))
                    .addParam(FeatureParameterSpec.create("a2"))
                    .build())
            .addSpec(FeatureSpec.builder("specA1")
                    .providesCapability("a1.$a.$a1")
                    .addFeatureRef(FeatureReferenceSpec.create("specA"))
                    .addParam(FeatureParameterSpec.createId("a"))
                    .addParam(FeatureParameterSpec.createId("a1"))
                    .build())
            .addSpec(FeatureSpec.builder("specA2")
                    .providesCapability("a2.$a.$a2")
                    .addFeatureRef(FeatureReferenceSpec.create("specA"))
                    .addParam(FeatureParameterSpec.createId("a"))
                    .addParam(FeatureParameterSpec.createId("a2"))
                    .build())
            .addSpec(FeatureSpec.builder("specA3")
                    .addFeatureRef(FeatureReferenceSpec.create("specA"))
                    .addParam(FeatureParameterSpec.createId("a"))
                    .addParam(FeatureParameterSpec.createId("a3"))
                    .build())
            .addSpec(FeatureSpec.builder("specA4")
                    .addFeatureRef(FeatureReferenceSpec.create("specA"))
                    .addParam(FeatureParameterSpec.createId("a"))
                    .addParam(FeatureParameterSpec.createId("a4"))
                    .build())

            .addConfig(ConfigModel.builder()
                    .setProperty(ConfigModel.BRANCH_IS_BATCH, "true")

                    .addFeature(new FeatureConfig("specA2").setParam("a", "1").setParam("a2", "1"))
                    .addFeature(new FeatureConfig("specA2").setParam("a", "2").setParam("a2", "1"))

                    .addFeature(new FeatureConfig("specA").setParam("a", "1").setParam("a1", "1").setParam("a2", "1"))
                    .addFeature(new FeatureConfig("specA").setParam("a", "2").setParam("a1", "1").setParam("a2", "1"))

                    .addFeature(new FeatureConfig("specA3").setParam("a", "1").setParam("a3", "1"))
                    .addFeature(new FeatureConfig("specA3").setParam("a", "2").setParam("a3", "1"))

                    .addFeature(new FeatureConfig("specA4").setParam("a", "1").setParam("a4", "1"))
                    .addFeature(new FeatureConfig("specA4").setParam("a", "2").setParam("a4", "1"))

                    .addFeature(new FeatureConfig("specA1").setParam("a", "1").setParam("a1", "1"))
                    .addFeature(new FeatureConfig("specA1").setParam("a", "2").setParam("a1", "1"))

                    .build())
            .addPlugin(TestConfigHandlersProvisioningPlugin.class)
            .addService(ProvisionedConfigHandler.class, ConfigHandler.class)
            .getInstaller()
        .install();
    }

    @Override
    protected FeaturePackConfig featurePackConfig() {
        return FeaturePackConfig.forGav(FP1_GAV);
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(FP1_GAV)
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setProperty(ConfigModel.BRANCH_IS_BATCH, "true")

                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.builder(FP1_GAV, "specA2").setParam("a", "1").setParam("a2", "1").build())
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP1_GAV, "specA", "a", "1")).setConfigParam("a1", "1").setConfigParam("a2", "1").build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.builder(FP1_GAV, "specA1").setParam("a", "1").setParam("a1", "1").build())
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.builder(FP1_GAV, "specA3").setParam("a", "1").setParam("a3", "1").build())
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.builder(FP1_GAV, "specA4").setParam("a", "1").setParam("a4", "1").build())
                                .build())

                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.builder(FP1_GAV, "specA2").setParam("a", "2").setParam("a2", "1").build())
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP1_GAV, "specA", "a", "2")).setConfigParam("a1", "1").setConfigParam("a2", "1").build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.builder(FP1_GAV, "specA1").setParam("a", "2").setParam("a1", "1").build())
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.builder(FP1_GAV, "specA3").setParam("a", "2").setParam("a3", "1").build())
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.builder(FP1_GAV, "specA4").setParam("a", "2").setParam("a4", "1").build())
                                .build()))

                .build();
    }
}
