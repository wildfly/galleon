/*
 * Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
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
