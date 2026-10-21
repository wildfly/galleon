/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.xml.util;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
public class TextNode extends Node {

    private final String text;

    public TextNode(final String text){
        this.text = text;
    }

    @Override
    public void marshall(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeCharacters(text);
    }
}
