/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.diff;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.state.ProvisionedFeature;

/**
 *
 * @author Alexey Loubyansky
 */
public interface ProvisionedFeatureDiffCallback {

    ProvisionedFeatureDiffCallback DEFAULT = new ProvisionedFeatureDiffCallback() {};

    default boolean added(ProvisionedFeature added) throws ProvisioningException {
        return true;
    }

    default boolean removed(ProvisionedFeature removed) throws ProvisioningException {
        return true;
    }

    default boolean matches(ProvisionedFeature provisioned, ProvisionedFeature actual) throws ProvisioningException {
        return provisioned.equals(actual);
    }
}
