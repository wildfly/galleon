/*
 * Copyright 2016-2025 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.featurepack.layout.test;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.spec.FeaturePackSpec.Family;
import org.jboss.galleon.spec.FeaturePackSpec.Family.Criteria;
import org.junit.Assert;
import org.junit.Test;

public class FamilyTestCase {

    @Test
    public void testFamily() throws Exception {
        {
            Family f = Family.fromString("foo:c1,c2");
            Assert.assertEquals("foo", f.getName());
            Assert.assertEquals(2, f.getCriteria().size());
            Assert.assertTrue(f.getCriteria().contains(new Criteria("c1", false)));
            Assert.assertTrue(f.getCriteria().contains(new Criteria("c2", false)));
        }
        {
            boolean failed = true;
            try {
                Family f = Family.fromString("foo,c1,c2");
                failed = false;
            } catch (ProvisioningDescriptionException ex) {
                // OK
            }
            if (!failed) {
                throw new Exception("Test should have failed");
            }
        }
        {
            boolean failed = true;
            try {
                Family f = Family.fromString(":c1,c2");
                failed = false;
            } catch (ProvisioningDescriptionException ex) {
                // OK
            }
            if (!failed) {
                throw new Exception("Test should have failed");
            }
        }
        {
            boolean failed = true;
            try {
                Family f = Family.fromString("foo:,c2");
                failed = false;
            } catch (ProvisioningDescriptionException ex) {
                // OK
            }
            if (!failed) {
                throw new Exception("Test should have failed");
            }
        }
        {
            boolean failed = true;
            try {
                Family f = Family.fromString("foo:c1,c2,");
                failed = false;
            } catch (ProvisioningDescriptionException ex) {
                // OK
            }
            if (!failed) {
                throw new Exception("Test should have failed");
            }
        }
        {
            boolean failed = true;
            try {
                Family f = Family.fromString("foo:");
                failed = false;
            } catch (ProvisioningDescriptionException ex) {
                // OK
            }
            if (!failed) {
                throw new Exception("Test should have failed");
            }
        }
    }
}
