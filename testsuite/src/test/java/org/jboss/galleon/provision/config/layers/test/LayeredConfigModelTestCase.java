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
import org.jboss.galleon.test.util.fs.state.DirState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.ProvisionFromUniverseTestBase;
import org.jboss.galleon.xml.ProvisionedConfigBuilder;
import org.jboss.galleon.xml.ProvisionedFeatureBuilder;

/**
 *
 * @author Alexey Loubyansky
 */
public class LayeredConfigModelTestCase extends ProvisionFromUniverseTestBase {

    private FeaturePackLocation prod1;
    private FeaturePackLocation prod2;
    private FeaturePackLocation prod3;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1");
        universe.createProducer("prod2");
        universe.createProducer("prod3");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {

        prod1 = newFpl("prod1", "1", "1.0.0.Final");
        prod2 = newFpl("prod2", "1", "1.0.0.Final");
        prod3 = newFpl("prod3", "1", "1.0.0.Final");

        creator.newFeaturePack()
            .setFPID(prod1.getFPID())
            .addConfigLayer(ConfigLayerSpec.builder()
                    .setModel("model1").setName("base")
                    .addPackageDep("base")
                    .build())
            .addConfigLayer(ConfigLayerSpec.builder()
                    .setModel("model1").setName("a")
                    .build())
            .addConfigLayer(ConfigLayerSpec.builder()
                    .setModel("model1").setName("b")
                    .build())
            .addConfigLayer(ConfigLayerSpec.builder()
                    .setModel("model1").setName("c")
                    .build())
            .addConfig(ConfigModel.builder("model1", "main")
                    .includeLayer("base")
                    .includeLayer("a")
                    .includeLayer("b")
                    .includeLayer("c")
                    .build(), true)
            .newPackage("base")
                .writeContent("base.txt", "base");

        creator.newFeaturePack()
            .setFPID(prod2.getFPID())
            .addDependency(prod1)
            .addFeatureSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("id"))
                    .build())
            .addConfigLayer(ConfigLayerSpec.builder()
                .setModel("model1").setName("a")
                .addPackageDep("a")
                .build())
            .addConfigLayer(ConfigLayerSpec.builder()
                    .setModel("model1").setName("c")
                    .addFeature(new FeatureConfig("specA").setParam("id", "prod2-c"))
                    .build())
            .newPackage("a")
                .writeContent("prod2/a.txt", "prod2 a");

        creator.newFeaturePack()
            .setFPID(prod3.getFPID())
            //.addDependency(prod1)
            .addFeatureSpec(FeatureSpec.builder("specB")
                    .addParam(FeatureParameterSpec.createId("id"))
                    .build())
            .addFeatureSpec(FeatureSpec.builder("specC")
                    .addParam(FeatureParameterSpec.createId("id"))
                    .build())
            .addConfigLayer(ConfigLayerSpec.builder()
                .setModel("model1").setName("b")
                .addFeature(new FeatureConfig("specB").setParam("id", "prod3-b"))
                .addPackageDep("b")
                .build())
            .addConfigLayer(ConfigLayerSpec.builder()
                .setModel("model1").setName("c")
                .addFeature(new FeatureConfig("specC").setParam("id", "prod3-c"))
                .build())
            .newPackage("b")
                .writeContent("prod3/b.txt", "prod3 b");

    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.forLocation(prod2))
                .addFeaturePackDep(FeaturePackConfig.forLocation(prod3))
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(prod1.getFPID())
                        .addPackage("base")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(prod2.getFPID())
                        .addPackage("a")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(prod3.getFPID())
                        .addPackage("b")
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setModel("model1")
                        .setName("main")
                        .addLayer("model1", "base")
                        .addLayer("model1", "a")
                        .addLayer("model1", "b")
                        .addLayer("model1", "c")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(prod3.getProducer(), "specB", "id", "prod3-b")).build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(prod2.getProducer(), "specA", "id", "prod2-c")).build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(prod3.getProducer(), "specC", "id", "prod3-c")).build())
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("base.txt", "base")
                .addFile("prod2/a.txt", "prod2 a")
                .addFile("prod3/b.txt", "prod3 b")
                .build();
    }
}