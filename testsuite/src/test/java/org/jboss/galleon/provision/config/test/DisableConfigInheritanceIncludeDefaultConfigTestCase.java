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
package org.jboss.galleon.provision.config.test;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.runtime.ResolvedFeatureId;
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
public class DisableConfigInheritanceIncludeDefaultConfigTestCase extends ProvisionFromUniverseTestBase {

    private FeaturePackLocation baseFp;
    private FeaturePackLocation topFp;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("base-prod");
        universe.createProducer("top-prod");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {

        baseFp = newFpl("base-prod", "1", "1.0");
        topFp = newFpl("top-prod", "1", "1.0");

        creator.newFeaturePack()
        .setFPID(baseFp.getFPID())
        .addFeatureSpec(FeatureSpec.builder("specA")
                .addParam(FeatureParameterSpec.createId("p1"))
                .build())
        .addConfig(ConfigModel.builder("model1", "name1")
                .addFeature(new FeatureConfig("specA").setParam("p1", "base")).build())
        .addConfig(ConfigModel.builder("model2", "name1")
                        .addFeature(new FeatureConfig("specA").setParam("p1", "base")).build());

        creator.newFeaturePack()
        .setFPID(topFp.getFPID())
        .addDependency(FeaturePackConfig.builder(baseFp)
                .setInheritConfigs(true)
                .build())
        .addFeatureSpec(FeatureSpec.builder("specB")
                .addParam(FeatureParameterSpec.createId("p1"))
                .build())
        .addConfig(ConfigModel.builder("model1", "name1")
                .addFeature(new FeatureConfig("specB").setParam("p1", "top"))
                .build())
        .addConfig(ConfigModel.builder("model2", "name1")
                .addFeature(new FeatureConfig("specB").setParam("p1", "top"))
                .build());

        creator.install();
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(topFp)
                        .setInheritConfigs(false)
                        .includeDefaultConfig("model1", "name1")
                        .build())
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(baseFp.getFPID())
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(topFp.getFPID())
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setModel("model1")
                        .setName("name1")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(baseFp.getFPID().getProducer(), "specA", "p1", "base")))
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(topFp.getFPID().getProducer(), "specB", "p1", "top")))
                        .build())
                .build();
    }
}