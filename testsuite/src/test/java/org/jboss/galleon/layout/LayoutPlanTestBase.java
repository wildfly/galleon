/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.layout;

import static org.junit.Assert.assertEquals;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class LayoutPlanTestBase extends LayoutTestBase {

    protected abstract ProvisioningPlan getPlan() throws ProvisioningDescriptionException;

    protected void assertInitialLayoutConfig(ProvisioningConfig config) throws Exception {
        assertEquals(initialLayoutConfig(), config);
    }

    protected ProvisioningConfig initialLayoutConfig() throws ProvisioningException {
        return provisioningConfig();
    }

    protected abstract FPID[] expectedInitialOrder();

    protected void assertInitialLayout(ProvisioningLayout<FeaturePackLayout> layout) throws Exception {
        assertOrdering(expectedInitialOrder(), layout);
    }

    protected abstract FPID[] expectedOrder();

    protected void assertFeaturePacks(ProvisioningLayout<FeaturePackLayout> layout) throws Exception {
    }

    @Override
    protected void assertLayout(ProvisioningLayout<FeaturePackLayout> layout) throws Exception {
        assertOrdering(expectedOrder(), layout);
        assertFeaturePacks(layout);
    }

    @Override
    protected ProvisioningLayout<FeaturePackLayout> buildLayout() throws Exception {
        try(ProvisioningLayout<FeaturePackLayout> layout = super.buildLayout()) {
            assertInitialLayoutConfig(layout.getConfig());
            assertInitialLayout(layout);
            layout.apply(getPlan());
            return layout;
        }
    }
}
