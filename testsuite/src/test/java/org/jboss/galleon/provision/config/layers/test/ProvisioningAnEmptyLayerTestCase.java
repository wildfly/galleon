/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.provision.config.layers.test;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.spec.ConfigLayerSpec;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.ProvisionFromUniverseTestBase;
import org.jboss.galleon.xml.ProvisionedConfigBuilder;

/**
 *
 * @author Alexey Loubyansky
 */
public class ProvisioningAnEmptyLayerTestCase extends ProvisionFromUniverseTestBase {

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
            .addFeatureSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("id"))
                    .addParam(FeatureParameterSpec.create("p1", "spec"))
                    .addParam(FeatureParameterSpec.create("p2", "spec"))
                    .addParam(FeatureParameterSpec.create("p3", "spec"))
                    .build())
            .addConfigLayer(ConfigLayerSpec.builder()
                    .setModel("model1").setName("base")
                    .build())
            .newPackage("base")
                .writeContent("base.txt", "base");

    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.forLocation(prod1))
                .addConfig(ConfigModel.builder("model1", "name1")
                        .includeLayer("base")
                        .build())
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(prod1.getFPID())
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setModel("model1")
                        .setName("name1")
                        .addLayer("model1", "base")
                        .build())
                .build();
    }
}