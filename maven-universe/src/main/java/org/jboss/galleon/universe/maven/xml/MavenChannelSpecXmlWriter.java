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

package org.jboss.galleon.universe.maven.xml;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.universe.maven.MavenChannel;
import org.jboss.galleon.universe.maven.xml.MavenChannelSpecXmlParser10.Attribute;
import org.jboss.galleon.universe.maven.xml.MavenChannelSpecXmlParser10.Element;
import org.jboss.galleon.xml.BaseXmlWriter;
import org.jboss.galleon.xml.util.ElementNode;
import org.jboss.galleon.xml.util.TextNode;

/**
 *
 * @author Alexey Loubyansky
 */
public class MavenChannelSpecXmlWriter extends BaseXmlWriter<MavenChannel> {

    private static final MavenChannelSpecXmlWriter INSTANCE = new MavenChannelSpecXmlWriter();

    public static MavenChannelSpecXmlWriter getInstance() {
        return INSTANCE;
    }

    @Override
    protected ElementNode toElement(MavenChannel channel) throws XMLStreamException {
        final ElementNode producerEl = addElement(null, Element.CHANNEL);
        addAttribute(producerEl, Attribute.NAME, channel.getName());
        String value = channel.getVersionRange();
        if(value == null) {
            throw new XMLStreamException("Channel " + channel.getName() + " is missing version-range");
        }
        addElement(producerEl, Element.VERSION_RANGE).addChild(new TextNode(value));
        return producerEl;
    }

}
