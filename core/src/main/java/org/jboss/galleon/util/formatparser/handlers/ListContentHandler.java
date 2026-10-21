/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.util.formatparser.handlers;

import java.util.Collections;
import java.util.List;

import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.formatparser.FormatContentHandler;
import org.jboss.galleon.util.formatparser.FormatParsingException;
import org.jboss.galleon.util.formatparser.ParsingFormat;

/**
 *
 * @author Alexey Loubyansky
 */
public class ListContentHandler extends FormatContentHandler {

    private List<Object> list = Collections.emptyList();

    public ListContentHandler(ParsingFormat format, int strIndex) {
        super(format, strIndex);
    }

    @Override
    public void addChild(FormatContentHandler childHandler) throws FormatParsingException {
        list = CollectionUtils.add(list, childHandler.getContent());
    }

    @Override
    public Object getContent() throws FormatParsingException {
        return list;
    }
}
