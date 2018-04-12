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

import java.util.ArrayList;
import java.util.List;

import org.jboss.galleon.util.formatparser.FormatParsingException;
import org.jboss.galleon.util.formatparser.ParsingContext;
import org.jboss.galleon.util.formatparser.ParsingFormat;
import org.jboss.galleon.util.formatparser.ParsingFormatBase;

/**
 *
 * @author Alexey Loubyansky
 */
public class WildcardParsingFormat extends ParsingFormatBase {

    public static final String NAME = "?";

    public static WildcardParsingFormat getInstance() {
        return new WildcardParsingFormat();
    }

    public static WildcardParsingFormat getInstance(MapParsingFormat mapFormat) {
        return new WildcardParsingFormat(mapFormat);
    }

    protected List<ParsingFormat> formats;

    private WildcardParsingFormat() {
        super(NAME, true);
        formats = new ArrayList<>(3);
        formats.add(CollectionParsingFormat.list(this));
        formats.add(MapParsingFormat.getInstance(KeyValueParsingFormat.newInstance(this, this)));
    }

    private WildcardParsingFormat(MapParsingFormat mapFormat) {
        super(NAME, true);
        formats = new ArrayList<>(3);
        formats.add(CollectionParsingFormat.list(this));
        formats.add(mapFormat);
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
        final char ch = ctx.charNow();
        if(Character.isWhitespace(ch)) {
            return;
        }
        for(ParsingFormat format : formats) {
            if(!format.isOpeningChar(ch)) {
                continue;
            }
            ctx.pushFormat(format);
            return;
        }
        ctx.pushFormat(StringParsingFormat.getInstance());
    }
}
