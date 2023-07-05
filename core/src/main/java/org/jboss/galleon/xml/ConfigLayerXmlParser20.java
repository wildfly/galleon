/*
 * Copyright 2016-2023 Red Hat, Inc. and/or its affiliates
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.spec.ConfigLayerSpec;
import org.jboss.galleon.util.ParsingUtils;
import org.jboss.staxmapper.XMLExtendedStreamReader;

/**
 *
 * @author JF Denise
 */
class ConfigLayerXmlParser20 implements PlugableXmlParser<ConfigLayerSpec.Builder> {
    public static final String NAMESPACE_2_0 = "urn:jboss:galleon:layer-spec:2.0";
    public static final QName ROOT_2_0 = new QName(NAMESPACE_2_0, ConfigLayerXml.Element.LAYER_SPEC.getLocalName());

    @Override
    public QName getRoot() {
        return ROOT_2_0;
    }

    @Override
    public void readElement(XMLExtendedStreamReader reader, ConfigLayerSpec.Builder builder) throws XMLStreamException {
        readConfigLayer(reader, builder);
    }

    public enum Element implements XmlNameProvider {

        DEPENDENCIES("dependencies"),
        LAYER("layer"),
        LAYER_SPEC("layer-spec"),
        PROPS("props"),
        PROP("prop"),
        // default unknown element
        UNKNOWN(null);

        private static final Map<String, Element> elementsByLocal;

        static {
            elementsByLocal = new HashMap<String, Element>(6);
            elementsByLocal.put(DEPENDENCIES.name, DEPENDENCIES);
            elementsByLocal.put(LAYER.name, LAYER);
            elementsByLocal.put(LAYER_SPEC.name, LAYER_SPEC);
            elementsByLocal.put(PROPS.name, PROPS);
            elementsByLocal.put(PROP.name, PROP);
            elementsByLocal.put(null, UNKNOWN);
        }

        static Element of(String localName) {
            final Element element = elementsByLocal.get(localName);
            return element == null ? UNKNOWN : element;
        }

        private final String name;
        private final String namespace = NAMESPACE_2_0;

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
    }

    protected enum Attribute implements XmlNameProvider {

        NAME("name"),
        OPTIONAL("optional"),
        VALUE("value"),
        // default unknown attribute
        UNKNOWN(null);

        private static final Map<QName, Attribute> attributes;

        static {
            attributes = new HashMap<>(3);
            attributes.put(new QName(NAME.getLocalName()), NAME);
            attributes.put(new QName(VALUE.getLocalName()), VALUE);
            attributes.put(new QName(OPTIONAL.getLocalName()), OPTIONAL);
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
    }

    private static void readConfigLayer(XMLExtendedStreamReader reader, ConfigLayerSpec.Builder builder) throws XMLStreamException {
        String name = null;
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i));
            switch (attribute) {
                case NAME:
                    name = reader.getAttributeValue(i);
                    builder.setName(name);
                    break;
                default:
                    throw ParsingUtils.unexpectedAttribute(reader, i);
            }
        }
        if (name == null) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(Attribute.NAME));
        }
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT:
                    return;
                case XMLStreamConstants.START_ELEMENT:
                    Element e = Element.elementsByLocal.get(reader.getName().getLocalPart());
                    if(e != null) {
                        switch(e) {
                            case DEPENDENCIES:
                                readDeps(reader, builder);
                                break;
                            case PROPS:
                                readProps(reader, builder);
                                break;
                            default:
                                throw ParsingUtils.unexpectedContent(reader);
                        }
                    } else if (!FeatureGroupXml.handleFeatureGroupBodyElement(reader, builder)) {
                        throw ParsingUtils.unexpectedContent(reader);
                    }
                    break;
                default:
                    throw ParsingUtils.unexpectedContent(reader);
            }
        }
        throw ParsingUtils.endOfDocument(reader.getLocation());
    }

    private static void readProps(XMLExtendedStreamReader reader, ConfigLayerSpec.Builder builder) throws XMLStreamException {
        ParsingUtils.parseNoAttributes(reader);
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName().getLocalPart());
                    switch (element) {
                        case PROP:
                            readProp(reader, builder);
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

    private static void readProp(XMLExtendedStreamReader reader, ConfigLayerSpec.Builder builder) throws XMLStreamException {
        String name = null;
        String value = null;
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i));
            switch (attribute) {
                case NAME:
                    name = reader.getAttributeValue(i);
                    break;
                case VALUE:
                    value = reader.getAttributeValue(i);
                    break;
                default:
                    throw ParsingUtils.unexpectedContent(reader);
            }
        }
        if (name == null) {
            if (value == null) {
                final Set<Attribute> attrs = new HashSet<>();
                attrs.add(Attribute.NAME);
                attrs.add(Attribute.VALUE);
                throw ParsingUtils.missingAttributes(reader.getLocation(), attrs);
            }
            throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(Attribute.NAME));
        } else if (value == null) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(Attribute.VALUE));
        }
        builder.setProperty(name, value);
        ParsingUtils.parseNoContent(reader);
    }

    private static void readDeps(XMLExtendedStreamReader reader, ConfigLayerSpec.Builder builder) throws XMLStreamException {
        ParsingUtils.parseNoAttributes(reader);
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName().getLocalPart());
                    switch (element) {
                        case LAYER:
                            readLayer(reader, builder);
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

    private static void readLayer(XMLExtendedStreamReader reader, ConfigLayerSpec.Builder builder) throws XMLStreamException {
        String name = null;
        boolean optional = false;
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i));
            switch (attribute) {
                case NAME:
                    name = reader.getAttributeValue(i);
                    break;
                case OPTIONAL:
                    optional = Boolean.parseBoolean(reader.getAttributeValue(i));
                    break;
                default:
                    throw ParsingUtils.unexpectedContent(reader);
            }
        }
        if(name == null) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(Attribute.NAME));
        }
        builder.addLayerDep(name, optional);
        ParsingUtils.parseNoContent(reader);
    }
}