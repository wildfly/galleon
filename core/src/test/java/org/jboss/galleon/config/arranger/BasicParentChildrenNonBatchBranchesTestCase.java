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
public class BasicParentChildrenNonBatchBranchesTestCase extends PmInstallFeaturePackTestBase {

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
                    featurePackEvent(FP1_GAV),
                    specEvent("specA"),
                    featureEvent(ResolvedFeatureId.create(FP1_GAV, "specA", "a", "1")),
                    specEvent("specB"),
                    featureEvent(ResolvedFeatureId.builder(FP1_GAV, "specB").setParam("a", "1").setParam("b", "1").build()),
                    specEvent("specC"),
                    featureEvent(ResolvedFeatureId.builder(FP1_GAV, "specC").setParam("a", "1").setParam("b", "1").setParam("c", "1").build()),
                    specEvent("specD"),
                    featureEvent(ResolvedFeatureId.builder(FP1_GAV, "specD").setParam("a", "1").setParam("b", "1").setParam("c", "1").setParam("d", "1").build()),
                    branchEndEvent(),

                    branchStartEvent(),
                    specEvent("specE"),
                    featureEvent(ResolvedFeatureId.builder(FP1_GAV, "specE").setParam("e", "1").build()),
                    specEvent("specF"),
                    featureEvent(ResolvedFeatureId.builder(FP1_GAV, "specF").setParam("e", "1").setParam("f", "1").build()),
                    specEvent("specG"),
                    featureEvent(ResolvedFeatureId.builder(FP1_GAV, "specG").setParam("e", "1").setParam("f", "1").setParam("g", "1").build()),
                    branchEndEvent(),

                    branchStartEvent(),
                    specEvent("specH"),
                    featureEvent(ResolvedFeatureId.create(FP1_GAV, "specH", "h", "1")),
                    branchEndEvent(),

                    branchStartEvent(),
                    specEvent("specI"),
                    featureEvent(ResolvedFeatureId.create(FP1_GAV, "specI", "i", "1")),
                    branchEndEvent(),
            };
        }
    }

    @Override
    protected void setupRepo(FeaturePackRepositoryManager repoManager) throws ProvisioningDescriptionException {
        repoManager.installer()
        .newFeaturePack(FP1_GAV)

            .addSpec(FeatureSpec.builder("specA")
                    .addAnnotation(FeatureAnnotation.parentChildrenBranch(false))
                    .addParam(FeatureParameterSpec.createId("a"))
                    .build())
            .addSpec(FeatureSpec.builder("specB")
                    .addFeatureRef(FeatureReferenceSpec.create("specA"))
                    .addParam(FeatureParameterSpec.createId("a"))
                    .addParam(FeatureParameterSpec.createId("b"))
                    .build())
            .addSpec(FeatureSpec.builder("specC")
                    .addFeatureRef(FeatureReferenceSpec.create("specA"))
                    .addParam(FeatureParameterSpec.createId("a"))
                    .addParam(FeatureParameterSpec.createId("b"))
                    .addParam(FeatureParameterSpec.createId("c"))
                    .build())
            .addSpec(FeatureSpec.builder("specD")
                    .addFeatureRef(FeatureReferenceSpec.create("specB"))
                    .addFeatureRef(FeatureReferenceSpec.create("specC"))
                    .addParam(FeatureParameterSpec.createId("a"))
                    .addParam(FeatureParameterSpec.createId("b"))
                    .addParam(FeatureParameterSpec.createId("c"))
                    .addParam(FeatureParameterSpec.createId("d"))
                    .build())

            .addSpec(FeatureSpec.builder("specE")
                    .addAnnotation(FeatureAnnotation.parentChildrenBranch(false))
                    .addParam(FeatureParameterSpec.createId("e"))
                    .build())
            .addSpec(FeatureSpec.builder("specF")
                    .addFeatureRef(FeatureReferenceSpec.create("specE"))
                    .addFeatureRef(FeatureReferenceSpec.create("specB"))
                    .addParam(FeatureParameterSpec.createId("f"))
                    .addParam(FeatureParameterSpec.createId("e"))
                    .addParam(FeatureParameterSpec.create("a", true))
                    .addParam(FeatureParameterSpec.create("b", true))
                    .build())
            .addSpec(FeatureSpec.builder("specG")
                    .addFeatureRef(FeatureReferenceSpec.create("specF"))
                    .addParam(FeatureParameterSpec.createId("e"))
                    .addParam(FeatureParameterSpec.createId("f"))
                    .addParam(FeatureParameterSpec.createId("g"))
                    .build())
            .addSpec(FeatureSpec.builder("specH")
                    .addParam(FeatureParameterSpec.createId("h"))
                    .build())
            .addSpec(FeatureSpec.builder("specI")
                    .addParam(FeatureParameterSpec.createId("i"))
                    .build())
            .addConfig(ConfigModel.builder()
                    .addFeature(new FeatureConfig("specG").setParam("g", "1").setParam("e", "1").setParam("f", "1"))
                    .addFeature(new FeatureConfig("specH").setParam("h", "1"))
                    .addFeature(new FeatureConfig("specI").setParam("i", "1"))

                    .addFeature(new FeatureConfig("specD").setParam("a", "1").setParam("b", "1").setParam("c", "1").setParam("d", "1"))
                    .addFeature(new FeatureConfig("specC").setParam("a", "1").setParam("b", "1").setParam("c", "1"))
                    .addFeature(new FeatureConfig("specB").setParam("a", "1").setParam("b", "1"))
                    .addFeature(new FeatureConfig("specA").setParam("a", "1"))

                    .addFeature(new FeatureConfig("specE").setParam("e", "1"))
                    .addFeature(new FeatureConfig("specF").setParam("f", "1").setParam("e", "1").setParam("a", "1").setParam("b", "1"))
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

                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP1_GAV, "specA", "a", "1"))
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.builder(FP1_GAV, "specB")
                                .setParam("a", "1")
                                .setParam("b", "1")
                                .build())
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.builder(FP1_GAV, "specC")
                                .setParam("a", "1")
                                .setParam("b", "1")
                                .setParam("c", "1")
                                .build())
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.builder(FP1_GAV, "specD")
                                .setParam("a", "1")
                                .setParam("b", "1")
                                .setParam("c", "1")
                                .setParam("d", "1")
                                .build())
                                .build())

                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP1_GAV, "specE", "e", "1"))
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.builder(FP1_GAV, "specF").setParam("e", "1").setParam("f", "1").build())
                                .setConfigParam("a", "1")
                                .setConfigParam("b", "1")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.builder(FP1_GAV, "specG").setParam("e", "1").setParam("f", "1").setParam("g", "1").build())
                                .build())

                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP1_GAV, "specH", "h", "1"))
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP1_GAV, "specI", "i", "1"))
                                .build())

                        .build())
                .build();
    }
}
