/*
 * Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
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

import java.util.Collection;

import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.runtime.CapabilityResolver;
import org.jboss.galleon.type.FeatureParameterType;
import org.jboss.galleon.type.ParameterTypeConversionException;
import org.jboss.galleon.util.formatparser.FormatParser;
import org.jboss.galleon.util.formatparser.FormatParsingException;
import org.jboss.galleon.util.formatparser.ParsingFormat;

/**
 *
 * @author Alexey Loubyansky
 */
public class FormattedParameterType implements FeatureParameterType {

    protected final ParsingFormat format;

    protected FormattedParameterType(ParsingFormat format) {
        this.format = format;
    }

    /* (non-Javadoc)
     * @see org.jboss.galleon.type.FeatureParameterType#getName()
     */
    @Override
    public String getName() {
        return format.getName();
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    @Override
    public Object fromString(String str) throws ParameterTypeConversionException {
        if(str == null) {
            return null;
        }
        try {
            return FormatParser.parse(format, str);
        } catch (FormatParsingException e) {
            throw new ParameterTypeConversionException("Failed to parse " + getName() + " parameter value", e);
        }
    }

    @Override
    public String toString(Object o) throws ParameterTypeConversionException {
        return o == null ? null : o.toString();
    }

    @Override
    public boolean isCollection() {
        return format.isCollection();
    }

    @Override
    public boolean isMergeable() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.jboss.galleon.type.FeatureParameterType#merge(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object merge(Object original, Object other) throws ProvisioningException {
        return other;
    }

    @Override
    public boolean resolveCapabilityElement(CapabilityResolver capResolver, Object o) throws ProvisioningException {
        if(!format.isCollection()) {
            capResolver.add(o);
            return true;
        }
        final Collection<?> col = (Collection<?>)o;
        if(col.isEmpty()) {
            if (capResolver.getSpec().isOptional()) {
                return false;
            }
            throw new ProvisioningException(Errors.capabilityMissingParameter(capResolver.getSpec(), capResolver.getElem()));
        }
        capResolver.multiply(col);
        return true;
    }
}
