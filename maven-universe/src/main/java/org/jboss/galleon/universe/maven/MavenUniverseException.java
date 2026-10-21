/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.universe.maven;

import org.jboss.galleon.ProvisioningException;

/**
 *
 * @author Alexey Loubyansky
 */
public class MavenUniverseException extends ProvisioningException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public MavenUniverseException(String message, Throwable cause) {
        super(message, cause);
    }

    public MavenUniverseException(String message) {
        super(message);
    }
}
