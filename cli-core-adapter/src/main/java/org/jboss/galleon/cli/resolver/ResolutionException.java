/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.resolver;

/**
 *
 * @author jdenise@redhat.com
 */
public class ResolutionException extends Exception {

    ResolutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
