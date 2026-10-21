/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.test;

import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class PmUninstallFeaturePackTestBase extends PmTestBase {

    protected abstract FPID uninstallGav() throws ProvisioningDescriptionException;

    @Override
    protected void testPm(ProvisioningManager pm) throws ProvisioningException {
        pm.uninstall(uninstallGav());
    }
}
