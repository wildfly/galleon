/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.universe;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.api.Provisioning;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class UninstallFeaturePackTestBase extends SingleUniverseTestBase {

    protected abstract FPID uninstallFpid() throws ProvisioningDescriptionException;

    @Override
    protected void testPm(Provisioning pm, ProvisioningManager mgr) throws ProvisioningException {
        pm.uninstall(uninstallFpid());
    }
}
