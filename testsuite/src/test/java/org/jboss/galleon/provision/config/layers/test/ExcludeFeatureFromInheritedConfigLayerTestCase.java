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
public class ExcludeFeatureFromInheritedConfigLayerTestCase extends ProvisionFromUniverseTestBase {

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
                    .build())
            .addConfigLayer(ConfigLayerSpec.builder()
                    .setModel("model1").setName("base")
                    .addFeature(new FeatureConfig("specA")
                            .setParam("id", "1")
                            .setParam("p1", "prod1"))
                    .addFeature(new FeatureConfig("specA")
                            .setParam("id", "2")
                            .setParam("p1", "prod1"))
                    .addPackageDep("prod1")
                    .build())
            .newPackage("prod1")
                .writeContent("prod1.txt", "prod1");

        creator.newFeaturePack()
            .setFPID(prod2.getFPID())
            .addDependency(prod1)
            .addFeatureSpec(FeatureSpec.builder("specB")
                .addParam(FeatureParameterSpec.createId("id"))
                .addParam(FeatureParameterSpec.create("p1", "spec"))
                .addParam(FeatureParameterSpec.create("p2", "spec"))
                .build())
        .addConfigLayer(ConfigLayerSpec.builder()
                .setModel("model1").setName("base")
                .excludeFeature(FeatureId.create("specA", "id", "1"))
                .addFeature(new FeatureConfig("specB")
                        .setParam("id", "1")
                        .setParam("p2", "prod2"))
                .addPackageDep("prod2")
                .build())
        .newPackage("prod2")
            .writeContent("prod2.txt", "prod2");

        creator.install();
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.forLocation(prod2))
                .addConfig(ConfigModel.builder("model1", "name1")
                        .includeLayer("base")
                        .build())
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(prod1.getFPID())
                        .addPackage("prod1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(prod2.getFPID())
                        .addPackage("prod2")
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setModel("model1")
                        .setName("name1")
                        .addLayer("model1", "base")
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.create(prod1.getProducer(), "specA", "id", "2"))
                                .setConfigParam("p1", "prod1")
                                .setConfigParam("p2", "spec")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.create(prod2.getProducer(), "specB", "id", "1"))
                                .setConfigParam("p1", "spec")
                                .setConfigParam("p2", "prod2")
                                .build())
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("prod1.txt", "prod1")
                .addFile("prod2.txt", "prod2")
                .build();
    }
}