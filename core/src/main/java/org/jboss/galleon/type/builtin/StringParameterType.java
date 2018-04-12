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
