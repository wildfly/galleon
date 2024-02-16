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
package org.jboss.galleon.featurepack.stability;

import org.jboss.galleon.Constants;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;

/**
 *
 * @author jfdenise
 */
public class FeaturePackSetExperimentalStabilityTestCase extends AbstractFpStatibilityTestCase {

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder().
                addFeaturePackDep(FeaturePackConfig.forLocation(FP1_100_GAV.getLocation())).
                addFeaturePackDep(FeaturePackConfig.forLocation(FP2_100_GAV.getLocation())).
                addOption(Constants.STABILITY_LEVEL, "experimental").build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningDescriptionException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(FP1_100_GAV).addPackage("fp1_1default").addPackage("fp1_2noStability").build())
                .addFeaturePack(ProvisionedFeaturePack.builder(FP2_100_GAV).addPackage("fp2_1default").addPackage("fp2_2noStability").addPackage("fp2_3community").addPackage("fp2_4preview").addPackage("fp2_5experimental").build())
                .build();
    }
}
