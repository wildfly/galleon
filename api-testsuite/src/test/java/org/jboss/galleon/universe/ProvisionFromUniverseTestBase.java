/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.universe;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.api.Provisioning;
import org.jboss.galleon.api.config.GalleonProvisioningConfig;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class ProvisionFromUniverseTestBase extends SingleUniverseTestBase {

    @Override
    protected GalleonProvisioningConfig provisionedConfig() throws ProvisioningException {
        return provisioningConfig();
    }

    protected abstract GalleonProvisioningConfig provisioningConfig() throws ProvisioningException;

    @Override
    protected void testPm(Provisioning pm, ProvisioningManager mgr) throws ProvisioningException {
        pm.provision(provisioningConfig());
    }
}
