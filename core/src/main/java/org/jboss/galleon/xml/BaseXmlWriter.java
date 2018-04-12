/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.galleon.xml;

import java.io.IOException;
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
abstract class BaseXmlWriter<T> {

    protected static void ensureParentDir(Path p) throws IOException {
        if(!Files.exists(p.getParent())) {
            Files.createDirectories(p.getParent());
        }
    }

    protected static ElementNode addElement(ElementNode parent, XmlNameProvider e) {
        return addElement(parent, e.getLocalName(), e.getNamespace());
    }

    protected static ElementNode addElement(ElementNode parent, String localName, String ns) {
        final ElementNode eNode = new ElementNode(parent, localName, ns);
        if(parent != null) {
            parent.addChild(eNode);
        }
        return eNode;
    }

    protected static void addAttribute(ElementNode e, XmlNameProvider name, String value) {
        addAttribute(e, name.getLocalName(), value);
    }

    protected static void addAttribute(ElementNode e, String name, String value) {
        e.addAttribute(name, new AttributeValue(value));
    }

    public void write(T t, Path outputFile) throws XMLStreamException, IOException {
        final ElementNode root = toElement(t);
        ensureParentDir(outputFile);
        try (FormattingXmlStreamWriter writer = new FormattingXmlStreamWriter(XMLOutputFactory.newInstance()
                .createXMLStreamWriter(Files.newBufferedWriter(outputFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)))) {
            writer.writeStartDocument();
            root.marshall(writer);
            writer.writeEndDocument();
        }
    }

    protected abstract ElementNode toElement(T type) throws XMLStreamException;
}
