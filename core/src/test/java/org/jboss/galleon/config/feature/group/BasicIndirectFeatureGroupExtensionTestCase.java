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

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeatureGroup;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
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
public class BasicIndirectFeatureGroupExtensionTestCase extends PmInstallFeaturePackTestBase {

    private static final FPID FP_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(FP_GAV)
            .addFeatureSpec(FeatureSpec.builder("specP")
                    .addParam(FeatureParameterSpec.createId("parent"))
                    .build())
            .addFeatureSpec(FeatureSpec.builder("specC")
                    .addParam(FeatureParameterSpec.createId("parent"))
                    .addParam(FeatureParameterSpec.createId("child"))
                    .addParam(FeatureParameterSpec.create("a"))
                    .addParam(FeatureParameterSpec.create("b"))
                    .addFeatureRef(FeatureReferenceSpec.create("specP"))
                    .build())
            .addFeatureGroup(FeatureGroup.builder("group1")
                    .addFeature(
                            new FeatureConfig("specP")
                            .setParam("parent", "p1"))
                    .build())
            .addConfig(ConfigModel.builder()
                    .setName("main")
                    .addFeatureGroup(FeatureGroup.builder("group1")
                            .includeFeature(FeatureId.create("specP", "parent", "p1"), new FeatureConfig()
                                    .addFeature(
                                            new FeatureConfig("specC")
                                            .setParam("child", "c1")
                                            .setParam("a", "config")
                                            .setParam("b", "config")))
                            .build())
                    .build())
            .getCreator()
        .install();
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
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specP", "parent", "p1")).build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.builder(FP_GAV.getProducer(), "specC").setParam("parent", "p1").setParam("child", "c1").build())
                                .setConfigParam("a", "config")
                                .setConfigParam("b", "config")
                                .build())
                        .build())
                .build();
    }
}
