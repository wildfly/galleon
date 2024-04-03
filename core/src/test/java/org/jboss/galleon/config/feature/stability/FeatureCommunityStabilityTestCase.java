/*
 * Copyright 2016-2024 Red Hat, Inc. and/or its affiliates
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
public class FeatureCommunityStabilityTestCase extends AbstractFeatureStabilityTestCase {

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder().
                addFeaturePackDep(FeaturePackConfig.forLocation(FP1_GAV.getLocation())).
                addFeaturePackDep(FeaturePackConfig.builder(FP2_GAV.getLocation()).setInheritPackages(true).build()).
                addOption(Constants.STABILITY_LEVEL, "community").build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningDescriptionException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(FP1_GAV).addPackage("p").addPackage("pCommunity").addPackage("pDefault").build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setName("configA")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(new ResolvedSpecId(FP1_GAV.getProducer(),  "specNoStability"), "id", "1")))
                        .addFeature(ProvisionedFeatureBuilder.
                                builder(ResolvedFeatureId.builder(new ResolvedSpecId(FP1_GAV.getProducer(),  "specCommunity")).
                                        setParam("id", "1").build()).setConfigParam("idDefault", "1").setConfigParam("idCommunity", "1").build())
                        .addFeature(ProvisionedFeatureBuilder.
                                builder(ResolvedFeatureId.builder(new ResolvedSpecId(FP1_GAV.getProducer(),  "specDefault")).
                                        setParam("id", "1").build()).setConfigParam("idDefault", "1").setConfigParam("idCommunity", "1").build())
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(FP2_GAV).addPackage("p").addPackage("pCommunity").addPackage("pDefault").build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setName("configB")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(new ResolvedSpecId(FP2_GAV.getProducer(),  "specNoStability"), "id", "1")))
                        .addFeature(ProvisionedFeatureBuilder.
                                builder(ResolvedFeatureId.builder(new ResolvedSpecId(FP2_GAV.getProducer(),  "specCommunity")).
                                        setParam("id", "1").build()).setConfigParam("idDefault", "1").setConfigParam("idCommunity", "1").build())
                        .addFeature(ProvisionedFeatureBuilder.
                                builder(ResolvedFeatureId.builder(new ResolvedSpecId(FP2_GAV.getProducer(),  "specDefault")).
                                        setParam("id", "1").build()).setConfigParam("idDefault", "1").setConfigParam("idCommunity", "1").build())

                        .build())
                .build();
    }
}
