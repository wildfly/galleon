/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.util.formatparser.handlers;

import java.util.Collections;
import java.util.Set;

import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.formatparser.FormatContentHandler;
import org.jboss.galleon.util.formatparser.FormatParsingException;
import org.jboss.galleon.util.formatparser.ParsingFormat;

/**
 *
 * @author Alexey Loubyansky
 */
public class SetContentHandler extends FormatContentHandler {

    private Set<Object> set = Collections.emptySet();

    public SetContentHandler(ParsingFormat format, int strIndex) {
        super(format, strIndex);
    }

    @Override
    public void addChild(FormatContentHandler childHandler) throws FormatParsingException {
        set = CollectionUtils.add(set, childHandler.getContent());
    }

    @Override
    public Object getContent() throws FormatParsingException {
        return set;
    }
}
