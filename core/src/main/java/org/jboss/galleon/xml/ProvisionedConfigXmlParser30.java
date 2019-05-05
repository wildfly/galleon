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

import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.runtime.ResolvedSpecId;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;
import org.jboss.galleon.util.ParsingUtils;
import org.jboss.staxmapper.XMLExtendedStreamReader;

/**
 *
 * @author Alexey Loubyansky
 */
public class ProvisionedConfigXmlParser30 implements PlugableXmlParser<ProvisionedConfigBuilder> {

    public static final String NAMESPACE_3_0 = "urn:jboss:galleon:provisioned-config:3.0";
    public static final QName ROOT_3_0 = new QName(NAMESPACE_3_0, Element.CONFIG.getLocalName());

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

        private static final Map<String, Element> elements;

        static {
            elements = new HashMap<>(13);
            elements.put(CONFIG.name, CONFIG);
            elements.put(FEATURE.name, FEATURE);
            elements.put(FEATURE_PACK.name, FEATURE_PACK);
            elements.put(LAYER.name, LAYER);
            elements.put(LAYERS.name, LAYERS);
            elements.put(PARAM.name, PARAM);
            elements.put(PROP.name, PROP);
            elements.put(PROPS.name, PROPS);
            elements.put(SPEC.name, SPEC);
            elements.put(null, UNKNOWN);
        }

        static Element of(QName qName) {
            final Element element = elements.get(qName.getLocalPart());
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
    public void readElement(XMLExtendedStreamReader reader, ProvisionedConfigBuilder config) throws XMLStreamException {
        read(reader, config);
    }

    public static void read(XMLExtendedStreamReader reader, ProvisionedConfigBuilder config) throws XMLStreamException {
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i));
            switch (attribute) {
                case NAME:
                    config.setName(reader.getAttributeValue(i));
                    break;
                case MODEL:
                    config.setModel(reader.getAttributeValue(i));
                    break;
                default:
                    throw ParsingUtils.unexpectedContent(reader);
            }
        }
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    switch (element) {
                        case PROPS:
                            readProps(reader, config);
                            break;
                        case LAYERS:
                            readLayers(reader, config);
                            break;
                        case FEATURE_PACK:
                            readFeaturePack(reader, config);
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

    private static void readProps(XMLExtendedStreamReader reader, ProvisionedConfigBuilder config) throws XMLStreamException {
        ParsingUtils.parseNoAttributes(reader);
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    switch (element) {
                        case PROP:
                            readProp(reader, config);
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

    private static void readProp(XMLExtendedStreamReader reader, ProvisionedConfigBuilder config) throws XMLStreamException {
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
        config.setProperty(name, value);
        ParsingUtils.parseNoContent(reader);
    }

    private static void readLayers(XMLExtendedStreamReader reader, ProvisionedConfigBuilder config) throws XMLStreamException {
        ParsingUtils.parseNoAttributes(reader);
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    switch (element) {
                        case LAYER:
                            readLayer(reader, config);
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

    private static void readLayer(XMLExtendedStreamReader reader, ProvisionedConfigBuilder config) throws XMLStreamException {
        String name = null;
        String model = null;
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i));
            switch (attribute) {
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
        if (name == null) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(Attribute.NAME));
        }
        config.addLayer(model, name);
        ParsingUtils.parseNoContent(reader);
    }

    private static void readFeaturePack(XMLExtendedStreamReader reader, ProvisionedConfigBuilder config)
            throws XMLStreamException {
        final FeaturePackLocation location = parseSource(reader);
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    switch (element) {
                        case SPEC:
                            readSpec(reader, location.getProducer(), config);
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

    private static void readSpec(XMLExtendedStreamReader reader, ProducerSpec producer, ProvisionedConfigBuilder config)
            throws XMLStreamException {
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
        if (name == null) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(Attribute.NAME));
        }
        final ResolvedSpecId specId = new ResolvedSpecId(producer, name);
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    switch (element) {
                        case FEATURE:
                            readFeature(reader, specId, config);
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

    private static void readFeature(XMLExtendedStreamReader reader, ResolvedSpecId specId, ProvisionedConfigBuilder config)
            throws XMLStreamException {
        ResolvedFeatureId id = null;
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i));
            switch (attribute) {
                case ID:
                    try {
                        id = ResolvedFeatureId.fromString(reader.getAttributeValue(i));
                    } catch (ProvisioningDescriptionException e) {
                        throw new XMLStreamException(Errors.parseXml(), e);
                    }
                    break;
                default:
                    throw ParsingUtils.unexpectedContent(reader);
            }
        }
        final ProvisionedFeatureBuilder featureBuilder = id == null ? ProvisionedFeatureBuilder.builder(specId)
                : ProvisionedFeatureBuilder.builder(id);
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    try {
                        config.addFeature(featureBuilder.build());
                    } catch (ProvisioningDescriptionException e) {
                        throw new XMLStreamException("Failed to instantiate provisioned feature", reader.getLocation(), e);
                    }
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    switch (element) {
                        case PARAM:
                            readParam(reader, featureBuilder);
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

    private static void readParam(XMLExtendedStreamReader reader, final ProvisionedFeatureBuilder featureBuilder)
            throws XMLStreamException {
        String name = null;
        String value = null;
        for (int i = 0; i < reader.getAttributeCount(); i++) {
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
            Set<Attribute> missingAttrs = null;
            if (value == null) {
                missingAttrs = new HashSet<>(2);
                missingAttrs.add(Attribute.NAME);
                missingAttrs.add(Attribute.VALUE);
            } else {
                missingAttrs = Collections.singleton(Attribute.NAME);
            }
            throw ParsingUtils.missingAttributes(reader.getLocation(), missingAttrs);
        }
        if (value == null) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(Attribute.VALUE));
        }
        featureBuilder.setConfigParam(name, value);
        ParsingUtils.parseNoContent(reader);
    }
}
