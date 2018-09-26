/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        log.errorf("Exception while completing: %s", ex.getLocalizedMessage());
    }

    static void exceptionResolving(Throwable ex) {
        log.errorf("Exception while resolving: %s", ex.getLocalizedMessage());
    }
}
