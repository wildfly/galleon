/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.util.formatparser.handlers;

import org.jboss.galleon.util.formatparser.FormatContentHandler;
import org.jboss.galleon.util.formatparser.FormatParsingException;
import org.jboss.galleon.util.formatparser.ParsingFormat;

/**
 * @author Alexey Loubyansky
 *
 */
public class WildcardContentHandler extends FormatContentHandler {

    private Object result;

    public WildcardContentHandler(ParsingFormat format, int strIndex) {
        super(format, strIndex);
    }

    /* (non-Javadoc)
     * @see org.jboss.galleon.spec.type.ParsingCallbackHandler#addChild(org.jboss.galleon.spec.type.ParsingCallbackHandler)
     */
    @Override
    public void addChild(FormatContentHandler childHandler) throws FormatParsingException {
        if(result != null) {
            throw new FormatParsingException("The value of the wildcard has already been initialized");
        }
        result = childHandler.getContent();
    }

    /* (non-Javadoc)
     * @see org.jboss.galleon.spec.type.ParsingCallbackHandler#getParsedValue()
     */
    @Override
    public Object getContent() throws FormatParsingException {
        return result;
    }

}
