/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.util.formatparser.handlers;

import java.util.Collections;
import java.util.Map;

import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.formatparser.FormatContentHandler;
import org.jboss.galleon.util.formatparser.FormatErrors;
import org.jboss.galleon.util.formatparser.FormatParsingException;
import org.jboss.galleon.util.formatparser.ParsingFormat;
import org.jboss.galleon.util.formatparser.formats.KeyValueParsingFormat;
import org.jboss.galleon.util.formatparser.formats.MapParsingFormat;

/**
 *
 * @author Alexey Loubyansky
 */
public class MapContentHandler extends FormatContentHandler {

    protected Map<Object, Object> map = Collections.emptyMap();

    public MapContentHandler(ParsingFormat format, int strIndex) {
        super(format, strIndex);
    }

    /* (non-Javadoc)
     * @see org.jboss.galleon.spec.type.ParsingCallbackHandler#addChild(org.jboss.galleon.spec.type.ParsingCallbackHandler)
     */
    @Override
    public void addChild(FormatContentHandler childHandler) throws FormatParsingException {
        if(!childHandler.getFormat().getName().equals(KeyValueParsingFormat.NAME)) {
            throw new FormatParsingException(FormatErrors.unexpectedChildFormat(format, childHandler.getFormat()));
        }
        final KeyValueContentHandler entry = (KeyValueContentHandler) childHandler;
        final MapParsingFormat objectFormat = (MapParsingFormat)format;
        if(objectFormat.isAcceptsKey(entry.key)) {
            map = CollectionUtils.putLinked(map, entry.key, entry.value);
        } else {
            throw new FormatParsingException(FormatErrors.unexpectedCompositeFormatElement(format, entry.key));
        }
    }

    /* (non-Javadoc)
     * @see org.jboss.galleon.spec.type.ParsingCallbackHandler#getParsedValue()
     */
    @Override
    public Object getContent() throws FormatParsingException {
        return map;
    }
}
