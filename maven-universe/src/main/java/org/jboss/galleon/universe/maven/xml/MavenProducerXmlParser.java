/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.universe.maven.xml;

import java.io.Reader;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.xml.XmlBaseParsers;

/**
 *
 * @author Alexey Loubyansky
 */
public class MavenProducerXmlParser {

    private static final MavenProducerXmlParser INSTANCE = new MavenProducerXmlParser();

    public static MavenProducerXmlParser getInstance() {
        return INSTANCE;
    }

    private MavenProducerXmlParser() {
        XmlBaseParsers.getInstance().plugin(MavenProducerXmlParser10.ROOT, new MavenProducerXmlParser10());
    }

    public void parse(final Reader input, final MavenParsedProducerCallbackHandler builder) throws XMLStreamException {
        XmlBaseParsers.parse(input, builder);
    }
}
