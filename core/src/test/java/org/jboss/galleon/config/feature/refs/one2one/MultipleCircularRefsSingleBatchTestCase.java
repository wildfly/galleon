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
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.plugin.ProvisionedConfigHandler;
import org.jboss.galleon.repomanager.FeaturePackRepositoryManager;
import org.jboss.galleon.runtime.ResolvedFeatureId;
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
public class MultipleCircularRefsSingleBatchTestCase extends PmInstallFeaturePackTestBase {

    private static final Gav FP_GAV = ArtifactCoords.newGav("org.jboss.pm.test", "fp1", "1.0.0.Final");

    public static class ConfigHandler extends TestProvisionedConfigHandler {
        @Override
        protected boolean loggingEnabled() {
            return false;
        }
        @Override
        protected String[] initEvents() {
            return new String[] {
                    batchStartEvent(),
                    featurePackEvent(FP_GAV),
                    specEvent("specA"),
                    featureEvent(ResolvedFeatureId.create(FP_GAV, "specA", "a", "a1")),
                    specEvent("specB"),
                    featureEvent(ResolvedFeatureId.create(FP_GAV, "specB", "b", "b1")),
                    specEvent("specC"),
                    featureEvent(ResolvedFeatureId.create(FP_GAV, "specC", "c", "c1")),
                    specEvent("specE"),
                    featureEvent(ResolvedFeatureId.create(FP_GAV, "specE", "e", "e1")),
                    specEvent("specD"),
                    featureEvent(ResolvedFeatureId.create(FP_GAV, "specD", "d", "d1")),
                    batchEndEvent(),
            };
        }
    }

    @Override
    protected void setupRepo(FeaturePackRepositoryManager repoManager) throws ProvisioningDescriptionException {
        repoManager.installer()
        .newFeaturePack(FP_GAV)
            .addSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("a"))
                    .addParam(FeatureParameterSpec.create("b"))
                    .addParam(FeatureParameterSpec.create("e"))
                    .addFeatureRef(FeatureReferenceSpec.create("specB"))
                    .addFeatureRef(FeatureReferenceSpec.create("specE"))
                    .build())
            .addSpec(FeatureSpec.builder("specB")
                    .addParam(FeatureParameterSpec.createId("b"))
                    .addParam(FeatureParameterSpec.create("c"))
                    .addParam(FeatureParameterSpec.create("d"))
                    .addFeatureRef(FeatureReferenceSpec.create("specC"))
                    .addFeatureRef(FeatureReferenceSpec.create("specD"))
                    .build())
            .addSpec(FeatureSpec.builder("specC")
                    .addParam(FeatureParameterSpec.createId("c"))
                    .addParam(FeatureParameterSpec.create("a"))
                    .addParam(FeatureParameterSpec.create("d"))
                    .addFeatureRef(FeatureReferenceSpec.create("specA"))
                    .addFeatureRef(FeatureReferenceSpec.create("specD"))
                    .build())
            .addSpec(FeatureSpec.builder("specD")
                    .addParam(FeatureParameterSpec.createId("d"))
                    .addParam(FeatureParameterSpec.create("a"))
                    .addParam(FeatureParameterSpec.create("e"))
                    .addFeatureRef(FeatureReferenceSpec.create("specE"))
                    .addFeatureRef(FeatureReferenceSpec.create("specA"))
                    .build())
            .addSpec(FeatureSpec.builder("specE")
                    .addParam(FeatureParameterSpec.createId("e"))
                    .addParam(FeatureParameterSpec.create("b"))
                    .addParam(FeatureParameterSpec.create("c"))
                    .addFeatureRef(FeatureReferenceSpec.create("specB"))
                    .addFeatureRef(FeatureReferenceSpec.create("specC"))
                    .build())
            .addConfig(ConfigModel.builder()
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("a", "a1")
                            .setParam("b", "b1")
                            .setParam("e", "e1"))
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("b", "b1")
                            .setParam("c", "c1")
                            .setParam("d", "d1"))
                    .addFeature(
                            new FeatureConfig("specC")
                            .setParam("c", "c1")
                            .setParam("a", "a1")
                            .setParam("d", "d1"))
                    .addFeature(
                            new FeatureConfig("specD")
                            .setParam("d", "d1")
                            .setParam("a", "a1")
                            .setParam("e", "e1"))
                    .addFeature(
                            new FeatureConfig("specE")
                            .setParam("e", "e1")
                            .setParam("b", "b1")
                            .setParam("c", "c1"))
                    .build())
                .addPlugin(TestConfigHandlersProvisioningPlugin.class)
                .addService(ProvisionedConfigHandler.class, ConfigHandler.class)
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
                .addFeaturePack(ProvisionedFeaturePack.forGav(FP_GAV))
                .addConfig(ProvisionedConfigBuilder.builder()
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specA", "a", "a1"))
                                .setConfigParam("b", "b1")
                                .setConfigParam("e", "e1")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specB", "b", "b1"))
                                .setConfigParam("c", "c1")
                                .setConfigParam("d", "d1")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specC", "c", "c1"))
                                .setConfigParam("a", "a1")
                                .setConfigParam("d", "d1")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specE", "e", "e1"))
                                .setConfigParam("b", "b1")
                                .setConfigParam("c", "c1")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specD", "d", "d1"))
                                .setConfigParam("a", "a1")
                                .setConfigParam("e", "e1")
                                .build())
                        .build())
                .build();
    }
}
