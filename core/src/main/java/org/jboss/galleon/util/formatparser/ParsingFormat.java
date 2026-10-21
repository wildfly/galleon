/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.util.formatparser;

/**
 *
 * @author Alexey Loubyansky
 */
public interface ParsingFormat {

    String getName();

    String getContentType();

    boolean isMap();

    boolean isCollection();

    boolean isWrapper();

    boolean isOpeningChar(char ch);

    void pushed(ParsingContext ctx) throws FormatParsingException;

    void react(ParsingContext ctx) throws FormatParsingException;

    void deal(ParsingContext ctx) throws FormatParsingException;

    void eol(ParsingContext ctx) throws FormatParsingException;
}
