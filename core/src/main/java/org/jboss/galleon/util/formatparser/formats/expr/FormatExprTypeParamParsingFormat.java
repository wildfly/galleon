/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.util.formatparser.formats.expr;

import org.jboss.galleon.util.formatparser.FormatParsingException;
import org.jboss.galleon.util.formatparser.ParsingContext;
import org.jboss.galleon.util.formatparser.ParsingFormatBase;

/**
 *
 * @author Alexey Loubyansky
 */
public class FormatExprTypeParamParsingFormat extends ParsingFormatBase {

    public static final String NAME = "TypeParam";

    private static FormatExprTypeParamParsingFormat INSTANCE;

    public static FormatExprTypeParamParsingFormat getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new FormatExprTypeParamParsingFormat();
        }
        return INSTANCE;
    }

    protected FormatExprTypeParamParsingFormat() {
        super(NAME);
    }

    @Override
    public boolean isOpeningChar(char ch) {
        return true;
    }

    @Override
    public boolean isWrapper() {
        return true;
    }

    @Override
    public void pushed(ParsingContext ctx) throws FormatParsingException {
        deal(ctx);
    }

    @Override
    public void react(ParsingContext ctx) throws FormatParsingException {
    }

    @Override
    public void deal(ParsingContext ctx) throws FormatParsingException {
        if(Character.isWhitespace(ctx.charNow())) {
            return;
        }
        ctx.pushFormat(FormatExprParsingFormat.INSTANCE);
    }
}