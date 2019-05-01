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
package org.jboss.galleon.userchanges.persist.test;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.plugin.StateDiffPlugin;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.spec.FeatureId;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.state.ProvisionedConfig;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.xml.ProvisionedConfigBuilder;
import org.jboss.galleon.xml.ProvisionedFeatureBuilder;

/**
 *
 * @author Alexey Loubyansky
 */
public class PersistFeatureIncludeExcludeAddWithInheritFeaturesTrueSpecExcludedTestCase extends PersistChangesTestBase {

    private FeaturePackLocation prod1;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        prod1 = newFpl("prod1", "1", "1.0.0.Final");
        creator.newFeaturePack(prod1.getFPID())
            .addFeatureSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("id"))
                    .addParam(FeatureParameterSpec.create("a", "spec"))
                    .addParam(FeatureParameterSpec.create("b", "spec"))
                    .addParam(FeatureParameterSpec.create("c", "spec"))
                    .build())
            .addConfig(ConfigModel.builder("model1", "name1")
                    .addFeature(new FeatureConfig("specA")
                            .setParam("id", "1")
                            .setParam("a", "config")
                            .setParam("b", "config"))
                    .addFeature(new FeatureConfig("specA")
                            .setParam("id", "2")
                            .setParam("a", "config")
                            .setParam("b", "config"))
                    .build())
            .addService(StateDiffPlugin.class, BasicStateDiffPlugin.class)
            .addPlugin(TestConfigsPersistingPlugin.class);

        creator.install();
    }

    @Override
    protected void testPm(ProvisioningManager pm) throws ProvisioningException {
        pm.provision(ProvisioningConfig.builder()
                .addFeaturePackDep(prod1)
                .addConfig(ConfigModel.builder("model1", "name1")
                        .excludeSpec("specA")
                        .build())
                .build());
        overwrite(ProvisionedConfigBuilder.builder()
                .setModel("model1")
                .setName("name1")
                .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(prod1.getProducer(), "specA", "id", "1"))
                        .setConfigParam("a", "user")
                        .setConfigParam("b", "config")
                        .setConfigParam("c", "spec")
                        .build())
                .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(prod1.getProducer(), "specA", "id", "3"))
                        .setConfigParam("c", "user")
                        .build())
                .build());
        pm.persistChanges();
    }


    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(prod1).build())
                .addConfig(ConfigModel.builder("model1", "name1")
                        .excludeSpec("specA")
                        .includeFeature(FeatureId.create("specA", "id", "1"),
                                new FeatureConfig()
                                .setParam("a", "user")
                                )
                        .addFeature(new FeatureConfig("specA")
                                .setParam("id", "3")
                                .setParam("c", "user"))
                        .build())
                .build();
    }

    protected FPID[] provisionedFpids() {
        return new FPID[] {prod1.getFPID()};
    }

    protected ProvisionedConfig[] provisionedConfigModels() throws ProvisioningException {
        return new ProvisionedConfig[] {
                ProvisionedConfigBuilder.builder()
                .setModel("model1")
                .setName("name1")
                .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(prod1.getProducer(), "specA", "id", "1"))
                        .setConfigParam("a", "user")
                        .setConfigParam("b", "config")
                        .setConfigParam("c", "spec")
                        .build())
                .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(prod1.getProducer(), "specA", "id", "3"))
                        .setConfigParam("a", "spec")
                        .setConfigParam("b", "spec")
                        .setConfigParam("c", "user")
                        .build())
                .build()
        };
    }
}
