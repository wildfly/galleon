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
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.spec.ConfigLayerSpec;
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
public class ConfigLayerPackageDependencyOrderTestCase extends ProvisionFromUniverseTestBase {

    private FeaturePackLocation prod1;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {

        prod1 = newFpl("prod1", "1", "1.0.0.Final");

        creator.newFeaturePack()
            .setFPID(prod1.getFPID())
            .addConfigLayer(ConfigLayerSpec.builder()
                    .setModel("model1").setName("base")
                    .addPackageDep("base")
                    .build())
            .addConfigLayer(ConfigLayerSpec.builder()
                    .setModel("model1").setName("main")
                    .addLayerDep("base")
                    .addPackageDep("main")
                    .build())
            .addConfigLayer(ConfigLayerSpec.builder()
                    .setModel("model1").setName("other")
                    .addPackageDep("other")
                    .build())
            .newPackage("base")
                .writeContent("base.txt", "base")
                .writeContent("1.txt", "base")
                .writeContent("2.txt", "base")
                .writeContent("3.txt", "base")
                .getFeaturePack()
            .newPackage("other")
                .writeContent("other.txt", "other")
                .writeContent("1.txt", "other")
                .getFeaturePack()
            .newPackage("main")
                .writeContent("1.txt", "main")
                .writeContent("2.txt", "main")
                .writeContent("main.txt", "main");

        creator.install();
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.forLocation(prod1))
                .addConfig(ConfigModel.builder("model1", "name1")
                        .includeLayer("main")
                        .includeLayer("other")
                        .build())
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(prod1.getFPID())
                        .addPackage("base")
                        .addPackage("main")
                        .addPackage("other")
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setModel("model1")
                        .setName("name1")
                        .addLayer("model1", "base")
                        .addLayer("model1", "main")
                        .addLayer("model1", "other")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("base.txt", "base")
                .addFile("main.txt", "main")
                .addFile("other.txt", "other")
                .addFile("1.txt", "other")
                .addFile("2.txt", "base")
                .addFile("3.txt", "base")
                .build();
    }
}