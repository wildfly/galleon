/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.util;

import org.jboss.galleon.ProvisioningException;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeaturePackInstallException extends ProvisioningException {

    private static final long serialVersionUID = 1L;

    public FeaturePackInstallException(String message, Throwable cause) {
        super(message, cause);
    }

    public FeaturePackInstallException(String message) {
        super(message);
    }
}
