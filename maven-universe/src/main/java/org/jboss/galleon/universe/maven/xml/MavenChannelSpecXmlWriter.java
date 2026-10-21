/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.universe.maven.xml;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.universe.maven.MavenChannelDescription;
import org.jboss.galleon.universe.maven.xml.MavenChannelSpecXmlParser10.Attribute;
import org.jboss.galleon.universe.maven.xml.MavenChannelSpecXmlParser10.Element;
import org.jboss.galleon.xml.BaseXmlWriter;
import org.jboss.galleon.xml.util.ElementNode;
import org.jboss.galleon.xml.util.TextNode;

/**
 *
 * @author Alexey Loubyansky
 */
public class MavenChannelSpecXmlWriter extends BaseXmlWriter<MavenChannelDescription> {

    private static final MavenChannelSpecXmlWriter INSTANCE = new MavenChannelSpecXmlWriter();

    public static MavenChannelSpecXmlWriter getInstance() {
        return INSTANCE;
    }

    @Override
    protected ElementNode toElement(MavenChannelDescription channel) throws XMLStreamException {
        final ElementNode producerEl = addElement(null, Element.CHANNEL);
        addAttribute(producerEl, Attribute.NAME, channel.getName());
        String value = channel.getVersionRange();
        if(value == null) {
            throw new XMLStreamException("Channel " + channel.getName() + " is missing version-range");
        }
        addElement(producerEl, Element.VERSION_RANGE).addChild(new TextNode(value));
        String includeRegex = channel.getVersionIncludeRegex();
        if (includeRegex != null) {
            addElement(producerEl, Element.VERSION_INCLUDE_REGEX).addChild(new TextNode(includeRegex));
        }
        String excludeRegex = channel.getVersionExcludeRegex();
        if (excludeRegex != null) {
            addElement(producerEl, Element.VERSION_EXCLUDE_REGEX).addChild(new TextNode(excludeRegex));
        }
        return producerEl;
    }

}
