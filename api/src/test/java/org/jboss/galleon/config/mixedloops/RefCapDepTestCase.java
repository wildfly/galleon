/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.config.mixedloops;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.api.config.GalleonFeaturePackConfig;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.plugin.ProvisionedConfigHandler;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.spec.FeatureId;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureReferenceSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.api.test.PmInstallFeaturePackTestBase;
import org.jboss.galleon.api.test.util.TestConfigHandlersProvisioningPlugin;
import org.jboss.galleon.api.test.util.TestProvisionedConfigHandler;
import org.jboss.galleon.xml.ProvisionedConfigBuilder;
import org.jboss.galleon.xml.ProvisionedFeatureBuilder;

/**
 *
 * @author Alexey Loubyansky
 */
public class RefCapDepTestCase extends PmInstallFeaturePackTestBase {

    private static final FPID FP_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");

    public static class ConfigHandler extends TestProvisionedConfigHandler {
        @Override
        protected String[] initEvents() {
            return new String[] {
                    batchStartEvent(),
                    featurePackEvent(FP_GAV),
                    specEvent("specA"),
                    featureEvent(ResolvedFeatureId.create(FP_GAV.getProducer(), "specA", "a", "a1")),
                    specEvent("specC"),
                    featureEvent(ResolvedFeatureId.create(FP_GAV.getProducer(), "specC", "c", "c1")),
                    specEvent("specB"),
                    featureEvent(ResolvedFeatureId.create(FP_GAV.getProducer(), "specB", "b", "b1")),
                    batchEndEvent(),
                    batchStartEvent(),
                    featureEvent(ResolvedFeatureId.create(FP_GAV.getProducer(), "specB", "b", "b2")),
                    specEvent("specA"),
                    featureEvent(ResolvedFeatureId.create(FP_GAV.getProducer(), "specA", "a", "a2")),
                    specEvent("specC"),
                    featureEvent(ResolvedFeatureId.create(FP_GAV.getProducer(), "specC", "c", "c2")),
                    batchEndEvent(),
                    batchStartEvent(),
                    featureEvent(ResolvedFeatureId.create(FP_GAV.getProducer(), "specC", "c", "c3")),
                    specEvent("specB"),
                    featureEvent(ResolvedFeatureId.create(FP_GAV.getProducer(), "specB", "b", "b3")),
                    specEvent("specA"),
                    featureEvent(ResolvedFeatureId.create(FP_GAV.getProducer(), "specA", "a", "a3")),
                    batchEndEvent()
            };
        }
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(FP_GAV)
            .addFeatureSpec(FeatureSpec.builder("specA")
                    .addFeatureRef(FeatureReferenceSpec.create("specB"))
                    .addParam(FeatureParameterSpec.createId("a"))
                    .addParam(FeatureParameterSpec.create("b"))
                    .build())
            .addFeatureSpec(FeatureSpec.builder("specB")
                    .requiresCapability("cap.$g")
                    .addParam(FeatureParameterSpec.createId("b"))
                    .addParam(FeatureParameterSpec.create("g"))
                    .build())
            .addFeatureSpec(FeatureSpec.builder("specC")
                    .providesCapability("cap.$c")
                    .addParam(FeatureParameterSpec.createId("c"))
                    .build())
            .addConfig(ConfigModel.builder()
                    .setName("main")
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("a", "a1")
                            .setParam("b", "b1"))
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("b", "b1")
                            .setParam("g", "c1"))
                    .addFeature(
                            new FeatureConfig("specC")
                            .addFeatureDep(FeatureId.create("specA", "a", "a1"))
                            .setParam("c", "c1"))

                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("b", "b2")
                            .setParam("g", "c2"))
                    .addFeature(
                            new FeatureConfig("specC")
                            .addFeatureDep(FeatureId.create("specA", "a", "a2"))
                            .setParam("c", "c2"))
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("a", "a2")
                            .setParam("b", "b2"))

                    .addFeature(
                            new FeatureConfig("specC")
                            .addFeatureDep(FeatureId.create("specA", "a", "a3"))
                            .setParam("c", "c3"))
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("a", "a3")
                            .setParam("b", "b3"))
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("b", "b3")
                            .setParam("g", "c3"))

                    .build())
            .addPlugin(TestConfigHandlersProvisioningPlugin.class)
            .addService(ProvisionedConfigHandler.class, ConfigHandler.class);
    }

    @Override
    protected GalleonFeaturePackConfig featurePackConfig() {
        return GalleonFeaturePackConfig.forLocation(FP_GAV.getLocation());
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.forFPID(FP_GAV))
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setName("main")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specA", "a", "a1")).setConfigParam("b", "b1").build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specC", "c", "c1")).build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specB", "b", "b1")).setConfigParam("g", "c1").build())

                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specB", "b", "b2")).setConfigParam("g", "c2").build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specA", "a", "a2")).setConfigParam("b", "b2").build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specC", "c", "c2")).build())

                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specC", "c", "c3")).build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specB", "b", "b3")).setConfigParam("g", "c3").build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specA", "a", "a3")).setConfigParam("b", "b3").build())

                        .build())
                .build();
    }
}
