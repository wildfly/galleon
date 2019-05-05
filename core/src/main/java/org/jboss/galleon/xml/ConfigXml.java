/*
 * Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
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

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.util.ParsingUtils;
import org.jboss.staxmapper.XMLExtendedStreamReader;

/**
 *
 * @author Alexey Loubyansky
 */
public class ConfigXml {

    private static final ConfigXml INSTANCE = new ConfigXml();

    public static ConfigXml getInstance() {
        return INSTANCE;
    }

    public static final String NAMESPACE_1_0 = "urn:jboss:galleon:config:1.0";

    public enum Element implements XmlNameProvider {

        CONFIG("config", false),
        CONFIG_DEP("config-dep", false),
        CONFIG_DEPS("config-deps", true),
        EXCLUDE("exclude", false),
        INCLUDE("include", false),
        LAYERS("layers", true),
        PROP("prop", false),
        PROPS("props", true),

        // default unknown element
        UNKNOWN(null, false);

        private static final Map<String, Element> elementsByLocal;

        static {
            elementsByLocal = new HashMap<String, Element>(8);
            elementsByLocal.put(CONFIG.name, CONFIG);
            elementsByLocal.put(CONFIG_DEP.name, CONFIG_DEP);
            elementsByLocal.put(CONFIG_DEPS.name, CONFIG_DEPS);
            elementsByLocal.put(EXCLUDE.name, EXCLUDE);
            elementsByLocal.put(INCLUDE.name, INCLUDE);
            elementsByLocal.put(LAYERS.name, LAYERS);
            elementsByLocal.put(PROP.name, PROP);
            elementsByLocal.put(PROPS.name, PROPS);
            elementsByLocal.put(null, UNKNOWN);
        }

        static Element of(String localName) {
            final Element element = elementsByLocal.get(localName);
            return element == null ? UNKNOWN : element;
        }

        private final String name;
        private final String namespace = NAMESPACE_1_0;
        private final boolean firstChild;

