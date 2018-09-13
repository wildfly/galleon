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
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.ProvisionConfigMvnTestBase;
import org.jboss.galleon.universe.maven.MavenArtifact;
import org.jboss.galleon.universe.maven.MavenUniverseFactory;
import org.jboss.galleon.xml.ProvisionedConfigBuilder;
import org.jboss.galleon.xml.ProvisionedFeatureBuilder;

/**
 *
 * @author Alexey Loubyansky
 */
public class ConfigLayersSameFeaturePackTestCase extends ProvisionConfigMvnTestBase {

    private static final FeaturePackLocation FINAL1_FPL = FeaturePackLocation.fromString("producer1:1#1.0.1.Final");

    private MavenArtifact universe1Art;
    private FPID prod1;

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {

        universe1Art = newMvnUniverse("universe1")
                .createProducer("producer1", "fp1")
                .install();

        prod1 = mvnFPID(FINAL1_FPL, universe1Art);

        creator.newFeaturePack()
            .setFPID(prod1)
            .addFeatureSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("p1"))
                    .addParam(FeatureParameterSpec.create("p2", "spec"))
                    .addParam(FeatureParameterSpec.create("p3", true))
                    .addParam(FeatureParameterSpec.create("p4", true))
                    .addParam(FeatureParameterSpec.create("p5", true))
                    .build())
            .addConfigLayer(ConfigLayerSpec.builder()
                    .setModel("model1").setName("base")
                    .addFeature(new FeatureConfig("specA")
                            .setParam("p1", "base")
                            .setParam("p2", "base")
                            .setParam("p5", "base"))
                    .addFeature(new FeatureConfig("specA")
                            .setParam("p1", "base1")
                            .setParam("p2", "base"))
                    .addPackageDep("base")
                    .build())
            .addConfigLayer(ConfigLayerSpec.builder()
                    .setModel("model1").setName("ejb")
                    .addLayerDep("base")
                    .addFeature(new FeatureConfig("specA").setParam("p1", "ejb"))
                    .addFeature(new FeatureConfig("specA")
                            .setParam("p1", "base1")
                            .setParam("p2", "ejb")
                            .setParam("p3", "ejb")
                            .setParam("p4", "ejb"))
                    .addPackageDep("ejb")
                    .build())
            .addConfigLayer(ConfigLayerSpec.builder()
                    .setModel("model1").setName("undertow")
                    .addLayerDep("base")
                    .addFeature(new FeatureConfig("specA").setParam("p1", "undertow"))
                    .addFeature(new FeatureConfig("specA")
                            .setParam("p1", "base1")
                            .setParam("p4", "undertow"))
                    .addPackageDep("undertow")
                    .build())
            .addConfig(ConfigModel.builder("model1", "name1")
                    .includeLayer("ejb")
                    .includeLayer("undertow")
                    .addFeature(new FeatureConfig("specA").setParam("p1", "1"))
                    .addFeature(new FeatureConfig("specA").setParam("p1", "2"))
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
                .writeContent("another.txt", "another");

        creator.install();
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .setDefaultUniverse(MavenUniverseFactory.ID, universe1Art.getCoordsAsString())
                .addFeaturePackDep(FeaturePackConfig.builder(FeaturePackLocation.fromString("producer1:1"))
                        .excludePackage("other")
                        .build())
                .addConfig(ConfigModel.builder("model1", "name1")
                        .excludeFeature(FeatureId.create("specA", "p1", "1"))
                        .includeFeature(FeatureId.create("specA", "p1", "base"), new FeatureConfig().setParam("p5", "config"))
                        .build())
                .build();
    }

    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
        .setDefaultUniverse(MavenUniverseFactory.ID, universe1Art.getCoordsAsString())
        .addFeaturePackDep(FeaturePackConfig.builder(FINAL1_FPL)
                .excludePackage("other")
                .build())
        .addConfig(ConfigModel.builder("model1", "name1")
                .excludeFeature(FeatureId.create("specA", "p1", "1"))
                .includeFeature(FeatureId.create("specA", "p1", "base"), new FeatureConfig().setParam("p5", "config"))
                .build())
        .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(prod1)
                        .addPackage("another")
                        .addPackage("base")
                        .addPackage("ejb")
                        .addPackage("undertow")
                        .addPackage("p1")
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setModel("model1")
                        .setName("name1")
                        .addLayer("model1", "base")
                        .addLayer("model1", "ejb")
                        .addLayer("model1", "undertow")
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.create(prod1.getProducer(), "specA", "p1", "base"))
                                .setConfigParam("p2", "base")
                                .setConfigParam("p5", "config")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.create(prod1.getProducer(), "specA", "p1", "base1"))
                                .setConfigParam("p2", "spec")
                                .setConfigParam("p3", "ejb")
                                .setConfigParam("p4", "undertow")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.create(prod1.getProducer(), "specA", "p1", "ejb"))
                                .setConfigParam("p2", "spec")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.create(prod1.getProducer(), "specA", "p1", "undertow"))
                                .setConfigParam("p2", "spec")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.create(prod1.getProducer(), "specA", "p1", "2"))
                                .setConfigParam("p2", "spec")
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
                .addFile("undertow.txt", "undertow")
                .build();
    }
}