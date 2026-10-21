/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.type.builtin;

import org.jboss.galleon.Constants;
import org.jboss.galleon.type.FeatureParameterType;
import org.jboss.galleon.type.ParameterTypeNotFoundException;
import org.jboss.galleon.type.ParameterTypeProvider;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;
import org.jboss.galleon.util.formatparser.FormatParser;
import org.jboss.galleon.util.formatparser.FormatParsingException;

/**
 * @author Alexey Loubyansky
 *
 */
public class BuiltInParameterTypeProvider implements ParameterTypeProvider {

    private static final BuiltInParameterTypeProvider INSTANCE = new BuiltInParameterTypeProvider();

    public static BuiltInParameterTypeProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public FeatureParameterType getType(ProducerSpec producer, String name) throws ParameterTypeNotFoundException {
        if(Constants.BUILT_IN_TYPE_STRING.equals(name)) {
            return StringParameterType.getInstance();
        } else {
            try {
                return new FormattedParameterType(FormatParser.resolveFormat(name));
            } catch (FormatParsingException e) {
                throw new ParameterTypeNotFoundException("Failed to resolve parameter type " + name, e);
            }
        }
    }
}
