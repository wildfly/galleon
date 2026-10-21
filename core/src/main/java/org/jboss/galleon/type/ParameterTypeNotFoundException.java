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
public class ParameterTypeNotFoundException extends ProvisioningException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public ParameterTypeNotFoundException(String message) {
        super(message);
    }

    public ParameterTypeNotFoundException(String message, Throwable t) {
        super(message, t);
    }
}
