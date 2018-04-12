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
public class MapParsingFormat extends ParsingFormatBase {

    public static final String NAME = "Map";
    public static final char OPENING_CHAR = '{';
    public static final char CLOSING_CHAR = '}';
    public static final char ENTRY_SEPARATOR_CHAR = ',';

    public static MapParsingFormat getInstance() {
        return new MapParsingFormat();
    }

    public static MapParsingFormat getInstance(ParsingFormat key, ParsingFormat value) {
        return getInstance(KeyValueParsingFormat.newInstance(key, value));
    }

    public static MapParsingFormat getInstance(KeyValueParsingFormat entryFormat) {
        return new MapParsingFormat(entryFormat);
    }

    protected KeyValueParsingFormat entryFormat;

    protected MapParsingFormat() {
        this(NAME, KeyValueParsingFormat.getInstance());
    }

    protected MapParsingFormat(KeyValueParsingFormat entryFormat) {
        this(NAME, entryFormat);
    }

    protected MapParsingFormat(String name) {
        this(name, KeyValueParsingFormat.getInstance());
    }

    protected MapParsingFormat(String name, String contentType) {
        this(name, contentType, KeyValueParsingFormat.getInstance());
    }

    protected MapParsingFormat(String name, KeyValueParsingFormat entryFormat) {
        this(name, NAME, entryFormat);
    }

    protected MapParsingFormat(String name, String contentType, KeyValueParsingFormat entryFormat) {
        super(name, contentType == null ? NAME : contentType);
        this.entryFormat = entryFormat;
    }

    public boolean isAcceptsKey(Object key) {
        return true;
    }

    public MapParsingFormat setEntryFormat(KeyValueParsingFormat entryFormat) {
        this.entryFormat = entryFormat;
        return this;
    }

    @Override
    public boolean isMap() {
        return true;
    }

    @Override
    public boolean isOpeningChar(char ch) {
        return ch == OPENING_CHAR;
    }

    @Override
    public void pushed(ParsingContext ctx) throws FormatParsingException {
        if(ctx.charNow() != OPENING_CHAR) {
            throw new FormatParsingException(FormatErrors.unexpectedStartingCharacter(this, OPENING_CHAR, ctx.charNow()));
        }
    }

    @Override
    public void react(ParsingContext ctx) throws FormatParsingException {
        switch(ctx.charNow()) {
            case ENTRY_SEPARATOR_CHAR :
                ctx.popFormats();
                break;
            case CLOSING_CHAR:
                ctx.end();
                break;
            default:
                ctx.bounce();
        }
    }

    @Override
    public void deal(ParsingContext ctx) throws FormatParsingException {
        if(Character.isWhitespace(ctx.charNow())) {
            return;
        }
        ctx.pushFormat(entryFormat);
    }

    @Override
    public void eol(ParsingContext ctx) throws FormatParsingException {
        throw new FormatParsingException(FormatErrors.formatIncomplete(this));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((entryFormat == null) ? 0 : entryFormat.hashCode());
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
        MapParsingFormat other = (MapParsingFormat) obj;
        if (entryFormat == null) {
            if (other.entryFormat != null)
                return false;
        } else if (!entryFormat.equals(other.entryFormat))
            return false;
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append(NAME).append('<').append(entryFormat.getKeyFormat()).append(',').append(entryFormat.getValueFormat());
        return buf.append('>').toString();
    }
}
