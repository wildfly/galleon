/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.util.formatparser.formats;

import org.jboss.galleon.util.formatparser.FormatParsingException;
import org.jboss.galleon.util.formatparser.ParsingContext;
import org.jboss.galleon.util.formatparser.ParsingFormatBase;

/**
 *
 * @author Alexey Loubyansky
 */
public class StringParsingFormat extends ParsingFormatBase {

    public static final String NAME = "String";

    private static final StringParsingFormat INSTANCE = new StringParsingFormat();

    public static StringParsingFormat getInstance() {
        return INSTANCE;
    }

    protected StringParsingFormat() {
        super(NAME);
    }

    @Override
    public boolean isOpeningChar(char ch) {
        return true;
    }

    @Override
    public void pushed(ParsingContext ctx) throws FormatParsingException {
        ctx.content();
    }

    @Override
    public void deal(ParsingContext ctx) throws FormatParsingException {
        ctx.content();
    }
}
