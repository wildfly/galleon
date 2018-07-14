/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
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

import static org.junit.Assert.assertArrayEquals;
import java.util.Collection;
import org.jboss.galleon.layout.ProvisioningLayout.FeaturePackLayout;
import org.jboss.galleon.layout.update.FeaturePackUpdatePlan;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class LayoutUpdatePlanTestBase extends LayoutTestBase {

    protected boolean checkTransitive() {
        return false;
    }

    protected ProducerSpec[] checkUpdates() {
        return new ProducerSpec[0];
    }

    protected abstract FeaturePackUpdatePlan[] expectedUpdatePlans();

    @Override
    protected void assertLayout(ProvisioningLayout<FeaturePackLayout> layout) throws Exception {
        final Collection<FeaturePackUpdatePlan> actual = (checkTransitive() ? layout.getUpdatePlans(true) : layout.getUpdatePlans(checkUpdates())).values();
        assertArrayEquals(expectedUpdatePlans(), actual.toArray(new FeaturePackUpdatePlan[actual.size()]));
    }
}
