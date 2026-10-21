/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.xml.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
public class ProcessingInstructionNode extends Node {
    private final Map<String, String> data;
    private List<Node> delegates = new ArrayList<Node>();

    public ProcessingInstructionNode(final Map<String, String> data) {
        this.data = data;
    }

    public void addDelegate(Node delegate) {
        if (delegate != null) {
            delegates.add(delegate);
        }
    }

    public String getDataValue(String name, String defaultValue) {
        if (data != null) {
            String s = data.get(name);
            if (s != null) {
                return s;
            }
        }
        return defaultValue;
    }

    @Override
    public void marshall(XMLStreamWriter writer) throws XMLStreamException {
        for (Node delegate : delegates) {
            delegate.marshall(writer);
        }
    }

    public boolean hasContent() {
        for (Node delegate : delegates) {
            if (delegate.hasContent()) {
                return true;
            }
        }
        return false;
    }
}
