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
package org.jboss.galleon.util.formatparser.formats;

import org.jboss.galleon.util.formatparser.FormatErrors;
import org.jboss.galleon.util.formatparser.FormatParsingException;
import org.jboss.galleon.util.formatparser.ParsingContext;
import org.jboss.galleon.util.formatparser.ParsingFormat;
import org.jboss.galleon.util.formatparser.ParsingFormatBase;

/**
 *
 * @author Alexey Loubyansky
 */
public class CollectionParsingFormat extends ParsingFormatBase {

    public static final String LIST = "List";
    public static final String SET = "Set";

    public static final char OPENING_CHAR = '[';
    public static final char CLOSING_CHAR = ']';
    public static final char ITEM_SEPARATOR_CHAR = ',';

    public static CollectionParsingFormat list() {
        return newInstance(LIST);
    }

    public static CollectionParsingFormat list(ParsingFormat itemFormat) {
        return newInstance(LIST, itemFormat);
    }

    public static CollectionParsingFormat list(ParsingFormat itemFormat, char openingChar, char closingChar) {
        return newInstance(LIST, itemFormat, openingChar, closingChar);
    }

    public static CollectionParsingFormat set() {
        return newInstance(SET);
    }

    public static CollectionParsingFormat set(ParsingFormat itemFormat) {
        return newInstance(SET, itemFormat);
    }

    public static CollectionParsingFormat set(ParsingFormat itemFormat, char openingChar, char closingChar) {
        return newInstance(SET, itemFormat, openingChar, closingChar);
    }

    public static CollectionParsingFormat newInstance(String type) {
        return newInstance(type, WildcardParsingFormat.getInstance());
    }

    public static CollectionParsingFormat newInstance(String type, ParsingFormat itemFormat) {
        return newInstance(type, itemFormat, OPENING_CHAR, CLOSING_CHAR);
    }

    public static CollectionParsingFormat newInstance(String type, ParsingFormat itemFormat, char openingChar, char closingChar) {
        return new CollectionParsingFormat(type, openingChar, closingChar, itemFormat);
    }

    private final char openingChar;
    private final char closingChar;
    private final ParsingFormat itemFormat;

    protected CollectionParsingFormat(String name, char openingChar, char closingChar, ParsingFormat itemFormat) {
        super(name);
        this.itemFormat = itemFormat;
        this.openingChar = openingChar;
        this.closingChar = closingChar;
    }

    @Override
    public boolean isCollection() {
        return true;
    }

    @Override
    public boolean isOpeningChar(char ch) {
        return openingChar == ch;
    }

    @Override
    public void pushed(ParsingContext ctx) throws FormatParsingException {
        if(ctx.charNow() != openingChar) {
            throw new FormatParsingException(FormatErrors.unexpectedStartingCharacter(this, openingChar, ctx.charNow()));
        }
    }

    @Override
    public void react(ParsingContext ctx) throws FormatParsingException {
        final char charNow = ctx.charNow();
        if(charNow == ITEM_SEPARATOR_CHAR) {
            ctx.popFormats();
            return;
        }
        if(charNow == closingChar) {
            ctx.end();
            return;
        }
        ctx.bounce();
    }

    @Override
    public void deal(ParsingContext ctx) throws FormatParsingException {
        if(!Character.isWhitespace(ctx.charNow())) {
            ctx.pushFormat(itemFormat);
        }
    }

    @Override
    public void eol(ParsingContext ctx) throws FormatParsingException {
        throw new FormatParsingException(FormatErrors.formatIncomplete(this));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((itemFormat == null) ? 0 : itemFormat.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        CollectionParsingFormat other = (CollectionParsingFormat) obj;
        if (itemFormat == null) {
            if (other.itemFormat != null)
                return false;
        } else if (!itemFormat.equals(other.itemFormat))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return name + "<" + itemFormat + ">";
    }
}
