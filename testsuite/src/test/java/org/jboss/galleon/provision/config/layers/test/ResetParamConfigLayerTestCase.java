/*
 * Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.provision.config.layers.test;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.spec.ConfigLayerSpec;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.ProvisionFromUniverseTestBase;
import org.jboss.galleon.xml.ProvisionedConfigBuilder;
import org.jboss.galleon.xml.ProvisionedFeatureBuilder;

/**
 *
 * @author Alexey Loubyansky
 */
public class ResetParamConfigLayerTestCase extends ProvisionFromUniverseTestBase {

    private FeaturePackLocation fp1;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        fp1 = newFpl("prod1", "1", "1.0.0.Final");
        creator.newFeaturePack(fp1.getFPID())
            .addFeatureSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("id"))
                    .addParam(FeatureParameterSpec.create("p1", "fp1"))
                    .addParam(FeatureParameterSpec.create("p2", "fp1"))
                    .addParam(FeatureParameterSpec.create("p3", false, true, "default_val_fp1"))
                    .build())
            .addConfigLayer(ConfigLayerSpec.builder()
                    .setModel("model1")
                    .setName("base")
                    .addFeature(new FeatureConfig("specA")
                            .setParam("id", "1")
                            .setParam("p1", "base")
                            .setParam("p2", "base")
                            .setParam("p3", "base"))
                    .build())
            .addConfigLayer(ConfigLayerSpec.builder()
                    .setModel("model1")
                    .setName("layer1")
                    .addLayerDep("base")
                    .addFeature(new FeatureConfig("specA")
                            .setParam("id", "1")
                            .setParam("p2", "layer1")
                            .resetParam("p3"))
                    .build())
            .addConfig(ConfigModel.builder("model1", "name1")
                    .includeLayer("layer1")
                    .build());

    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        final ProvisioningConfig config = ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(fp1)
                        .build())
                .build();
        return config;
    }


    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(fp1.getFPID())
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setModel("model1")
                        .setName("name1")
                        .addLayer("model1", "base")
                        .addLayer("model1", "layer1")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(fp1.getProducer(), "specA", "id", "1"))
                                .setConfigParam("p1", "base")
                                .setConfigParam("p2", "layer1")
                                .setConfigParam("p3", "default_val_fp1")
                                .build())
                        .build())
                .build();
    }
}
