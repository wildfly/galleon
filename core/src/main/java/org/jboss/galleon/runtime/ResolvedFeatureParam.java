/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.runtime;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.type.FeatureParameterType;

/**
 *
 * @author Alexey Loubyansky
 */
class ResolvedFeatureParam {

    final FeatureParameterSpec spec;
    final FeatureParameterType type;
    final Object defaultValue;

    ResolvedFeatureParam(FeatureParameterSpec spec, FeatureParameterType type) throws ProvisioningException {
        this.spec = spec;
        this.type = type;
        if(spec.hasDefaultValue()) {
            defaultValue = type.fromString(spec.getDefaultValue());
        } else {
            defaultValue = type.getDefaultValue();
        }
        if(spec.isFeatureId() && type.isCollection()) {
            throw new ProvisioningException("Feature ID parameter cannot be of a collection type");
        }
    }
}
