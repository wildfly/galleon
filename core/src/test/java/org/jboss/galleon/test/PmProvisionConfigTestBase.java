/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.test;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.config.ProvisioningConfig;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class PmProvisionConfigTestBase extends PmTestBase {

    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningException {
        return provisioningConfig();
    }

    protected abstract ProvisioningConfig provisioningConfig() throws ProvisioningException;

    @Override
    protected void testPm(ProvisioningManager pm) throws ProvisioningException {
        pm.provision(provisioningConfig());
    }
}
