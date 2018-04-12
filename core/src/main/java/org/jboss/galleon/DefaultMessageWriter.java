/*
 * Copyright ${license.git.copyrightYears} Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon;

import java.io.PrintStream;

/**
 * A default {@link MessageWriter}. The default instance simply writes to {@link System#out stdout} for verbose and
 * informational messages and {@link System#err stderr} for error messages.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class DefaultMessageWriter implements MessageWriter {

    private static class Holder {
        static final DefaultMessageWriter INSTANCE = new DefaultMessageWriter();
    }

    private final boolean verbose;
    private final PrintStream stdout;
    private final PrintStream stderr;
    private final boolean closeStreams;

    /**
     * Creates a new message writer which writes to {@link System#out stdout} for verbose and informational messages
     * and {@link System#err stderr} for error messages.
     */
    public DefaultMessageWriter() {
        this(System.out, System.err);
    }

    /**
     * Creates a new message writer which will write messages to the streams provided.
     * <p>
     * Note that this will not print verbose messages nor with the streams be closed by this instance.
     * </p>
     *
     * @param stdout the stream to write verbose and informational messages to
     * @param stderr the stream to write error messages to
     */
    public DefaultMessageWriter(final PrintStream stdout, final PrintStream stderr) {
        this(stdout, stderr, false);
    }

    /**
     * Creates a new message writer which will write messages to the streams provided.
     * <p>
     * Note that the streams be closed by this instance.
     * </p>
     *
     * @param stdout  the stream to write verbose and informational messages to
     * @param stderr  the stream to write error messages to
     * @param verbose {@code true} if verbose messages should be written, otherwise {@code false}
     */
    public DefaultMessageWriter(final PrintStream stdout, final PrintStream stderr, final boolean verbose) {
        this(stdout, stderr, verbose, false);
    }

    /**
     * Creates a new message writer which will write messages to the streams provided.
     *
     * @param stdout       the stream to write verbose and informational messages to
     * @param stderr       the stream to write error messages to
     * @param verbose      {@code true} if verbose messages should be written, otherwise {@code false}
     * @param closeStreams {@code true} if the streams should be closed, otherwise {@code false} if the closing of the
     *                     streams will be handled elsewhere
     */
    public DefaultMessageWriter(final PrintStream stdout, final PrintStream stderr, final boolean verbose, final boolean closeStreams) {
        this.stdout = stdout;
        this.stderr = stderr;
        this.verbose = verbose;
        this.closeStreams = closeStreams;
    }

    /**
     * Returns a default instance which writes to {@link System#out stdout} for verbose and informational messages and
     * {@link System#err stderr} for error messages.
     *
     * @return a default static instance
     */
    public static DefaultMessageWriter getDefaultInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public void verbose(final Throwable cause, final CharSequence message) {
        if (isVerboseEnabled()) {
            if (message != null) {
                stdout.println(message);
            }
            if (cause != null) {
                cause.printStackTrace(stdout);
            }
        }
    }

    @Override
    public void print(final Throwable cause, final CharSequence message) {
        if (message != null) {
            stdout.println(message);
        }
        if (cause != null) {
            cause.printStackTrace(stdout);
        }
    }

    @Override
    public void error(final Throwable cause, final CharSequence message) {
        if (message != null) {
            stderr.println(message);
        }
        if (cause != null) {
            cause.printStackTrace(stderr);
        }
    }

    @Override
    public boolean isVerboseEnabled() {
        return verbose;
    }

    @Override
    public void close() throws Exception {
        if (closeStreams) {
            try {
                stdout.close();
            } finally {
                stderr.close();
            }
        }
    }
}
