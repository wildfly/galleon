/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
public class DisableConfigInheritanceIncludeDefaultConfigModelTestCase extends ProvisionFromUniverseTestBase {

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
        .addConfig(ConfigModel.builder("model1", "name2")
                .addFeature(new FeatureConfig("specA").setParam("p1", "base2")).build())
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

    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(topFp)
                        .setInheritConfigs(false)
                        .includeConfigModel("model1")
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
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setModel("model1")
                        .setName("name2")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(baseFp.getFPID().getProducer(), "specA", "p1", "base2")))
                        .build())
                .build();
    }
}