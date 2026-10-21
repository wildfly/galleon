/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.test;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class PmInstallFeaturePackTestBase extends PmTestBase {

    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder().addFeaturePackDep(featurePackConfig()).build();
    }

    protected abstract FeaturePackConfig featurePackConfig() throws ProvisioningDescriptionException;

    @Override
    protected void testPm(ProvisioningManager pm) throws ProvisioningException {
        pm.install(featurePackConfig());
    }
}
