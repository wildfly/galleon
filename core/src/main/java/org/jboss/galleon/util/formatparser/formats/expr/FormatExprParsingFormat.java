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
package org.jboss.galleon.util.formatparser.formats.expr;

import java.util.Collections;
import java.util.Map;

import org.jboss.galleon.util.formatparser.FormatContentHandler;
import org.jboss.galleon.util.formatparser.FormatErrors;
import org.jboss.galleon.util.formatparser.FormatParsingException;
import org.jboss.galleon.util.formatparser.ParsingContext;
import org.jboss.galleon.util.formatparser.ParsingFormat;
import org.jboss.galleon.util.formatparser.ParsingFormatBase;
import org.jboss.galleon.util.formatparser.formats.CollectionParsingFormat;
import org.jboss.galleon.util.formatparser.formats.CompositeParsingFormat;
import org.jboss.galleon.util.formatparser.formats.KeyValueParsingFormat;
import org.jboss.galleon.util.formatparser.formats.StringParsingFormat;
import org.jboss.galleon.util.formatparser.handlers.MapContentHandler;

/**
 *
 * @author Alexey Loubyansky
 */
public class FormatExprParsingFormat extends ParsingFormatBase {

    public static final String NAME = "FormatExpr";
    public static final String COMPOSITE_TYPE_FORMAT_NAME = "CompTypeExpr";
    public static final String LIST_TYPE_FORMAT_NAME = "ListTypeExpr";

    static final FormatExprParsingFormat INSTANCE = new FormatExprParsingFormat();

    private static final CollectionParsingFormat TYPE_PARAM_EXPR = CollectionParsingFormat.list(FormatExprTypeParamParsingFormat.getInstance(), '<', '>');

    private static final CompositeParsingFormat COMPOSITE_TYPE_FORMAT = CompositeParsingFormat.newInstance(
            COMPOSITE_TYPE_FORMAT_NAME, COMPOSITE_TYPE_FORMAT_NAME,
            KeyValueParsingFormat.newInstance(StringParsingFormat.getInstance(), ':', FormatExprParsingFormat.INSTANCE))
            .addElement("!name", StringParsingFormat.getInstance())
            .addElement("!content-type", StringParsingFormat.getInstance())
            .setAcceptAll(true);


    private static final ParsingFormat LIST_TYPE_FORMAT = new ParsingFormatBase(LIST_TYPE_FORMAT_NAME) {
        @Override
        public boolean isOpeningChar(char ch) {
            return ch == '[';
        }

        @Override
        public void pushed(ParsingContext ctx) throws FormatParsingException {
            if(ctx.charNow() != '[') {
                throw new FormatParsingException(FormatErrors.unexpectedStartingCharacter(this, '[', ctx.charNow()));
            }
        }

        @Override
        public void react(ParsingContext ctx) throws FormatParsingException {
            if(ctx.charNow() == ']') {
                ctx.end();
                return;
            }
            ctx.bounce();
        }

        @Override
        public void deal(ParsingContext ctx) throws FormatParsingException {
            if(Character.isWhitespace(ctx.charNow())) {
                return;
            }
            ctx.pushFormat(FormatExprParsingFormat.INSTANCE);
        }
    };

    public static class ListTypeContentHandler extends FormatContentHandler {

        private ParsingFormat param;

        public ListTypeContentHandler(ParsingFormat format, int strIndex) {
            super(format, strIndex);
        }

        public void addChild(FormatContentHandler childHandler) throws FormatParsingException {
            if(param != null) {
                throw new FormatParsingException("Format " + format + " type parameters have already been initialized");
            }
            param = (ParsingFormat) childHandler.getContent();
        }

        @Override
        public Object getContent() throws FormatParsingException {
            return Collections.singletonList(param == null ? CollectionParsingFormat.list() : CollectionParsingFormat.list(param));
        }
    }

    public static class CompositeTypeContentHandler extends MapContentHandler {

        public CompositeTypeContentHandler(ParsingFormat format, int strIndex) {
            super(format, strIndex);
        }

        @Override
        public Object getContent() throws FormatParsingException {
            if(map.isEmpty()) {
                return Collections.singletonList(CompositeParsingFormat.getInstance());
            }
            final String typeName = (String) map.get("!name");
            final String contentType = (String) map.get("!content-type");
            final CompositeParsingFormat format = CompositeParsingFormat.newInstance(typeName, contentType);
            for(Map.Entry<Object, Object> entry : map.entrySet()) {
                final String key = (String)entry.getKey();
                if(key.charAt(0) == '!') {
                    continue;
                }
                format.addElement(key, (ParsingFormat)entry.getValue());
            }
            return Collections.singletonList(format);
        }
    }

    public static FormatExprParsingFormat getInstance() {
        return INSTANCE;
    }

    protected FormatExprParsingFormat() {
        super(NAME);
    }

    @Override
    public boolean isOpeningChar(char ch) {
        return true;
    }

    @Override
    public void pushed(ParsingContext ctx) throws FormatParsingException {
        deal(ctx);
    }

    @Override
    public void deal(ParsingContext ctx) throws FormatParsingException {
        if(Character.isWhitespace(ctx.charNow())) {
            return;
        }
        switch(ctx.charNow()) {
            case '<':
                ctx.pushFormat(TYPE_PARAM_EXPR);
                break;
            case '[':
                ctx.pushFormat(LIST_TYPE_FORMAT);
                break;
            case '{':
                ctx.pushFormat(COMPOSITE_TYPE_FORMAT);
                break;
            default:
                ctx.content();
        }
    }
}
