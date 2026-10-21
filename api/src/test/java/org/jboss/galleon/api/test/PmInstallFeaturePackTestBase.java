/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.api.test;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.api.Provisioning;
import org.jboss.galleon.api.config.GalleonFeaturePackConfig;
import org.jboss.galleon.api.config.GalleonProvisioningConfig;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class PmInstallFeaturePackTestBase extends PmTestBase {

    @Override
    protected GalleonProvisioningConfig provisionedConfig() throws ProvisioningDescriptionException {
        return GalleonProvisioningConfig.builder().addFeaturePackDep(featurePackConfig()).build();
    }

    protected abstract GalleonFeaturePackConfig featurePackConfig() throws ProvisioningDescriptionException;

    @Override
    protected void testPm(Provisioning pm, ProvisioningManager mgr) throws ProvisioningException {
        pm.install(featurePackConfig());
    }
}
