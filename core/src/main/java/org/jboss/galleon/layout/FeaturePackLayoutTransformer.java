/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.layout;

import org.jboss.galleon.ProvisioningException;

/**
 *
 * @author Alexey Loubyansky
 */
public interface FeaturePackLayoutTransformer<F extends FeaturePackLayout, O extends FeaturePackLayout> {
    F transform(O other) throws ProvisioningException;
}