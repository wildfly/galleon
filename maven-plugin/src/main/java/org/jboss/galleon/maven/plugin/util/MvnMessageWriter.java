/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.maven.plugin.util;

import org.apache.maven.plugin.logging.Log;
import org.jboss.galleon.MessageWriter;

/**
 *
 * @author Alexey Loubyansky
 */
public class MvnMessageWriter implements MessageWriter {

    private final Log log;

    public MvnMessageWriter(Log log) {
        this.log = log;
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public void verbose(Throwable cause, CharSequence message) {
        if(log.isDebugEnabled()) {
            log.debug(message, cause);
        }
    }

    @Override
    public void print(Throwable cause, CharSequence message) {
        log.info(message, cause);
    }

    @Override
    public void error(Throwable cause, CharSequence message) {
        log.error(message, cause);
    }

    @Override
    public boolean isVerboseEnabled() {
        return log.isDebugEnabled();
    }

}
