/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.universe;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.layout.ProvisioningPlan;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class ProvisioningPlanTestBase extends SingleUniverseTestBase {

    protected abstract ProvisioningPlan getPlan() throws ProvisioningDescriptionException;

    @Override
    protected void testPm(ProvisioningManager pm) throws ProvisioningException {
        pm.apply(getPlan());
    }
}
