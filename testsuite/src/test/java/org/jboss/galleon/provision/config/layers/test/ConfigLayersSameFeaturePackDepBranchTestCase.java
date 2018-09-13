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

package org.jboss.galleon.provision.config.layers.test;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.spec.ConfigLayerSpec;
import org.jboss.galleon.spec.FeatureId;
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
public class ConfigLayersSameFeaturePackDepBranchTestCase extends ProvisionFromUniverseTestBase {

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
            .addFeatureSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("id"))
                    .addParam(FeatureParameterSpec.create("p1", "spec"))
                    .addParam(FeatureParameterSpec.create("p2", true))
                    .addParam(FeatureParameterSpec.create("p3", true))
                    .addParam(FeatureParameterSpec.create("p4", true))
                    .addParam(FeatureParameterSpec.create("p5", true))
                    .build())
            .addConfigLayer(ConfigLayerSpec.builder()
                    .setModel("model1").setName("base")
                    .addFeature(new FeatureConfig("specA")
                            .setParam("id", "base-prod1")
                            .setParam("p1", "base-prod1")
                            .setParam("p2", "base-prod1"))
                    .addPackageDep("base")
                    .build())
            .addConfigLayer(ConfigLayerSpec.builder()
                    .setModel("model1").setName("ejb")
                    .addLayerDep("base")
                    .addFeature(new FeatureConfig("specA")
                            .setParam("id", "ejb-prod1")
                            .setParam("p1", "ejb-prod1")
                            .setParam("p2", "ejb-prod1"))
                    .addPackageDep("ejb")
                    .build())
            .addConfigLayer(ConfigLayerSpec.builder()
                    .setModel("model1").setName("undertow")
                    .addLayerDep("base")
                    .addFeature(new FeatureConfig("specA")
                            .setParam("id", "undertow-prod1")
                            .setParam("p1", "undertow-prod1")
                            .setParam("p2", "undertow-prod1")
                            .setParam("p3", "undertow-prod1"))
                    .addFeature(new FeatureConfig("specA")
                            .setParam("id", "undertow-other"))
                    .addFeature(new FeatureConfig("specA")
                            .setParam("id", "undertow-other2"))
                    .addPackageDep("undertow")
                    .build())
            .addConfigLayer(ConfigLayerSpec.builder()
                    .setModel("model1").setName("layer-x")
                    .addLayerDep("base")
                    .addFeature(new FeatureConfig("specA").setParam("id", "layer-x-prod1").setParam("p1", "layer-x-prod1"))
                    .addPackageDep("layer-x")
                    .build())
            .addConfig(ConfigModel.builder("model1", "name1")
                    .includeLayer("ejb")
                    .includeLayer("undertow")
                    .includeFeature(FeatureId.create("specA", "id", "undertow-prod1"),
                            new FeatureConfig()
                            .setParam("p2", "config-prod1")
                            .setParam("p3", "config-prod1"))
                    .excludeFeature(FeatureId.create("specA", "id", "undertow-other"))
                    .addFeature(new FeatureConfig("specA").setParam("id", "prod1"))
                    .build())
            .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "p1 final 1.0.1")
                .getFeaturePack()
            .newPackage("base")
            .addDependency("other", true)
            .addDependency("another", true)
                .writeContent("base.txt", "base")
                .getFeaturePack()
            .newPackage("ejb")
                .writeContent("ejb.txt", "ejb")
                .getFeaturePack()
            .newPackage("undertow")
                .writeContent("undertow.txt", "undertow")
                .getFeaturePack()
            .newPackage("other")
                .writeContent("other.txt", "other")
                .getFeaturePack()
            .newPackage("another")
                .writeContent("another.txt", "another")
                .getFeaturePack()
            .newPackage("layer-x")
                .writeContent("layer-x.txt", "layer x");

        creator.newFeaturePack()
            .setFPID(prod2.getFPID())
            .addDependency(prod1)
            .addConfigLayer(ConfigLayerSpec.builder()
                    .setModel("model1").setName("ejb-plus")
                .addFeature(new FeatureConfig("specA")
                        .setParam("id", "ejb-plus-prod2"))
                .build());

        creator.newFeaturePack()
            .setFPID(prod3.getFPID())
            .addDependency(prod1)
            .addDependency(prod2)
            .addFeatureSpec(FeatureSpec.builder("specB")
                    .addParam(FeatureParameterSpec.createId("id"))
                    .build())
            .addConfigLayer(ConfigLayerSpec.builder()
                    .setModel("model1").setName("base")
                    .includeFeature(FeatureId.create("specA", "id", "base-prod1"),
                            new FeatureConfig()
                            .setParam("p2", "base-prod3"))
                    .addFeature(new FeatureConfig("specB")
                            .setParam("id", "base-prod3"))
                    .addPackageDep("base")
                    .build())
            .addConfigLayer(ConfigLayerSpec.builder()
                    .setModel("model1").setName("ejb")
                    .addLayerDep("ejb-plus")
                    .includeFeature(FeatureId.create("specA", "id", "ejb-prod1"),
                            new FeatureConfig()
                            .setParam("p2", "ejb-prod3"))
                    .addFeature(new FeatureConfig("specB")
                            .setParam("id", "ejb-prod3"))
                    .addPackageDep("base")
                    .build());

        creator.install();
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.transitiveBuilder(prod1)
                        .excludePackage("other")
                        .build())
                .addFeaturePackDep(FeaturePackConfig.builder(prod3)
                        .build())
                .addConfig(ConfigModel.builder("model1", "name1")
                        .includeLayer("layer-x")
                        .includeFeature(FeatureId.create("specA", "id", "base-prod1"), new FeatureConfig().setParam("p5", "config"))
                        .includeFeature(FeatureId.create("specA", "id", "undertow-prod1"), new FeatureConfig().setParam("p3", "config-prod3"))
                        .excludeFeature(FeatureId.create("specA", "id", "undertow-other2"))
                        .build())
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(prod1.getFPID())
                        .addPackage("another")
                        .addPackage("base")
                        .addPackage("ejb")
                        .addPackage("layer-x")
                        .addPackage("p1")
                        .addPackage("undertow")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(prod2.getFPID())
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(prod3.getFPID())
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setModel("model1")
                        .setName("name1")
                        .addLayer("model1", "base")
                        .addLayer("model1", "ejb-plus")
                        .addLayer("model1", "ejb")
                        .addLayer("model1", "undertow")
                        .addLayer("model1", "layer-x")
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.create(prod1.getProducer(), "specA", "id", "base-prod1"))
                                .setConfigParam("p1", "base-prod1")
                                .setConfigParam("p2", "base-prod3")
                                .setConfigParam("p5", "config")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.create(prod3.getProducer(), "specB", "id", "base-prod3"))
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.create(prod1.getProducer(), "specA", "id", "ejb-plus-prod2"))
                                .setConfigParam("p1", "spec")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.create(prod1.getProducer(), "specA", "id", "ejb-prod1"))
                                .setConfigParam("p1", "ejb-prod1")
                                .setConfigParam("p2", "ejb-prod3")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.create(prod3.getProducer(), "specB", "id", "ejb-prod3"))
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.create(prod1.getProducer(), "specA", "id", "undertow-prod1"))
                                .setConfigParam("p1", "undertow-prod1")
                                .setConfigParam("p2", "config-prod1")
                                .setConfigParam("p3", "config-prod3")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.create(prod1.getProducer(), "specA", "id", "prod1"))
                                .setConfigParam("p1", "spec")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.create(prod1.getProducer(), "specA", "id", "layer-x-prod1"))
                                .setConfigParam("p1", "layer-x-prod1")
                                .build())
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("another.txt", "another")
                .addFile("base.txt", "base")
                .addFile("ejb.txt", "ejb")
                .addFile("fp1/p1.txt", "p1 final 1.0.1")
                .addFile("layer-x.txt", "layer x")
                .addFile("undertow.txt", "undertow")
                .build();
    }
}