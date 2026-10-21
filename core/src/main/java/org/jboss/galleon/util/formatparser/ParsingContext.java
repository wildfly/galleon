/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.util.formatparser;

/**
 *
 * @author Alexey Loubyansky
 */
public interface ParsingContext {

    void pushFormat(ParsingFormat format) throws FormatParsingException;

    void popFormats() throws FormatParsingException;

    void bounce();

    void end() throws FormatParsingException;

    char charNow();

    boolean startsNow(String str);

    void content() throws FormatParsingException;
}
