/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.layout;

import org.jboss.galleon.universe.FeaturePackLocation.FPID;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class LayoutOrderingTestBase extends LayoutTestBase {

    protected abstract FPID[] expectedOrder();

    @Override
    protected void assertLayout(ProvisioningLayout<FeaturePackLayout> layout) throws Exception {
        assertOrdering(expectedOrder(), layout);
    }
}
