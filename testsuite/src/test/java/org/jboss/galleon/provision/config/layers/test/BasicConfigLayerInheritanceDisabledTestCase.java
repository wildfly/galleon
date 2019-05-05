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
import org.jboss.galleon.spec.ConfigLayerSpec;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.util.fs.state.DirState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.ProvisionFromUniverseTestBase;
import org.jboss.galleon.xml.ProvisionedConfigBuilder;

/**
 *
 * @author Alexey Loubyansky
 */
public class BasicConfigLayerInheritanceDisabledTestCase extends ProvisionFromUniverseTestBase {

    private FeaturePackLocation prod1;
    private FeaturePackLocation prod2;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1");
        universe.createProducer("prod2");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {

        prod1 = newFpl("prod1", "1", "1.0.0.Final");
        prod2 = newFpl("prod2", "1", "1.0.0.Final");

        creator.newFeaturePack()
            .setFPID(prod1.getFPID())
            .addFeatureSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("id"))
                    .addParam(FeatureParameterSpec.create("p1", "spec"))
                    .addParam(FeatureParameterSpec.create("p2", "spec"))
                    .addParam(FeatureParameterSpec.create("p3", "spec"))
                    .build())
            .addConfigLayer(ConfigLayerSpec.builder()
                    .setModel("model1").setName("base")
                    .addFeature(new FeatureConfig("specA")
                            .setParam("id", "base-prod1")
                            .setParam("p2", "base"))
                    .addPackageDep("base")
                    .build())
            .addConfig(ConfigModel.builder("model1", "main")
                    .includeLayer("base")
                    .build())
            .newPackage("base")
                .writeContent("base.txt", "base");

        creator.newFeaturePack()
            .setFPID(prod2.getFPID())
            .addFeatureSpec(FeatureSpec.builder("specB")
                .addParam(FeatureParameterSpec.createId("id"))
                .addParam(FeatureParameterSpec.create("p1", "spec"))
                .addParam(FeatureParameterSpec.create("p2", "spec"))
                .addParam(FeatureParameterSpec.create("p3", "spec"))
                .build())
        .addConfigLayer(ConfigLayerSpec.builder()
                .setModel("model1").setName("other")
                .addFeature(new FeatureConfig("specB")
                        .setParam("id", "base-prod2")
                        .setParam("p2", "base"))
                .addPackageDep("other")
                .build())
        .addConfig(ConfigModel.builder("model1", "main")
                .includeLayer("other")
                .build())
        .newPackage("other")
            .writeContent("other.txt", "other");

        creator.install();
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.forLocation(prod1))
                .addFeaturePackDep(FeaturePackConfig.forLocation(prod2))
                .addConfig(ConfigModel.builder("model1", "main")
                        .setInheritLayers(false)
                        .build())
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(prod1.getFPID()).build())
                .addFeaturePack(ProvisionedFeaturePack.builder(prod2.getFPID()).build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setModel("model1")
                        .setName("main")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .build();
    }
}