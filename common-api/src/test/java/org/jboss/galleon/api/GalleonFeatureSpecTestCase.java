/*
 * Copyright 2016-2024 Red Hat, Inc. and/or its affiliates
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
