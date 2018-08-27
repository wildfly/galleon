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
package org.jboss.galleon.config.arranger.specbranches;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.plugin.ProvisionedConfigHandler;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.spec.FeatureAnnotation;
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
public class BranchForSpecificSpecTestCase extends PmInstallFeaturePackTestBase {

    private static final FPID FP1_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");

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
                    featureEvent(ResolvedFeatureId.create(FP1_GAV.getProducer(), "specA", "a", "1")),
                    branchEndEvent(),
                    branchStartEvent(),
                    specEvent("specB"),
                    featureEvent(ResolvedFeatureId.create(FP1_GAV.getProducer(), "specB", "b", "1")),
                    featureEvent(ResolvedFeatureId.create(FP1_GAV.getProducer(), "specB", "b", "2")),
                    featureEvent(ResolvedFeatureId.create(FP1_GAV.getProducer(), "specB", "b", "3")),
                    branchEndEvent(),
                    branchStartEvent(),
                    specEvent("specC"),
                    featureEvent(ResolvedFeatureId.create(FP1_GAV.getProducer(), "specC", "c", "1")),
                    specEvent("specA"),
                    featureEvent(ResolvedFeatureId.create(FP1_GAV.getProducer(), "specA", "a", "2")),
                    specEvent("specC"),
                    featureEvent(ResolvedFeatureId.create(FP1_GAV.getProducer(), "specC", "c", "2")),
                    specEvent("specA"),
                    featureEvent(ResolvedFeatureId.create(FP1_GAV.getProducer(), "specA", "a", "3")),
                    specEvent("specC"),
                    featureEvent(ResolvedFeatureId.create(FP1_GAV.getProducer(), "specC", "c", "3")),
                    branchEndEvent()
            };
        }
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(FP1_GAV)

            .addFeatureSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("a"))
                    .build())
            .addFeatureSpec(FeatureSpec.builder("specB")
                    .addAnnotation(FeatureAnnotation.specBranch(true))
                    .addParam(FeatureParameterSpec.createId("b"))
                    .build())
            .addFeatureSpec(FeatureSpec.builder("specC")
                    .addParam(FeatureParameterSpec.createId("c"))
                    .build())
            .addConfig(ConfigModel.builder()
                    .setName("main")
                    .setProperty(ConfigModel.BRANCH_PER_SPEC, "false")
                    .addFeature(new FeatureConfig("specA").setParam("a", "1"))
                    .addFeature(new FeatureConfig("specB").setParam("b", "1"))
                    .addFeature(new FeatureConfig("specC").setParam("c", "1"))

                    .addFeature(new FeatureConfig("specA").setParam("a", "2"))
                    .addFeature(new FeatureConfig("specB").setParam("b", "2"))
                    .addFeature(new FeatureConfig("specC").setParam("c", "2"))

                    .addFeature(new FeatureConfig("specA").setParam("a", "3"))
                    .addFeature(new FeatureConfig("specB").setParam("b", "3"))
                    .addFeature(new FeatureConfig("specC").setParam("c", "3"))

                    .build())
            .addPlugin(TestConfigHandlersProvisioningPlugin.class)
            .addService(ProvisionedConfigHandler.class, ConfigHandler.class)
            .getCreator()
        .install();
    }

    @Override
    protected FeaturePackConfig featurePackConfig() {
        return FeaturePackConfig.forLocation(FP1_GAV.getLocation());
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(FP1_GAV)
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setName("main")
                        .setProperty(ConfigModel.BRANCH_PER_SPEC, "false")

                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP1_GAV.getProducer(), "specA", "a", "1")).build())

                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP1_GAV.getProducer(), "specB", "b", "1")).build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP1_GAV.getProducer(), "specB", "b", "2")).build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP1_GAV.getProducer(), "specB", "b", "3")).build())

                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP1_GAV.getProducer(), "specC", "c", "1")).build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP1_GAV.getProducer(), "specA", "a", "2")).build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP1_GAV.getProducer(), "specC", "c", "2")).build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP1_GAV.getProducer(), "specA", "a", "3")).build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP1_GAV.getProducer(), "specC", "c", "3")).build())

                        .build())
                .build();
    }
}
