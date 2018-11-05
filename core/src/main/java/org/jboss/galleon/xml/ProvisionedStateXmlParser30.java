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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedPackage;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.util.ParsingUtils;
import org.jboss.staxmapper.XMLExtendedStreamReader;

/**
 *
 * @author Alexey Loubyansky
 */
class ProvisionedStateXmlParser30 implements PlugableXmlParser<ProvisionedState.Builder> {

    public static final String NAMESPACE_3_0 = "urn:jboss:galleon:provisioned-state:3.0";
    public static final QName ROOT_3_0 = new QName(NAMESPACE_3_0, Element.INSTALLATION.getLocalName());

    enum Element implements XmlNameProvider {

        CONFIG("config"),
        FEATURE("feature"),
        FEATURE_PACK("feature-pack"),
        INSTALLATION("installation"),
        LAYER("layer"),
        LAYERS("layers"),
        PACKAGE("package"),
        PACKAGES("packages"),
        PARAM("param"),
        PROP("prop"),
        PROPS("props"),
        SPEC("spec"),

        // default unknown element
        UNKNOWN(null);


        private static final Map<QName, Element> elements;

        static {
            elements = new HashMap<>(13);
            elements.put(new QName(NAMESPACE_3_0, CONFIG.name), CONFIG);
            elements.put(new QName(NAMESPACE_3_0, FEATURE.name), FEATURE);
            elements.put(new QName(NAMESPACE_3_0, FEATURE_PACK.name), FEATURE_PACK);
            elements.put(new QName(NAMESPACE_3_0, INSTALLATION.name), INSTALLATION);
            elements.put(new QName(NAMESPACE_3_0, LAYER.name), LAYER);
            elements.put(new QName(NAMESPACE_3_0, LAYERS.name), LAYERS);
            elements.put(new QName(NAMESPACE_3_0, PACKAGE.name), PACKAGE);
            elements.put(new QName(NAMESPACE_3_0, PACKAGES.name), PACKAGES);
            elements.put(new QName(NAMESPACE_3_0, PARAM.name), PARAM);
            elements.put(new QName(NAMESPACE_3_0, PROP.name), PROP);
            elements.put(new QName(NAMESPACE_3_0, PROPS.name), PROPS);
            elements.put(new QName(NAMESPACE_3_0, SPEC.name), SPEC);
            elements.put(null, UNKNOWN);
        }

        static Element of(QName qName) {
            QName name;
            if (qName.getNamespaceURI().equals("")) {
                name = new QName(NAMESPACE_3_0, qName.getLocalPart());
            } else {
                name = qName;
            }
            final Element element = elements.get(name);
            return element == null ? UNKNOWN : element;
        }

        private final String name;
        private final String namespace = NAMESPACE_3_0;

        Element(final String name) {
            this.name = name;
        }

        /**
         * Get the local name of this element.
         *
         * @return the local name
         */
        @Override
        public String getLocalName() {
            return name;
        }

        @Override
        public String getNamespace() {
            return namespace;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    enum Attribute implements XmlNameProvider {

        ID("id"),
        LOCATION("location"),
        MODEL("model"),
        NAME("name"),
        VALUE("value"),

        // default unknown attribute
        UNKNOWN(null);

        private static final Map<QName, Attribute> attributes;

        static {
            attributes = new HashMap<>(6);
            attributes.put(new QName(ID.name), ID);
            attributes.put(new QName(LOCATION.name), LOCATION);
            attributes.put(new QName(MODEL.name), MODEL);
            attributes.put(new QName(NAME.name), NAME);
            attributes.put(new QName(VALUE.name), VALUE);
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
        @Override
        public String getLocalName() {
            return name;
        }

        @Override
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
        return ROOT_3_0;
    }

    @Override
    public void readElement(XMLExtendedStreamReader reader, ProvisionedState.Builder builder) throws XMLStreamException {
        ParsingUtils.parseNoAttributes(reader);
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    switch (element) {
                        case FEATURE_PACK:
                            builder.addFeaturePack(readFeaturePack(reader));
                            break;
                        case CONFIG:
                            final ProvisionedConfigBuilder configBuilder = ProvisionedConfigBuilder.builder();
                            ProvisionedConfigXmlParser30.read(reader, configBuilder);
                            builder.addConfig(configBuilder.build());
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

    private ProvisionedFeaturePack readFeaturePack(XMLExtendedStreamReader reader) throws XMLStreamException {
        final ProvisionedFeaturePack.Builder fpBuilder = ProvisionedFeaturePack.builder(parseSource(reader).getFPID());
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    return fpBuilder.build();
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    switch (element) {
                        case PACKAGES:
                            readPackageList(reader, fpBuilder);
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

    private static FeaturePackLocation parseSource(XMLExtendedStreamReader reader) throws XMLStreamException {
        final int count = reader.getAttributeCount();
        FeaturePackLocation fps = null;
        for (int i = 0; i < count; i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i));
            switch (attribute) {
                case LOCATION:
                    try {
                        fps = FeaturePackLocation.fromString(reader.getAttributeValue(i));
                    } catch (IllegalArgumentException e) {
                        throw new XMLStreamException("Failed to parse feature-pack location", e);
                    }
                    break;
                default:
                    throw ParsingUtils.unexpectedContent(reader);
            }
        }
        if (fps == null) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(Attribute.LOCATION));
        }
        return fps;
    }

    private void readPackageList(XMLExtendedStreamReader reader, ProvisionedFeaturePack.Builder builder) throws XMLStreamException {
        ParsingUtils.parseNoAttributes(reader);
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    switch (element) {
                        case PACKAGE:
                            builder.addPackage(readPackage(reader));
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

    private ProvisionedPackage readPackage(XMLExtendedStreamReader reader) throws XMLStreamException {
        String name = null;
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i));
            switch (attribute) {
                case NAME:
                    name = reader.getAttributeValue(i);
                    break;
                default:
                    throw ParsingUtils.unexpectedContent(reader);
            }
        }
        if (name == null) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(Attribute.NAME));
        }

        ParsingUtils.parseNoContent(reader);
        return ProvisionedPackage.newInstance(name);
    }
}
