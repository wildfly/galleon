/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.util.formatparser;

/**
 *
 * @author Alexey Loubyansky
 */
public interface FormatContentHandlerFactory {

    FormatContentHandler forFormat(ParsingFormat format, int strIndex) throws FormatParsingException;
}
