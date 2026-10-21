/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon;


/**
 *
 * @author Alexey Loubyansky
 */
public class ProvisioningDescriptionException extends ProvisioningException {

    private static final long serialVersionUID = 1L;

    public ProvisioningDescriptionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProvisioningDescriptionException(String message) {
        super(message);
    }

}
