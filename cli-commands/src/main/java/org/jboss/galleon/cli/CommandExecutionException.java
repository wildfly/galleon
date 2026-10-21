/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli;

/**
 *
 * @author Alexey Loubyansky
 */
public class CommandExecutionException extends Exception {

    private static final long serialVersionUID = 1L;
    private final PmSession session;

    public CommandExecutionException(PmSession session, String message, Throwable cause) {
        super(message, cause);
        this.session = session;
    }

    public CommandExecutionException(String message) {
        super(message);
        this.session = null;
    }

    public PmSession getPmSession() {
        return session;
    }
}
