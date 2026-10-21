/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli;

import org.jboss.logging.Logger;

/**
 *
 * @author jdenise@redhat.com
 */
public interface CliLogging {

    Logger log = Logger.getLogger("org.jboss.galleon.cli");

    static void exception(Throwable ex) {
        log.error(null, ex);
    }

    static void error(String msg) {
        log.errorf("Error: %s", msg);
    }

    static void commandNotFound(String cmd) {
        log.errorf("Command named %s was not found.", cmd);
    }

    static void completionException(Throwable ex) {
        log.errorf("Exception while completing: %s", ex.getLocalizedMessage());
    }

    static void exceptionResolvingBuiltinUniverse(Throwable ex) {
        log.errorf("Exception resolving builtin universe: %s", ex.getLocalizedMessage());
    }

    static void exceptionResolving(Throwable ex) {
        log.errorf("Exception while resolving: %s", ex.getLocalizedMessage());
    }
}
