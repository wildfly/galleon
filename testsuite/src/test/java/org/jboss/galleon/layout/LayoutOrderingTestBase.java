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

import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.List;

import org.jboss.galleon.layout.ProvisioningLayout.FeaturePackLayout;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.util.StringUtils;
import org.junit.Assert;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class LayoutOrderingTestBase extends LayoutTestBase {

    protected abstract FPID[] expectedOrder();

    @Override
    protected void assertLayout(ProvisioningLayout<FeaturePackLayout> layout) throws Exception {
        final FPID[] expected = expectedOrder();
        if (expected.length == 0) {
            assertFalse("Layout not empty", layout.hasFeaturePacks());
            return;
        }
        final List<FeaturePackLayout> featurePacks = layout.getOrderedFeaturePacks();
        if (expected.length != featurePacks.size()) {
            fail(expected, featurePacks);
        }
        for (int i = 0; i < expected.length; ++i) {
            if (expected[i].equals(featurePacks.get(i).getFPID())) {
                continue;
            }
            fail(expected, featurePacks);
        }
    }

    private void fail(final FPID[] expected, final List<FeaturePackLayout> featurePacks) {
        final StringBuilder buf = new StringBuilder();
        buf.append("Expected ");
        StringUtils.append(buf, Arrays.asList(expected));
        buf.append(" but was ");
        if(featurePacks.isEmpty()) {
            buf.append("empty");
        } else {
            buf.append(featurePacks.get(0).getFPID());
            for (int j = 1; j < featurePacks.size(); ++j) {
                buf.append(',').append(featurePacks.get(j).getFPID());
            }
        }
        Assert.fail(buf.toString());
    }
}
