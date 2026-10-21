/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.xml;

import java.io.Reader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLMapper;

/**
 *
 * @author Alexey Loubyansky
 */
public class XmlBaseParsers {

    private static final XmlBaseParsers INSTANCE = new XmlBaseParsers();

    public static XmlBaseParsers getInstance() {
        return INSTANCE;
    }

    public static void parse(final Reader reader, Object builder) throws XMLStreamException {
        INSTANCE.doParse(reader, builder);
    }
    private static final XMLInputFactory inputFactory;
    static {
        final XMLInputFactory tmpIF = XMLInputFactory.newInstance();
        setIfSupported(tmpIF, XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
        setIfSupported(tmpIF, XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        inputFactory = tmpIF;
    }

    private static void setIfSupported(final XMLInputFactory inputFactory, final String property, final Object value) {
        if (inputFactory.isPropertySupported(property)) {
            inputFactory.setProperty(property, value);
        }
    }

    public static XMLStreamReader createXMLStreamReader(Reader reader) throws XMLStreamException {
        return inputFactory.createXMLStreamReader(reader);
    }

    private final XMLMapper mapper;

    protected XmlBaseParsers() {
        mapper = XMLMapper.Factory.create();
    }

    public void plugin(QName root, XMLElementReader<?> reader) {
        mapper.registerRootElement(root, reader);
    }

    public void doParse(final Reader reader, Object builder) throws XMLStreamException {
        mapper.parseDocument(builder, inputFactory.createXMLStreamReader(reader));
    }
}
