/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
