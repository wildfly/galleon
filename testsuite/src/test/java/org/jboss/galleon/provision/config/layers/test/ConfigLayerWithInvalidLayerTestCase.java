/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.provision.config.layers.test;

import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.spec.ConfigLayerSpec;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.util.fs.state.DirState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.ProvisionFromUniverseTestBase;

/**
 *
 * @author jfdenise
 */
public class ConfigLayerWithInvalidLayerTestCase extends ProvisionFromUniverseTestBase {

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
                        .build())
                .addConfigLayer(ConfigLayerSpec.builder()
                        .setModel("model1").setName("layer1")
                        .addFeature(new FeatureConfig("specA")
                                .setParam("id", "base-prod1")
                                .setParam("p1", "base-prod1")
                                .setParam("p2", "base-prod1"))
                        .build());

        creator.newFeaturePack()
                .setFPID(prod2.getFPID())
                .addDependency(prod1)
                .addConfigLayer(ConfigLayerSpec.builder()
                        .setModel("model1").setName("layer2")
                        .addFeature(new FeatureConfig("specA")
                                .setParam("id", "ejb-plus-prod2"))
                        .build());

        creator.newFeaturePack()
                .setFPID(prod3.getFPID())
                .addDependency(prod1)
                .addConfigLayer(ConfigLayerSpec.builder()
                        .setModel("model1").setName("layer2")
                        .addFeature(new FeatureConfig("specA")
                                .setParam("id", "ejb-plus-prod2"))
                        .build());

    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(prod2)
                        .build())
                .addFeaturePackDep(FeaturePackConfig.builder(prod3)
                        .build())
                .addConfig(ConfigModel.builder("model1", "name1")
                        .includeLayer("layer-foo")
                        .build())
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder().build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return null;
    }

    @Override
    protected String[] pmErrors() {
        return new String[]{
            Errors.layerNotFound(new ConfigId("model1", "layer-foo"))
        };
    }

    @Override
    protected boolean assertProvisionedHomeDir() {
        return false;
    }
}
