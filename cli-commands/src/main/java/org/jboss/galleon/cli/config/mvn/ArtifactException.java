/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.config.mvn;

import org.jboss.galleon.ProvisioningException;

/**
 *
 * @author Alexey Loubyansky
 */
public class ArtifactException extends ProvisioningException {

    private static final long serialVersionUID = 1L;

    public ArtifactException(String message) {
        super(message);
    }

    public ArtifactException(String message, Throwable cause) {
        super(message, cause);
    }
}
