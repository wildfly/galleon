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
public class CDataNode extends Node {
    private final String cdata;

    public CDataNode(final String cdata) {
        this.cdata = cdata;
    }

    @Override
    public void marshall(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeCData(cdata);
    }
}
