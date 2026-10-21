/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.type.builtin;

import org.jboss.galleon.Constants;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.runtime.CapabilityResolver;
import org.jboss.galleon.type.FeatureParameterType;
import org.jboss.galleon.type.ParameterTypeConversionException;

/**
 *
 * @author Alexey Loubyansky
 */
public class StringParameterType implements FeatureParameterType {

    private static final StringParameterType INSTANCE = new StringParameterType();

    public static final StringParameterType getInstance() {
        return INSTANCE;
    }

    @Override
    public String getName() {
        return Constants.BUILT_IN_TYPE_STRING;
    }

    @Override
    public String getDefaultValue() {
        return null;
    }

    @Override
    public String fromString(String str) throws ParameterTypeConversionException {
        return str;
    }

    @Override
    public String toString(Object o) throws ParameterTypeConversionException {
        return (String) o;
    }

    @Override
    public boolean isCollection() {
        return false;
    }

    @Override
    public boolean isMergeable() {
        return false;
    }

    @Override
    public Object merge(Object original, Object other) throws ProvisioningException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean resolveCapabilityElement(CapabilityResolver capResolver, Object o) throws ProvisioningException {
        capResolver.add(o);
        return true;
    }
}
