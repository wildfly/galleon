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
package org.jboss.galleon;

import java.io.PrintStream;

/**
 * This API allows messages to be written to the tools target output. The tool itself will determine where and out
 * messages are written.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface MessageWriter extends AutoCloseable {

    /**
     * Prints a message if {@link #isVerboseEnabled()} is {@code true}.
     *
     * @param message the message to print, may be {@code null}
     */
    default void verbose(CharSequence message) {
        verbose(null, message);
    }

    /**
     * Prints a message if {@link #isVerboseEnabled()} is {@code true}.
     * <p>
     * The message will not be formatted unless {@link #isVerboseEnabled()} is {@code true}. This is safe to call
     * without first checking {@link #isVerboseEnabled()} for performance.
     * </p>
     *
     * @param format the format
     * @param args the arguments for the format
     *
     * @see java.util.Formatter
     */
    default void verbose(String format, Object... args) {
        if (isVerboseEnabled()) {
            verbose(null, String.format(format, args));
        }
    }

    /**
     * Prints a message if {@link #isVerboseEnabled()} is {@code true}. If the {@code cause} is not {@code null} the
     * {@linkplain Throwable#printStackTrace(PrintStream) stack trace} will be written as well.
     *
     * @param cause the cause of an error or {@code null}
     * @param message the message to print, may be {@code null}
     */
    void verbose(Throwable cause, CharSequence message);

    /**
     * Prints a message if {@link #isVerboseEnabled()} is {@code true}. If the {@code cause} is not {@code null} the
     * {@linkplain Throwable#printStackTrace(PrintStream) stack trace} will be written as well.
     * <p>
     * The message will not be formatted unless {@link #isVerboseEnabled()} is {@code true}. This is safe to call
     * without first checking {@link #isVerboseEnabled()} for performance.
     * </p>
     *
     * @param cause the cause of an error or {@code null}
     * @param format the format
     * @param args the arguments for the format
     *
     * @see java.util.Formatter
     */
    default void verbose(Throwable cause, String format, Object... args) {
        if (isVerboseEnabled()) {
            verbose(cause, String.format(format, args));
        }
    }

    /**
     * Prints an informational message.
     *
     * @param message the message to print, may be {@code null}
     */
    default void print(CharSequence message) {
        print(null, message);
    }

    /**
     * Prints an informational message.
     *
     * @param format the format
     * @param args the arguments for the format
     *
     * @see java.util.Formatter
     */
    default void print(String format, Object... args) {
        print(null, String.format(format, args));
    }

    /**
     * Prints an informational message. If the {@code cause} is not {@code null} the
     * {@linkplain Throwable#printStackTrace(PrintStream) stack trace} will be written as well.
     *
     * @param cause the cause of an error or {@code null}
     * @param message the message to print, may be {@code null}
     */
    void print(Throwable cause, CharSequence message);

    /**
     * Prints an informational message. If the {@code cause} is not {@code null} the
     * {@linkplain Throwable#printStackTrace(PrintStream) stack trace} will be written as well.
     *
     * @param cause the cause of an error or {@code null}
     * @param format the format
     * @param args the arguments for the format
     *
     * @see java.util.Formatter
     */
    default void print(Throwable cause, String format, Object... args) {
        print(cause, String.format(format, args));
    }

    /**
     * Prints an error message.
     *
     * @param message the message to print, may be {@code null}
     */
    default void error(CharSequence message) {
        print(null, message);
    }

    /**
     * Prints an error message.
     *
     * @param format the format
     * @param args the arguments for the format
     *
     * @see java.util.Formatter
     */
    default void error(String format, Object... args) {
        print(null, String.format(format, args));
    }

    /**
     * Prints an error message. If the {@code cause} is not {@code null} the
     * {@linkplain Throwable#printStackTrace(PrintStream) stack trace} will be written as well.
     *
     * @param cause the cause of an error or {@code null}
     * @param message the message to print, may be {@code null}
     */
    void error(Throwable cause, CharSequence message);

    /**
     * Prints an error message. If the {@code cause} is not {@code null} the
     * {@linkplain Throwable#printStackTrace(PrintStream) stack trace} will be written as well.
     *
     * @param cause the cause of an error or {@code null}
     * @param format the format
     * @param args the arguments for the format
     *
     * @see java.util.Formatter
     */
    default void error(Throwable cause, String format, Object... args) {
        print(cause, String.format(format, args));
    }

    /**
     * Indicates whether or not verbose output should be printed.
     *
     * @return {@code true} if verbose output should be printed, otherwise {@code false}
     */
    boolean isVerboseEnabled();
}
