/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.util.formatparser.handlers;

import org.jboss.galleon.util.formatparser.FormatContentHandler;
import org.jboss.galleon.util.formatparser.FormatParsingException;
import org.jboss.galleon.util.formatparser.ParsingFormat;

/**
 *
 * @author Alexey Loubyansky
 */
public class StringContentHandler extends FormatContentHandler {

    private final StringBuilder buf = new StringBuilder();

    public StringContentHandler(ParsingFormat format, int strIndex) {
        super(format, strIndex);
    }

    @Override
    public void character(char ch) throws FormatParsingException {
        buf.append(ch);
    }

    @Override
    public Object getContent() throws FormatParsingException {
        return buf.toString().trim();
    }
}
