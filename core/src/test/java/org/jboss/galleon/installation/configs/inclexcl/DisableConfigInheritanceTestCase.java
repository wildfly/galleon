/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.installation.configs.inclexcl;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.PmProvisionConfigTestBase;

/**
 *
 * @author Alexey Loubyansky
 */
public class DisableConfigInheritanceTestCase extends PmProvisionConfigTestBase {

    private static final FPID FP1_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");
    private static final FPID FP2_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "2", "2.0.0.Final");

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
            .newFeaturePack(FP1_GAV)
                .addFeatureSpec(FeatureSpec.builder("specA")
                        .addParam(FeatureParameterSpec.createId("id"))
                        .build())
                .addConfig(ConfigModel.builder("model1", "config1")
                        .addFeature(new FeatureConfig("specA").setParam("id", "11"))
                        .build())
                .addConfig(ConfigModel.builder("model1", "config2")
                        .addFeature(new FeatureConfig("specA").setParam("id", "12"))
                        .build())
                .addConfig(ConfigModel.builder("model2", "config1")
                        .addFeature(new FeatureConfig("specA").setParam("id", "21"))
                        .build())
                .addConfig(ConfigModel.builder("model2", "config2")
                        .addFeature(new FeatureConfig("specA").setParam("id", "22"))
                        .build())
                .getCreator()
            .newFeaturePack(FP2_GAV)
                .addFeatureSpec(FeatureSpec.builder("specB")
                    .addParam(FeatureParameterSpec.createId("id"))
                    .build())
                .addConfig(ConfigModel.builder("model1", "config1")
                        .addFeature(new FeatureConfig("specB").setParam("id", "11"))
                        .build())
                .addConfig(ConfigModel.builder("model1", "config2")
                        .addFeature(new FeatureConfig("specB").setParam("id", "12"))
                        .build())
                .addConfig(ConfigModel.builder("model2", "config1")
                        .addFeature(new FeatureConfig("specB").setParam("id", "21"))
                        .build())
                .addConfig(ConfigModel.builder("model2", "config2")
                        .addFeature(new FeatureConfig("specB").setParam("id", "22"))
                        .build());
    }

    @Override
    protected ProvisioningConfig provisioningConfig()
            throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.forLocation(FP1_GAV.getLocation()))
                .addFeaturePackDep(FeaturePackConfig.forLocation(FP2_GAV.getLocation()))
                .setInheritConfigs(false)
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.forFPID(FP1_GAV))
                .addFeaturePack(ProvisionedFeaturePack.forFPID(FP2_GAV))
                .build();
    }
}
