/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.universe.maven.xml;

import java.io.Reader;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.universe.maven.MavenChannel;
import org.jboss.galleon.universe.maven.MavenProducerBase;
import org.jboss.galleon.xml.XmlBaseParsers;

/**
 *
 * @author Alexey Loubyansky
 */
public class MavenChannelSpecXmlParser {

    private static final MavenChannelSpecXmlParser INSTANCE = new MavenChannelSpecXmlParser();

    public static MavenChannelSpecXmlParser getInstance() {
        return INSTANCE;
    }

    private MavenChannelSpecXmlParser() {
        XmlBaseParsers.getInstance().plugin(MavenChannelSpecXmlParser10.ROOT_1_0, new MavenChannelSpecXmlParser10());
    }

    public void parse(final Reader input, final ParsedCallbackHandler<MavenProducerBase, MavenChannel> builder) throws XMLStreamException {
        XmlBaseParsers.parse(input, builder);
    }
}
