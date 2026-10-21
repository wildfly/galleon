/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.util.formatparser;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class FormatContentHandler {

    protected final ParsingFormat format;
    protected final int strIndex;

    public FormatContentHandler(ParsingFormat format, int strIndex) {
        this.format = format;
        this.strIndex = strIndex;
    }

    public ParsingFormat getFormat() {
        return format;
    }

    public void addChild(FormatContentHandler childHandler) throws FormatParsingException {
        throw new FormatParsingException("Format " + format + " does not support nested formats");
    }

    public void character(char ch) throws FormatParsingException {
        throw new FormatParsingException("Format " + format + " does not support character content");
    }

    public abstract Object getContent() throws FormatParsingException;
}
