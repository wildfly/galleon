/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
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
            return new FormatExprTypeParamParsingFormat();
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