/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
