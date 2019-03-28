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
public class DisableConfigInheritanceIncludeDefaultConfigVariation2TestCase extends ProvisionFromUniverseTestBase {

    private FeaturePackLocation servFp;
    private FeaturePackLocation wfFp;
    private FeaturePackLocation topFp;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("serv-prod");
        universe.createProducer("wf-prod");
        universe.createProducer("top-prod");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {

        servFp = newFpl("serv-prod", "1", "1.0");
        wfFp = newFpl("wf-prod", "1", "1.0");
        topFp = newFpl("top-prod", "1", "1.0");

        creator.newFeaturePack()
        .setFPID(servFp.getFPID())
        .addFeatureSpec(FeatureSpec.builder("specA")
                .addParam(FeatureParameterSpec.createId("p1"))
                .build())
        .addConfig(ConfigModel.builder("model1", "name1")
                .addFeature(new FeatureConfig("specA").setParam("p1", "serv")).build())
        .addConfig(ConfigModel.builder("model1", "name2")
                .addFeature(new FeatureConfig("specA").setParam("p1", "serv")).build())
        .addConfig(ConfigModel.builder("model2", "name1")
                        .addFeature(new FeatureConfig("specA").setParam("p1", "wf")).build());

        creator.newFeaturePack()
        .setFPID(wfFp.getFPID())
        .addDependency(FeaturePackConfig.builder(servFp)
                .setInheritConfigs(false)
                .includeDefaultConfig("model1", "name2")
                .build())
        .addConfig(ConfigModel.builder("model1", "name1")
                .addFeature(new FeatureConfig("specA").setParam("p1", "wf")).build())
        .addConfig(ConfigModel.builder("model2", "name1")
                        .addFeature(new FeatureConfig("specA").setParam("p1", "wf")).build());

        creator.newFeaturePack()
        .setFPID(topFp.getFPID())
        .addDependency(FeaturePackConfig.builder(wfFp)
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
                .addFeaturePackDep(FeaturePackConfig.transitiveBuilder(wfFp)
                        .setInheritConfigs(true)
                        .build())
                .addFeaturePackDep(FeaturePackConfig.builder(topFp)
                        .setInheritConfigs(false)
                        .build())
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(servFp.getFPID())
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(wfFp.getFPID())
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(topFp.getFPID())
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setModel("model1")
                        .setName("name2")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(servFp.getFPID().getProducer(), "specA", "p1", "serv")))
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setModel("model1")
                        .setName("name1")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(servFp.getFPID().getProducer(), "specA", "p1", "wf")))
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setModel("model2")
                        .setName("name1")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(servFp.getFPID().getProducer(), "specA", "p1", "wf")))
                        .build())
                .build();
    }
}