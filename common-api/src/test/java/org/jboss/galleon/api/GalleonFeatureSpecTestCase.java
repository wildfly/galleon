/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.api;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jdenise
 */
public class GalleonFeatureSpecTestCase {

    @Test
    public void test() {
        GalleonFeatureSpec spec = new GalleonFeatureSpec("foo", "experimental");
        Assert.assertEquals("foo", spec.getName());
        Assert.assertEquals("experimental", spec.getStability());
        spec.addParam(new GalleonFeatureParamSpec("p", null));
        spec.addParam(new GalleonFeatureParamSpec("p2", null));
        Assert.assertEquals(2, spec.getParams().size());
        Assert.assertEquals("p", spec.getParams().get(0).getName());
        Assert.assertNull(spec.getParams().get(0).getStability());
        Assert.assertEquals("p2", spec.getParams().get(1).getName());
        GalleonFeatureSpec spec2 = new GalleonFeatureSpec("foo", null);
        Assert.assertEquals("foo", spec2.getName());
        Assert.assertNull(spec2.getStability());

    }
}
