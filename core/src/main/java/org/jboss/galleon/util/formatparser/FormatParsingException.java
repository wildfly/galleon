/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.util.formatparser;

/**
 *
 * @author Alexey Loubyansky
 */
public class FormatParsingException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public FormatParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public FormatParsingException(String message) {
        super(message);
    }
}
