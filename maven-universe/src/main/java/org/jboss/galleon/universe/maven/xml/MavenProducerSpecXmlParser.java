/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.universe.maven.xml;

import java.io.Reader;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.universe.maven.MavenProducer;
import org.jboss.galleon.universe.maven.MavenUniverse;
import org.jboss.galleon.xml.XmlBaseParsers;

/**
 *
 * @author Alexey Loubyansky
 */
public class MavenProducerSpecXmlParser {

    private static final MavenProducerSpecXmlParser INSTANCE = new MavenProducerSpecXmlParser();

    public static MavenProducerSpecXmlParser getInstance() {
        return INSTANCE;
    }

    private MavenProducerSpecXmlParser() {
        XmlBaseParsers.getInstance().plugin(MavenProducerSpecXmlParser10.ROOT, new MavenProducerSpecXmlParser10());
    }

    public void parse(final Reader input, final ParsedCallbackHandler<MavenUniverse, MavenProducer> builder) throws XMLStreamException {
        XmlBaseParsers.parse(input, builder);
    }
}
