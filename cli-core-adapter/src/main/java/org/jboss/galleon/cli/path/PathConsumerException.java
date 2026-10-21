/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.path;

/**
 *
 * @author jdenise@redhat.com
 */
public class PathConsumerException extends Exception {
    PathConsumerException(String msg, Exception cause) {
        super(msg, cause);
    }
    PathConsumerException(String msg) {
        super(msg);
    }
}
