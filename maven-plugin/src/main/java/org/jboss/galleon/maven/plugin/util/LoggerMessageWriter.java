/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.maven.plugin.util;

import javax.inject.Singleton;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.jboss.galleon.MessageWriter;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@Singleton
public class LoggerMessageWriter extends AbstractLogEnabled implements MessageWriter {
    @Override
    public void verbose(final Throwable cause, final CharSequence message) {
        final Logger logger = getLogger();
        if (logger.isDebugEnabled()) {
            if (message != null) {
                logger.debug(message.toString(), cause);
            } else {
                logger.debug(null, cause);
            }
        }
    }

    @Override
    public void print(final Throwable cause, final CharSequence message) {
        final Logger logger = getLogger();
        if (logger.isInfoEnabled()) {
            if (message != null) {
                logger.info(message.toString(), cause);
            } else {
                logger.info(null, cause);
            }
        }
    }

    @Override
    public void error(final Throwable cause, final CharSequence message) {
        final Logger logger = getLogger();
        if (logger.isErrorEnabled()) {
            if (message != null) {
                logger.error(message.toString(), cause);
            } else {
                logger.error(null, cause);
            }
        }
    }

    @Override
    public boolean isVerboseEnabled() {
        return getLogger().isDebugEnabled();
    }

    @Override
    public void close() throws Exception {
        // nothing to do
    }
}
