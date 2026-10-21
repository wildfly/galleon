/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
public class FeaturePackSetPreviewStabilityTestCase extends AbstractFpStatibilityTestCase {

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder().
                addFeaturePackDep(FeaturePackConfig.forLocation(FP1_100_GAV.getLocation())).
                addFeaturePackDep(FeaturePackConfig.forLocation(FP2_100_GAV.getLocation())).
                addOption(Constants.PACKAGE_STABILITY_LEVEL, "preview").build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningDescriptionException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(FP1_100_GAV).addPackage("fp1_1default").addPackage("fp1_2noStability").build())
                .addFeaturePack(ProvisionedFeaturePack.builder(FP2_100_GAV).addPackage("fp2_1default").addPackage("fp2_2noStability").addPackage("fp2_3community").addPackage("fp2_4preview").build())
                .build();
    }
}
