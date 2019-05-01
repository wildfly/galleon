/*
 * Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.galleon.layout;

import static org.junit.Assert.assertEquals;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class LayoutPlanTestBase extends LayoutTestBase {

    protected abstract ProvisioningPlan getPlan() throws ProvisioningDescriptionException;

    protected void assertInitialLayoutConfig(ProvisioningConfig config) throws Exception {
        assertEquals(provisioningConfig(), config);
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
