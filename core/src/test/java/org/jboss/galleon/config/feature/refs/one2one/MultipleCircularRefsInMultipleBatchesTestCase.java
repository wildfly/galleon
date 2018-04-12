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
 * A->B->C, D->E->F, G->H->I, J->K->L are the basic loops.
 * L->H and I->K were added to combine GHI and JKL into the same batch.
 * A->I was added to re-order the batches ABC and GHIJKL.
 *
 * @author Alexey Loubyansky
 */
public class MultipleCircularRefsInMultipleBatchesTestCase extends PmInstallFeaturePackTestBase {

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
                    specEvent("specG"),
                    featureEvent(ResolvedFeatureId.create(FP_GAV, "specG", "g", "g1")),
                    specEvent("specH"),
                    featureEvent(ResolvedFeatureId.create(FP_GAV, "specH", "h", "h1")),
                    specEvent("specJ"),
                    featureEvent(ResolvedFeatureId.create(FP_GAV, "specJ", "j", "j1")),
                    specEvent("specL"),
                    featureEvent(ResolvedFeatureId.create(FP_GAV, "specL", "l", "l1")),
                    specEvent("specK"),
                    featureEvent(ResolvedFeatureId.create(FP_GAV, "specK", "k", "k1")),
                    specEvent("specI"),
                    featureEvent(ResolvedFeatureId.create(FP_GAV, "specI", "i", "i1")),
                    batchEndEvent(),
                    batchStartEvent(),
                    specEvent("specA"),
                    featureEvent(ResolvedFeatureId.create(FP_GAV, "specA", "a", "a1")),
                    specEvent("specC"),
                    featureEvent(ResolvedFeatureId.create(FP_GAV, "specC", "c", "c1")),
                    specEvent("specB"),
                    featureEvent(ResolvedFeatureId.create(FP_GAV, "specB", "b", "b1")),
                    batchEndEvent(),
                    batchStartEvent(),
                    specEvent("specD"),
                    featureEvent(ResolvedFeatureId.create(FP_GAV, "specD", "d", "d1")),
                    specEvent("specF"),
                    featureEvent(ResolvedFeatureId.create(FP_GAV, "specF", "f", "f1")),
                    specEvent("specE"),
                    featureEvent(ResolvedFeatureId.create(FP_GAV, "specE", "e", "e1")),
                    batchEndEvent()
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
                    .addParam(FeatureParameterSpec.create("i"))
                    .addFeatureRef(FeatureReferenceSpec.create("specB"))
                    .addFeatureRef(FeatureReferenceSpec.create("specI"))
                    .build())
            .addSpec(FeatureSpec.builder("specB")
                    .addParam(FeatureParameterSpec.createId("b"))
                    .addParam(FeatureParameterSpec.create("c"))
                    .addFeatureRef(FeatureReferenceSpec.create("specC"))
                    .build())
            .addSpec(FeatureSpec.builder("specC")
                    .addParam(FeatureParameterSpec.createId("c"))
                    .addParam(FeatureParameterSpec.create("a"))
                    .addFeatureRef(FeatureReferenceSpec.create("specA"))
                    .build())
            .addSpec(FeatureSpec.builder("specD")
                    .addParam(FeatureParameterSpec.createId("d"))
                    .addParam(FeatureParameterSpec.create("c"))
                    .addParam(FeatureParameterSpec.create("e"))
                    .addFeatureRef(FeatureReferenceSpec.create("specC"))
                    .addFeatureRef(FeatureReferenceSpec.create("specE"))
                    .build())
            .addSpec(FeatureSpec.builder("specE")
                    .addParam(FeatureParameterSpec.createId("e"))
                    .addParam(FeatureParameterSpec.create("f"))
                    .addParam(FeatureParameterSpec.create("j"))
                    .addFeatureRef(FeatureReferenceSpec.create("specF"))
                    .addFeatureRef(FeatureReferenceSpec.create("specJ"))
                    .build())
            .addSpec(FeatureSpec.builder("specF")
                    .addParam(FeatureParameterSpec.createId("f"))
                    .addParam(FeatureParameterSpec.create("d"))
                    .addParam(FeatureParameterSpec.create("g"))
                    .addFeatureRef(FeatureReferenceSpec.create("specD"))
                    .addFeatureRef(FeatureReferenceSpec.create("specG"))
                    .build())
            .addSpec(FeatureSpec.builder("specG")
                    .addParam(FeatureParameterSpec.createId("g"))
                    .addParam(FeatureParameterSpec.create("h"))
                    .addFeatureRef(FeatureReferenceSpec.create("specH"))
                    .build())
            .addSpec(FeatureSpec.builder("specH")
                    .addParam(FeatureParameterSpec.createId("h"))
                    .addParam(FeatureParameterSpec.create("i"))
                    .addFeatureRef(FeatureReferenceSpec.create("specI"))
                    .build())
            .addSpec(FeatureSpec.builder("specI")
                    .addParam(FeatureParameterSpec.createId("i"))
                    .addParam(FeatureParameterSpec.create("g"))
                    .addParam(FeatureParameterSpec.create("k"))
                    .addFeatureRef(FeatureReferenceSpec.create("specG"))
                    .addFeatureRef(FeatureReferenceSpec.create("specK"))
                    .build())
            .addSpec(FeatureSpec.builder("specJ")
                    .addParam(FeatureParameterSpec.createId("j"))
                    .addParam(FeatureParameterSpec.create("k"))
                    .addFeatureRef(FeatureReferenceSpec.create("specK"))
                    .build())
            .addSpec(FeatureSpec.builder("specK")
                    .addParam(FeatureParameterSpec.createId("k"))
                    .addParam(FeatureParameterSpec.create("l"))
                    .addFeatureRef(FeatureReferenceSpec.create("specL"))
                    .build())
            .addSpec(FeatureSpec.builder("specL")
                    .addParam(FeatureParameterSpec.createId("l"))
                    .addParam(FeatureParameterSpec.create("j"))
                    .addParam(FeatureParameterSpec.create("h"))
                    .addFeatureRef(FeatureReferenceSpec.create("specJ"))
                    .addFeatureRef(FeatureReferenceSpec.create("specH"))
                    .build())
            .addConfig(ConfigModel.builder()
                    .addFeature(
                            new FeatureConfig("specD")
                            .setParam("d", "d1")
                            .setParam("c", "c1")
                            .setParam("e", "e1"))
                    .addFeature(
                            new FeatureConfig("specE")
                            .setParam("e", "e1")
                            .setParam("f", "f1")
                            .setParam("j", "j1"))
                    .addFeature(
                            new FeatureConfig("specF")
                            .setParam("f", "f1")
                            .setParam("d", "d1")
                            .setParam("g", "g1"))

                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("a", "a1")
                            .setParam("b", "b1")
                            .setParam("i", "i1"))
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("b", "b1")
                            .setParam("c", "c1"))
                    .addFeature(
                            new FeatureConfig("specC")
                            .setParam("c", "c1")
                            .setParam("a", "a1"))

                    .addFeature(
                            new FeatureConfig("specG")
                            .setParam("g", "g1")
                            .setParam("h", "h1"))
                    .addFeature(
                            new FeatureConfig("specH")
                            .setParam("h", "h1")
                            .setParam("i", "i1"))
                    .addFeature(
                            new FeatureConfig("specI")
                            .setParam("i", "i1")
                            .setParam("g", "g1")
                            .setParam("k", "k1"))

                    .addFeature(
                            new FeatureConfig("specJ")
                            .setParam("j", "j1")
                            .setParam("k", "k1"))
                    .addFeature(
                            new FeatureConfig("specK")
                            .setParam("k", "k1")
                            .setParam("l", "l1"))
                    .addFeature(
                            new FeatureConfig("specL")
                            .setParam("l", "l1")
                            .setParam("j", "j1")
                            .setParam("h", "h1"))
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

                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specG", "g", "g1"))
                                .setConfigParam("h", "h1")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specH", "h", "h1"))
                                .setConfigParam("i", "i1")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specJ", "j", "j1"))
                                .setConfigParam("k", "k1")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specL", "l", "l1"))
                                .setConfigParam("j", "j1")
                                .setConfigParam("h", "h1")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specK", "k", "k1"))
                                .setConfigParam("l", "l1")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specI", "i", "i1"))
                                .setConfigParam("g", "g1")
                                .setConfigParam("k", "k1")
                                .build())

                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specA", "a", "a1"))
                                .setConfigParam("b", "b1")
                                .setConfigParam("i", "i1")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specC", "c", "c1"))
                                .setConfigParam("a", "a1")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specB", "b", "b1"))
                                .setConfigParam("c", "c1")
                                .build())

                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specD", "d", "d1"))
                                .setConfigParam("c", "c1")
                                .setConfigParam("e", "e1")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specF", "f", "f1"))
                                .setConfigParam("d", "d1")
                                .setConfigParam("g", "g1")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specE", "e", "e1"))
                                .setConfigParam("f", "f1")
                                .setConfigParam("j", "j1")
                                .build())
                        .build())
                .build();
    }
}
