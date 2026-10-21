/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.util.formatparser.formats;

/**
 *
 * @author Alexey Loubyansky
 */
public class ObjectParsingFormat extends MapParsingFormat {

    public static final String NAME = "Object";

    public static ObjectParsingFormat getInstance() {
        return new ObjectParsingFormat();
    }

    protected ObjectParsingFormat() {
        this(NAME);
    }

    protected ObjectParsingFormat(String name) {
        super(name);
        setEntryFormat(KeyValueParsingFormat.newInstance(StringParsingFormat.getInstance(), KeyValueParsingFormat.SEPARATOR, WildcardParsingFormat.getInstance(this)));
    }

    protected ObjectParsingFormat(String name, String contentType) {
        super(name, contentType);
    }

    protected ObjectParsingFormat(String name, KeyValueParsingFormat entryFormat) {
        super(name, entryFormat);
    }

    protected ObjectParsingFormat(String name, String contentType, KeyValueParsingFormat entryFormat) {
        super(name, contentType, entryFormat);
    }

    @Override
    public String toString() {
        return NAME;
    }
}
