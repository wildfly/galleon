/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.config.feature.stability;

import org.jboss.galleon.Constants;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningOption;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.state.ProvisionedState;

/**
 *
 * @author jfdenise
 */
public class FeatureInvalidConfigStabilityTestCase extends AbstractFeatureStabilityTestCase {

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder().
                addFeaturePackDep(FeaturePackConfig.builder(FP1_GAV.getLocation()).setInheritPackages(true).build()).
                addFeaturePackDep(FeaturePackConfig.builder(FP2_GAV.getLocation()).setInheritPackages(true).build()).
                addOption(Constants.CONFIG_STABILITY_LEVEL, "default").
                addOption(Constants.STABILITY_LEVEL, "community").
                build();
    }

    @Override
    protected String[] pmErrors() throws ProvisioningException {
        String[] array = {ProvisioningOption.STABILITY_LEVEL.getName() + " option can't be set when "
                        + ProvisioningOption.CONFIG_STABILITY_LEVEL.getName() + " is set."};
        return array;
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningDescriptionException {
        throw new ProvisioningDescriptionException("Shouldn't be called.");
    }
}
