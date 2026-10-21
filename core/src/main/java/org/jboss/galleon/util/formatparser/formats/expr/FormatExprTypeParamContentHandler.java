/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.util.formatparser.formats.expr;

import org.jboss.galleon.util.formatparser.FormatContentHandler;
import org.jboss.galleon.util.formatparser.FormatParsingException;
import org.jboss.galleon.util.formatparser.ParsingFormat;

/**
 *
 * @author Alexey Loubyansky
 */
public class FormatExprTypeParamContentHandler extends FormatContentHandler {

    private ParsingFormat type;

    public FormatExprTypeParamContentHandler(ParsingFormat format, int strIndex) {
        super(format, strIndex);
    }

    @Override
    public void addChild(FormatContentHandler childHandler) throws FormatParsingException {
        if(type != null) {
            throw new FormatParsingException("Type parameter of " + format + " has already been initialized to " + type);
        }
        type = (ParsingFormat) childHandler.getContent();
    }

    @Override
    public Object getContent() throws FormatParsingException {
        return type;
    }
}
