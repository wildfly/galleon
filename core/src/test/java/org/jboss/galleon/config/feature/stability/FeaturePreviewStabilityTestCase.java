/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.config.feature.stability;

import org.jboss.galleon.Constants;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.runtime.ResolvedSpecId;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.xml.ProvisionedConfigBuilder;
import org.jboss.galleon.xml.ProvisionedFeatureBuilder;

/**
 *
 * @author jfdenise
 */
public class FeaturePreviewStabilityTestCase extends AbstractFeatureStabilityTestCase {

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder().
                addFeaturePackDep(FeaturePackConfig.forLocation(FP1_GAV.getLocation())).
                addFeaturePackDep(FeaturePackConfig.builder(FP2_GAV.getLocation()).setInheritPackages(true).build()).
                addOption(Constants.CONFIG_STABILITY_LEVEL, "preview").
                addOption(Constants.PACKAGE_STABILITY_LEVEL, "preview").build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningDescriptionException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(FP1_GAV).addPackage("p").addPackage("pDefault").addPackage("pCommunity").addPackage("pPreview").build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setName("configA")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(new ResolvedSpecId(FP1_GAV.getProducer(),  "specNoStability"), "id", "1")))
                        .addFeature(ProvisionedFeatureBuilder.
                                builder(ResolvedFeatureId.builder(new ResolvedSpecId(FP1_GAV.getProducer(),  "specPreview")).
                                        setParam("id", "1").build()).setConfigParam("idDefault", "1").setConfigParam("idCommunity", "1").setConfigParam("idPreview", "1").build())
                        .addFeature(ProvisionedFeatureBuilder.
                                builder(ResolvedFeatureId.builder(new ResolvedSpecId(FP1_GAV.getProducer(),  "specCommunity")).
                                        setParam("id", "1").build()).setConfigParam("idDefault", "1").setConfigParam("idCommunity", "1").setConfigParam("idPreview", "1").build())
                        .addFeature(ProvisionedFeatureBuilder.
                                builder(ResolvedFeatureId.builder(new ResolvedSpecId(FP1_GAV.getProducer(),  "specDefault")).
                                        setParam("id", "1").build()).setConfigParam("idDefault", "1").setConfigParam("idCommunity", "1").build())
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(FP2_GAV).addPackage("p").addPackage("pDefault").addPackage("pCommunity").addPackage("pPreview").build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setName("configB")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(new ResolvedSpecId(FP2_GAV.getProducer(),  "specNoStability"), "id", "1")))
                        .addFeature(ProvisionedFeatureBuilder.
                                builder(ResolvedFeatureId.builder(new ResolvedSpecId(FP2_GAV.getProducer(),  "specPreview")).
                                        setParam("id", "1").build()).setConfigParam("idDefault", "1").setConfigParam("idCommunity", "1").setConfigParam("idPreview", "1").build())
                        .addFeature(ProvisionedFeatureBuilder.
                                builder(ResolvedFeatureId.builder(new ResolvedSpecId(FP2_GAV.getProducer(),  "specCommunity")).
                                        setParam("id", "1").build()).setConfigParam("idDefault", "1").setConfigParam("idCommunity", "1").setConfigParam("idPreview", "1").build())
                        .addFeature(ProvisionedFeatureBuilder.
                                builder(ResolvedFeatureId.builder(new ResolvedSpecId(FP2_GAV.getProducer(),  "specDefault")).
                                        setParam("id", "1").build()).setConfigParam("idDefault", "1").setConfigParam("idCommunity", "1").build())
                        .build())
                .build();
    }
}
