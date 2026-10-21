/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.layout;

import static org.junit.Assert.assertArrayEquals;
import java.util.Collection;

import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class FeaturePackUpdatePlanTestBase extends LayoutTestBase {

    protected boolean checkTransitive() {
        return false;
    }

    protected ProducerSpec[] checkUpdates() {
        return new ProducerSpec[0];
    }

    protected abstract FeaturePackUpdatePlan[] expectedUpdatePlans();

    @Override
    protected void assertLayout(ProvisioningLayout<FeaturePackLayout> layout) throws Exception {
        final ProvisioningPlan actual = checkTransitive() ? layout.getUpdates(true) : layout.getUpdates(checkUpdates());
        final Collection<FeaturePackUpdatePlan> updates = actual.getUpdates();
        assertArrayEquals(expectedUpdatePlans(), updates.toArray(new FeaturePackUpdatePlan[updates.size()]));
    }
}
