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

import org.jboss.galleon.util.formatparser.FormatParsingException;
import org.jboss.galleon.util.formatparser.ParsingContext;
import org.jboss.galleon.util.formatparser.ParsingFormat;
import org.jboss.galleon.util.formatparser.ParsingFormatBase;

/**
 *
 * @author Alexey Loubyansky
 */
public class KeyValueParsingFormat extends ParsingFormatBase {

    public static final String NAME = "KeyValue";

    public static final char SEPARATOR = '=';

    public static KeyValueParsingFormat getInstance() {
        final WildcardParsingFormat wildcard = WildcardParsingFormat.getInstance();
        return new KeyValueParsingFormat(wildcard, SEPARATOR, wildcard);
    }

    public static KeyValueParsingFormat newInstance(ParsingFormat keyFormat, ParsingFormat valueFormat) {
        return newInstance(keyFormat, SEPARATOR, valueFormat);
    }

    public static KeyValueParsingFormat newInstance(ParsingFormat keyFormat, char separator, ParsingFormat valueFormat) {
        return new KeyValueParsingFormat(keyFormat, separator, valueFormat);
    }

    private final ParsingFormat keyFormat;
    private final char separator;
    private final ParsingFormat valueFormat;

    protected KeyValueParsingFormat(ParsingFormat keyFormat, char separator, ParsingFormat valueFormat) {
        super(NAME);
        this.keyFormat = keyFormat;
        this.separator = separator;
        this.valueFormat = valueFormat;
    }

    public ParsingFormat getKeyFormat() {
        return keyFormat;
    }

    public char getSeparator() {
        return separator;
    }

    public ParsingFormat getValueFormat() {
        return valueFormat;
    }

    @Override
    public boolean isOpeningChar(char ch) {
        return keyFormat.isOpeningChar(ch);
    }

    @Override
    public void react(ParsingContext ctx) throws FormatParsingException {
        if(ctx.charNow() == separator) {
            ctx.popFormats();
        }
    }

    @Override
    public void pushed(ParsingContext ctx) throws FormatParsingException {
        ctx.pushFormat(keyFormat);
    }

    @Override
    public void deal(ParsingContext ctx) throws FormatParsingException {
        if(Character.isWhitespace(ctx.charNow())) {
            return;
        }
        ctx.pushFormat(valueFormat);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + separator;
        result = prime * result + ((valueFormat == null) ? 0 : valueFormat.hashCode());
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
        KeyValueParsingFormat other = (KeyValueParsingFormat) obj;
        if (separator != other.separator)
            return false;
        if (valueFormat == null) {
            if (other.valueFormat != null)
                return false;
        } else if (!valueFormat.equals(other.valueFormat))
            return false;
        return true;
    }
}
