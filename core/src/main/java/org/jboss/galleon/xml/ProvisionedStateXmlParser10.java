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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.runtime.ResolvedSpecId;
import org.jboss.galleon.state.ProvisionedConfig;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedPackage;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;
import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.util.ParsingUtils;
import org.jboss.staxmapper.XMLExtendedStreamReader;

/**
 *
 * @author Alexey Loubyansky
 */
class ProvisionedStateXmlParser10 implements PlugableXmlParser<ProvisionedState.Builder> {

    public static final String NAMESPACE_1_0 = "urn:jboss:galleon:provisioned-state:1.0";
    public static final QName ROOT_1_0 = new QName(NAMESPACE_1_0, Element.INSTALLATION.getLocalName());

    enum Element implements XmlNameProvider {

        CONFIG("config"),
        FEATURE("feature"),
        FEATURE_PACK("feature-pack"),
        INSTALLATION("installation"),
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
            elements = new HashMap<>(11);
            elements.put(new QName(NAMESPACE_1_0, CONFIG.name), CONFIG);
            elements.put(new QName(NAMESPACE_1_0, FEATURE.name), FEATURE);
            elements.put(new QName(NAMESPACE_1_0, FEATURE_PACK.name), FEATURE_PACK);
            elements.put(new QName(NAMESPACE_1_0, INSTALLATION.name), INSTALLATION);
            elements.put(new QName(NAMESPACE_1_0, PACKAGE.name), PACKAGE);
            elements.put(new QName(NAMESPACE_1_0, PACKAGES.name), PACKAGES);
            elements.put(new QName(NAMESPACE_1_0, PARAM.name), PARAM);
            elements.put(new QName(NAMESPACE_1_0, PROP.name), PROP);
            elements.put(new QName(NAMESPACE_1_0, PROPS.name), PROPS);
            elements.put(new QName(NAMESPACE_1_0, SPEC.name), SPEC);
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

        ARTIFACT_ID("artifactId"),
        GROUP_ID("groupId"),
        ID("id"),
        MODEL("model"),
        NAME("name"),
        VALUE("value"),
        VERSION("version"),

        // default unknown attribute
        UNKNOWN(null);

        private static final Map<QName, Attribute> attributes;

        static {
            attributes = new HashMap<>(8);
            attributes.put(new QName(ARTIFACT_ID.name), ARTIFACT_ID);
            attributes.put(new QName(GROUP_ID.name), GROUP_ID);
            attributes.put(new QName(ID.name), ID);
            attributes.put(new QName(MODEL.name), MODEL);
            attributes.put(new QName(NAME.name), NAME);
            attributes.put(new QName(VALUE.name), VALUE);
            attributes.put(new QName(VERSION.name), VERSION);
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
        return ROOT_1_0;
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
                            builder.addConfig(readConfig(reader));
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
        final int count = reader.getAttributeCount();
        String groupId = null;
        String artifactId = null;
        String version = null;
        for (int i = 0; i < count; i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i));
            switch (attribute) {
                case GROUP_ID:
                    groupId = reader.getAttributeValue(i);
                    break;
                case ARTIFACT_ID:
                    artifactId = reader.getAttributeValue(i);
                    break;
                case VERSION:
                    version = reader.getAttributeValue(i);
                    break;
                default:
                    throw ParsingUtils.unexpectedContent(reader);
            }
        }
        if (groupId == null) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(Attribute.GROUP_ID));
        }
        if (artifactId == null) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(Attribute.ARTIFACT_ID));
        }
        if (version == null) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(Attribute.VERSION));
        }

        final ProvisionedFeaturePack.Builder fpBuilder = ProvisionedFeaturePack.builder(LegacyGalleon1Universe.toFpl(ArtifactCoords.newGav(groupId, artifactId, version)).getFPID());

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

    private static ProvisionedConfig readConfig(XMLExtendedStreamReader reader) throws XMLStreamException {
        final ProvisionedConfigBuilder config = ProvisionedConfigBuilder.builder();
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
                    return config.build();
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    switch (element) {
                        case PROPS:
                            readProps(reader, config);
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
        config.setProperty(name, value);
        ParsingUtils.parseNoContent(reader);
    }

    private static void readFeaturePack(XMLExtendedStreamReader reader, ProvisionedConfigBuilder config) throws XMLStreamException {
        String group = null;
        String artifact = null;
        String version = null;
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i));
            switch (attribute) {
                case GROUP_ID:
                    group = reader.getAttributeValue(i);
                    break;
                case ARTIFACT_ID:
                    artifact = reader.getAttributeValue(i);
                    break;
                case VERSION:
                    version = reader.getAttributeValue(i);
                    break;
                default:
                    throw ParsingUtils.unexpectedContent(reader);
            }
        }
        Set<Attribute> missingAttrs = null;
        if (group == null) {
            missingAttrs = new HashSet<Attribute>();
            missingAttrs.add(Attribute.GROUP_ID);
        }
        if (artifact == null) {
            if (missingAttrs == null) {
                missingAttrs = new HashSet<Attribute>();
            }
            missingAttrs.add(Attribute.ARTIFACT_ID);
        }
        if (version == null) {
            if (missingAttrs == null) {
                missingAttrs = new HashSet<Attribute>();
            }
            missingAttrs.add(Attribute.VERSION);
        }
        if (missingAttrs != null) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), missingAttrs);
        }
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    switch (element) {
                        case SPEC:
                            readSpec(reader, LegacyGalleon1Universe.newFPID(group, artifact, version).getProducer(), config);
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

    private static void readSpec(XMLExtendedStreamReader reader, ProducerSpec producer, ProvisionedConfigBuilder config) throws XMLStreamException {
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

    private static void readFeature(XMLExtendedStreamReader reader, ResolvedSpecId specId, ProvisionedConfigBuilder config) throws XMLStreamException {
        ResolvedFeatureId id = null;
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i));
            switch (attribute) {
                case ID:
                    try {
                        id = ResolvedFeatureId.fromGalleon1String(reader.getAttributeValue(i));
                    } catch (ProvisioningDescriptionException e) {
                        throw new XMLStreamException(Errors.parseXml(), e);
                    }
                    break;
                default:
                    throw ParsingUtils.unexpectedContent(reader);
            }
        }
        final ProvisionedFeatureBuilder featureBuilder = id == null ? ProvisionedFeatureBuilder.builder(specId) :  ProvisionedFeatureBuilder.builder(id);
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
        if(name == null) {
            Set<Attribute> missingAttrs = null;
            if(value == null) {
                missingAttrs = new HashSet<>(2);
                missingAttrs.add(Attribute.NAME);
                missingAttrs.add(Attribute.VALUE);
            } else {
                missingAttrs = Collections.singleton(Attribute.NAME);
            }
            throw ParsingUtils.missingAttributes(reader.getLocation(), missingAttrs);
        } if(value == null) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(Attribute.VALUE));
        }
        featureBuilder.setConfigParam(name, value);
        ParsingUtils.parseNoContent(reader);
    }
}
