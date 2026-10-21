/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.type;

import org.jboss.galleon.ProvisioningException;

/**
 *
 * @author Alexey Loubyansky
 */
public class ParameterTypeConversionException extends ProvisioningException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public ParameterTypeConversionException(String message) {
        super(message);
    }

    public ParameterTypeConversionException(String message, Throwable t) {
        super(message, t);
    }
}
