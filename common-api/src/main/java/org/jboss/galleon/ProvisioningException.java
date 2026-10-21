/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon;

/**
 *
 * @author Alexey Loubyansky
 */
public class ProvisioningException extends Exception {

    private static final long serialVersionUID = 1L;

    public ProvisioningException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProvisioningException(String message) {
        super(message);
    }

    public ProvisioningException(Throwable cause) {
        super(cause);
    }
}
