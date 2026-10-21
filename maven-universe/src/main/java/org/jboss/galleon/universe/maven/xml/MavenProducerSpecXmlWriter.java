/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.universe.maven.xml;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.universe.maven.MavenArtifact;
import org.jboss.galleon.universe.maven.MavenProducerBase;
import org.jboss.galleon.universe.maven.xml.MavenProducerSpecXmlParser10.Attribute;
import org.jboss.galleon.universe.maven.xml.MavenProducerSpecXmlParser10.Element;
import org.jboss.galleon.xml.BaseXmlWriter;
import org.jboss.galleon.xml.util.ElementNode;
import org.jboss.galleon.xml.util.TextNode;

/**
 *
 * @author Alexey Loubyansky
 */
public class MavenProducerSpecXmlWriter extends BaseXmlWriter<MavenProducerBase> {

    private static final MavenProducerSpecXmlWriter INSTANCE = new MavenProducerSpecXmlWriter();

    public static MavenProducerSpecXmlWriter getInstance() {
        return INSTANCE;
    }

    @Override
    protected ElementNode toElement(MavenProducerBase producer) throws XMLStreamException {
        final ElementNode producerEl = addElement(null, Element.PRODUCER);
        addAttribute(producerEl, Attribute.NAME, producer.getName());
        final MavenArtifact artifact = producer.getArtifact();
        String value = artifact.getGroupId();
        if(value == null) {
            throw new XMLStreamException("Producer " + producer.getName() + " is missing groupId");
        }
        addElement(producerEl, Element.GROUP_ID).addChild(new TextNode(value));
        value = artifact.getArtifactId();
        if(value == null) {
            throw new XMLStreamException("Producer " + producer.getName() + " is missing artifactId");
        }
        addElement(producerEl, Element.ARTIFACT_ID).addChild(new TextNode(value));
        value = artifact.getVersionRange();
        if(value == null) {
            throw new XMLStreamException("Producer " + producer.getName() + " is missing version-range");
        }
        addElement(producerEl, Element.VERSION_RANGE).addChild(new TextNode(value));
        return producerEl;
    }

}
