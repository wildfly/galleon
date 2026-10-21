/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.xml;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.xml.util.AttributeValue;
import org.jboss.galleon.xml.util.ElementNode;
import org.jboss.galleon.xml.util.FormattingXmlStreamWriter;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class BaseXmlWriter<T> {

    public static void ensureParentDir(Path p) throws IOException {
        if(!Files.exists(p.getParent())) {
            Files.createDirectories(p.getParent());
        }
    }

    public static ElementNode addElement(ElementNode parent, XmlNameProvider e) {
        return addElement(parent, e.getLocalName(), e.getNamespace());
    }

    public static ElementNode addElement(ElementNode parent, String localName, String ns) {
        final ElementNode eNode = new ElementNode(parent, localName, ns);
        if(parent != null) {
            parent.addChild(eNode);
        }
        return eNode;
    }

    public static void addAttribute(ElementNode e, XmlNameProvider name, String value) {
        addAttribute(e, name.getLocalName(), value);
    }

    public static void addAttribute(ElementNode e, String name, String value) {
        e.addAttribute(name, new AttributeValue(value));
    }

    public void write(T t, Path outputFile) throws XMLStreamException, IOException {
        ensureParentDir(outputFile);
        try(Writer writer = Files.newBufferedWriter(outputFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            write(t, writer);
        }
    }

    public void write(T t, Writer stream) throws XMLStreamException, IOException {
        final ElementNode root = toElement(t);
        try (FormattingXmlStreamWriter writer = new FormattingXmlStreamWriter(XMLOutputFactory.newDefaultFactory()
                .createXMLStreamWriter(stream))) {
            writer.writeStartDocument(StandardCharsets.UTF_8.name(), null);
            root.marshall(writer);
            writer.writeEndDocument();
        }
    }

    protected abstract ElementNode toElement(T type) throws XMLStreamException;
}
