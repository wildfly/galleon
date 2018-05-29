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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.universe.maven.MavenArtifact;
import org.jboss.galleon.universe.maven.MavenProducer;
import org.jboss.galleon.universe.maven.MavenUniverseBase;
import org.jboss.galleon.universe.maven.MavenUniverseException;
import org.jboss.galleon.universe.maven.repo.MavenRepoManager;
import org.jboss.galleon.util.ParsingUtils;
import org.jboss.galleon.xml.PlugableXmlParser;
import org.jboss.galleon.xml.XmlNameProvider;
import org.jboss.staxmapper.XMLExtendedStreamReader;


/**
 *
 * @author Alexey Loubyansky
 */
public class MavenProducerSpecXmlParser10 implements PlugableXmlParser<ParsedCallbackHandler<MavenUniverseBase, MavenProducer>> {

    public static final String NAMESPACE_1_0 = "urn:jboss:galleon:maven:producer:spec:1.0";
    public static final QName ROOT_1_0 = new QName(NAMESPACE_1_0, Element.PRODUCER.name);

    enum Element implements XmlNameProvider {

        ARTIFACT_ID("artifactId"),
        GROUP_ID("groupId"),
        PRODUCER("producer"),
        VERSION_RANGE("version-range"),

        // default unknown element
        UNKNOWN(null);

        private static final Map<QName, Element> elements;

        static {
            elements = new HashMap<>(5);
            elements.put(new QName(NAMESPACE_1_0, ARTIFACT_ID.name), ARTIFACT_ID);
            elements.put(new QName(NAMESPACE_1_0, GROUP_ID.name), GROUP_ID);
            elements.put(new QName(NAMESPACE_1_0, PRODUCER.name), PRODUCER);
            elements.put(new QName(NAMESPACE_1_0, VERSION_RANGE.name), VERSION_RANGE);
            elements.put(null, UNKNOWN);
        }

        static Element of(QName qName) {
            QName name;
            if (qName.getNamespaceURI().equals("")) {
                name = new QName(NAMESPACE_1_0, qName.getLocalPart());
            } else {
                name = qName;
            }
            final Element element = elements.get(name);
            return element == null ? UNKNOWN : element;
        }

        private final String name;
        private final String namespace = NAMESPACE_1_0;

        Element(final String name) {
            this.name = name;
        }

        /**
         * Get the local name of this element.
         *
         * @return the local name
         */
        public String getLocalName() {
            return name;
        }

        public String getNamespace() {
            return namespace;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    enum Attribute implements XmlNameProvider {

        NAME("name"),

        // default unknown attribute
        UNKNOWN(null);

        private static final Map<QName, Attribute> attributes;

        static {
            attributes = new HashMap<>(1);
            attributes.put(new QName(NAME.name), NAME);
            attributes.put(null, UNKNOWN);
        }

        static Attribute of(QName qName) {
            final Attribute attribute = attributes.get(qName);
            return attribute == null ? UNKNOWN : attribute;
        }

        private final String name;

        Attribute(final String name) {
            this.name = name;
        }

        /**
         * Get the local name of this element.
         *
         * @return the local name
         */
        public String getLocalName() {
            return name;
        }

        public String getNamespace() {
            return null;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @Override
    public QName getRoot() {
        return ROOT_1_0;
    }

    @Override
    public void readElement(XMLExtendedStreamReader reader, ParsedCallbackHandler<MavenUniverseBase, MavenProducer> builder) throws XMLStreamException {
        String name = null;
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i));
            switch (attribute) {
                case NAME:
                    name = reader.getAttributeValue(i);
                    break;
                default:
                    throw ParsingUtils.unexpectedAttribute(reader, i);
            }
        }
        if(name == null) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(Attribute.NAME));
        }
        final MavenArtifact artifact = new MavenArtifact();
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    try {
                        final MavenRepoManager repo = builder.getParent().getRepo();
                        repo.resolveLatestVersion(artifact);
                        builder.parsed(new MavenProducer(name, repo, artifact));
                    } catch (MavenUniverseException e) {
                        throw new XMLStreamException(getParserMessage("Failed to instantiate producer " + name, reader.getLocation()), e);
                    }
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    switch (element) {
                        case GROUP_ID:
                            artifact.setGroupId(reader.getElementText());
                            break;
                        case ARTIFACT_ID:
                            artifact.setArtifactId(reader.getElementText());
                            break;
                        case VERSION_RANGE:
                            artifact.setVersionRange(reader.getElementText());
                            break;
                        default:
                            throw ParsingUtils.unexpectedContent(reader);
                    }
                    break;
                }
                default: {
                    throw ParsingUtils.unexpectedContent(reader);
                }
            }
        }
        throw ParsingUtils.endOfDocument(reader.getLocation());
    }

    private static String getParserMessage(String msg, Location location) {
        return "ParseError at [row,col]:["+location.getLineNumber()+","+
                location.getColumnNumber()+"]\n"+
                "Message: "+msg;
    }
}
