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
public class DisableInheritanceIncludeConfigTestCase extends ProvisionFromUniverseTestBase {

    private FeaturePackLocation fp1;
    private FeaturePackLocation fp2;
    private FeaturePackLocation fp3;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1");
        universe.createProducer("prod2");
        universe.createProducer("prod3");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {

        fp1 = newFpl("prod1", "1", "1.0");
        fp2 = newFpl("prod2", "1", "1.0");
        fp3 = newFpl("prod3", "1", "1.0");

        creator.newFeaturePack()
        .setFPID(fp1.getFPID())
        .addDependency(FeaturePackConfig.builder(fp2)
                .setInheritConfigs(true)
                .setInheritPackages(true)
                .build())
        .addDependency(FeaturePackConfig.builder(fp3)
                .setInheritConfigs(false)
                .setInheritPackages(false)
                .build())
        .addFeatureSpec(FeatureSpec.builder("specA")
                .addParam(FeatureParameterSpec.createId("p1"))
                .build())
        .addConfig(ConfigModel.builder("model1", "name1")
                .addFeature(new FeatureConfig("specA").setParam("p1", "1")).build());

        creator.newFeaturePack()
        .setFPID(fp2.getFPID())
        .addFeatureSpec(FeatureSpec.builder("specB")
                .addParam(FeatureParameterSpec.createId("p1"))
                .build())
        .addConfig(ConfigModel.builder("model1", "name1")
                .addFeature(new FeatureConfig("specB").setParam("p1", "1"))
                .build());

        creator.newFeaturePack()
        .setFPID(fp3.getFPID())
        .addDependency(FeaturePackConfig.builder(fp2)
                .setInheritConfigs(true)
                .setInheritPackages(true)
                .build())
        .addFeatureSpec(FeatureSpec.builder("specC")
                .addParam(FeatureParameterSpec.createId("p1"))
                .build())
        .addConfig(ConfigModel.builder("model1", "name1")
                .addFeature(new FeatureConfig("specC").setParam("p1", "1"))
                .build());

        creator.install();
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(fp1)
                        .setInheritConfigs(false)
                        .includeDefaultConfig("model1", "name1")
                        .build())
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(fp2.getFPID())
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(fp3.getFPID())
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(fp1.getFPID())
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setModel("model1")
                        .setName("name1")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(fp2.getFPID().getProducer(), "specB", "p1", "1")))
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(fp1.getFPID().getProducer(), "specA", "p1", "1")))
                        .build())
                .build();
    }
}