        Element(final String name, boolean firstChild) {
            this.name = name;
            this.firstChild = firstChild;
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

        ID("id"),
        INHERIT("inherit"),
        INHERIT_FEATURES("inherit-features"),
        NAME("name"),
        MODEL("model"),
        VALUE("value"),

        // default unknown attribute
        UNKNOWN(null);

        private static final Map<QName, Attribute> attributes;

        static {
            attributes = new HashMap<>(7);
            attributes.put(new QName(ID.name), ID);
            attributes.put(new QName(INHERIT.getLocalName()), INHERIT);
            attributes.put(new QName(INHERIT_FEATURES.getLocalName()), INHERIT_FEATURES);
            attributes.put(new QName(NAME.getLocalName()), NAME);
            attributes.put(new QName(MODEL.getLocalName()), MODEL);
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
    }

    public ConfigXml() {
        super();
    }

    public static void readConfig(XMLExtendedStreamReader reader, ConfigModel.Builder configBuilder) throws XMLStreamException {
        String name = null;
        Boolean inheritFeatures = null;
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i));
            switch (attribute) {
                case NAME:
                    name = reader.getAttributeValue(i);
                    configBuilder.setName(name);
                    break;
                case MODEL:
                    configBuilder.setModel(reader.getAttributeValue(i));
                    break;
                case INHERIT_FEATURES:
                    inheritFeatures = Boolean.parseBoolean(reader.getAttributeValue(i));
                    configBuilder.setInheritFeatures(inheritFeatures);
                    break;
                default:
                    throw ParsingUtils.unexpectedAttribute(reader, i);
            }
        }
        if (name == null && inheritFeatures != null) {
            throw new XMLStreamException(Attribute.INHERIT_FEATURES + " attribute can't be used w/o attribute " + Attribute.NAME);
        }
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT:
                    return;
                case XMLStreamConstants.START_ELEMENT:
                    Element e = Element.elementsByLocal.get(reader.getName().getLocalPart());
                    if(e != null && e.firstChild) {
                        switch(e) {
                            case CONFIG_DEPS:
                                readConfigDeps(reader, configBuilder);
                                break;
                            case PROPS:
                                readProps(reader, configBuilder);
                                break;
                            case LAYERS:
                                readLayers(reader, configBuilder);
                                break;
                            default:
                                throw ParsingUtils.unexpectedContent(reader);
                        }
                    } else if (!FeatureGroupXml.handleFeatureGroupBodyElement(reader, configBuilder)) {
                        throw ParsingUtils.unexpectedContent(reader);
                    }
                    break;
                default:
                    throw ParsingUtils.unexpectedContent(reader);
            }
        }
        throw ParsingUtils.endOfDocument(reader.getLocation());
    }

    private static void readProps(XMLExtendedStreamReader reader, ConfigModel.Builder builder) throws XMLStreamException {
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

    private static void readProp(XMLExtendedStreamReader reader, ConfigModel.Builder builder) throws XMLStreamException {
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
        if(name == null) {
            if(value == null) {
                final Set<Attribute> attrs = new HashSet<>();
                attrs.add(Attribute.NAME);
                attrs.add(Attribute.VALUE);
                throw ParsingUtils.missingAttributes(reader.getLocation(), attrs);
            }
            throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(Attribute.NAME));
        } else if(value == null) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(Attribute.VALUE));
        }
        builder.setProperty(name, value);
        ParsingUtils.parseNoContent(reader);
    }

    private static void readConfigDeps(XMLExtendedStreamReader reader, ConfigModel.Builder builder) throws XMLStreamException {
        ParsingUtils.parseNoAttributes(reader);
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName().getLocalPart());
                    switch (element) {
                        case CONFIG_DEP:
                            readConfigDep(reader, builder);
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

    private static void readConfigDep(XMLExtendedStreamReader reader, ConfigModel.Builder builder) throws XMLStreamException {
        String id = null;
        String name = null;
        String model = null;
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i));
            switch (attribute) {
                case ID:
                    id = reader.getAttributeValue(i);
                    break;
                case NAME:
                    name = reader.getAttributeValue(i);
                    break;
                case MODEL:
                    model = reader.getAttributeValue(i);
                    break;
                default:
                    throw ParsingUtils.unexpectedContent(reader);
            }
        }
        if(id == null || name == null && model == null) {
            if(id == null) {
                throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(Attribute.ID));
            }
            throw ParsingUtils.missingOneOfAttributes(reader.getLocation(), Attribute.NAME, Attribute.MODEL);
        }
        builder.setConfigDep(id, new ConfigId(model, name));
        ParsingUtils.parseNoContent(reader);
    }

    private static void readLayers(XMLExtendedStreamReader reader, ConfigModel.Builder builder) throws XMLStreamException {
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i));
            switch (attribute) {
                case INHERIT:
                    builder.setInheritLayers(Boolean.parseBoolean(reader.getAttributeValue(i)));
                    break;
                default:
                    throw ParsingUtils.unexpectedContent(reader);
            }
        }
        try {
            while (reader.hasNext()) {
                switch (reader.nextTag()) {
                    case XMLStreamConstants.END_ELEMENT: {
                        return;
                    }
                    case XMLStreamConstants.START_ELEMENT: {
                        final Element element = Element.of(reader.getName().getLocalPart());
                        switch (element) {
                            case INCLUDE:
                                builder.includeLayer(readLayer(reader, builder));
                                break;
                            case EXCLUDE:
                                builder.excludeLayer(readLayer(reader, builder));
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
        } catch (ProvisioningDescriptionException e) {
            throw new XMLStreamException("Failed to parse layers configuration: " + e.getMessage(), reader.getLocation());
        }
        throw ParsingUtils.endOfDocument(reader.getLocation());
    }

    private static String readLayer(XMLExtendedStreamReader reader, ConfigModel.Builder builder) throws XMLStreamException {
        String name = null;
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i));
            switch (attribute) {
                case NAME:
                    name = reader.getAttributeValue(i);
                    break;
                default:
                    throw ParsingUtils.unexpectedContent(reader);
            }
        }
        if(name == null) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(Attribute.NAME));
        }
        ParsingUtils.parseNoContent(reader);
        return name;
    }
}