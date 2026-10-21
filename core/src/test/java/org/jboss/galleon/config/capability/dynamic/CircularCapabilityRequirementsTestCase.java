/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.config.capability.dynamic;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.plugin.ProvisionedConfigHandler;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.spec.FeatureParameterSpec;
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
public class CircularCapabilityRequirementsTestCase extends PmInstallFeaturePackTestBase {

    private static final FPID FP_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");

    public static class ConfigHandler extends TestProvisionedConfigHandler {
        @Override
        protected boolean loggingEnabled() {
            return false;
        }
        @Override
        protected boolean branchesEnabled() {
            return false;
        }
        @Override
        protected String[] initEvents() {
            return new String[] {
                    batchStartEvent(),
                    featurePackEvent(FP_GAV),
                    specEvent("specB"),
                    featureEvent(ResolvedFeatureId.create(FP_GAV.getProducer(), "specB", "p1", "b1")),
                    specEvent("specA"),
                    featureEvent(ResolvedFeatureId.create(FP_GAV.getProducer(), "specA", "p1", "a1")),
                    batchEndEvent(),
                    batchStartEvent(),
                    featureEvent(ResolvedFeatureId.create(FP_GAV.getProducer(), "specA", "p1", "a2")),
                    specEvent("specB"),
                    featureEvent(ResolvedFeatureId.create(FP_GAV.getProducer(), "specB", "p1", "b2")),
                    batchEndEvent()
            };
        }
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(FP_GAV)
            .addFeatureSpec(FeatureSpec.builder("specA")
                    .providesCapability("cap.$p1")
                    .requiresCapability("cap.$p2")
                    .addParam(FeatureParameterSpec.createId("p1"))
                    .addParam(FeatureParameterSpec.create("p2"))
                    .build())
            .addFeatureSpec(FeatureSpec.builder("specB")
                    .providesCapability("cap.$p1")
                    .requiresCapability("cap.$p2")
                    .addParam(FeatureParameterSpec.createId("p1"))
                    .addParam(FeatureParameterSpec.create("p2"))
                    .build())
            .addConfig(ConfigModel.builder()
                    .setName("main")
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("p1", "b1")
                            .setParam("p2", "a1"))
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("p1", "a1")
                            .setParam("p2", "b1"))
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("p1", "a2")
                            .setParam("p2", "b2"))
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("p1", "b2")
                            .setParam("p2", "a2"))
                    .build())
            .addPlugin(TestConfigHandlersProvisioningPlugin.class)
            .addService(ProvisionedConfigHandler.class, ConfigHandler.class);
    }

    @Override
    protected FeaturePackConfig featurePackConfig() {
        return FeaturePackConfig.forLocation(FP_GAV.getLocation());
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.forFPID(FP_GAV))
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setName("main")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specB", "p1", "b1")).setConfigParam("p2", "a1").build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specA", "p1", "a1")).setConfigParam("p2", "b1").build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specA", "p1", "a2")).setConfigParam("p2", "b2").build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specB", "p1", "b2")).setConfigParam("p2", "a2").build())
                        .build())
                .build();
    }
}
