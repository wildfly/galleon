/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.installation.configs.order;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.state.ProvisionedState;

/**
 *
 * @author Alexey Loubyansky
 */
public class UnsatisfiedConfigDepTestCase extends ConfigOrderTestBase {

    private static final FPID FP1_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
            .newFeaturePack(FP1_GAV)
                .addFeatureSpec(FeatureSpec.builder("specA")
                        .addParam(FeatureParameterSpec.createId("id"))
                        .build())
                .addConfig(ConfigModel.builder("model1", "config1")
                        .setConfigDep("dep1", new ConfigId(null, "configA"))
                        .addFeature(new FeatureConfig("specA").setParam("id", "11"))
                        .build())
                .addConfig(ConfigModel.builder("model1", "config2")
                        .setConfigDep("dep1", new ConfigId("model1", "config1"))
                        .addFeature(new FeatureConfig("specA").setParam("id", "12"))
                        .build())
                .addPlugin(ConfigListPlugin.class);
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder().addFeaturePackDep(FeaturePackConfig.forLocation(FP1_GAV.getLocation())).build();
    }

    @Override
    protected String[] pmErrors() {
        return new String[] {"Config [model=model1 name=config1] has unsatisfied dependency on config [name=configA]"};
    }

    @Override
    protected String[] configList() {
        return null;
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return null;
    }
}
