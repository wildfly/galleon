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

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeaturePackDepsConfigBuilder;
import org.jboss.galleon.spec.FeaturePackSpec;
import org.jboss.galleon.spec.FeaturePackSpec.Builder;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.util.ParsingUtils;
import org.jboss.staxmapper.XMLExtendedStreamReader;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeaturePackXmlParser20 implements PlugableXmlParser<FeaturePackSpec.Builder> {

    public static final String NAMESPACE_2_0 = "urn:jboss:galleon:feature-pack:2.0";
    public static final QName ROOT_1_0 = new QName(NAMESPACE_2_0, Element.FEATURE_PACK.getLocalName());

    public enum Element implements XmlNameProvider {

        CONFIG("config"),
        DEFAULT_CONFIGS("default-configs"),
        DEFAULT_PACKAGES("default-packages"),
        DEPENDENCIES("dependencies"),
        DEPENDENCY("dependency"),
        EXCLUDE("exclude"),
        FEATURE_PACK("feature-pack"),
        INCLUDE("include"),
        NAME("name"),
        PACKAGES("packages"),
        PACKAGE("package"),
        PATCH("patch"),
        TRANSITIVE("transitive"),
        UNIVERSES("universes"),

        // default unknown element
        UNKNOWN(null);

        private static final Map<String, Element> elements;

        static {
            elements = new HashMap<>(15);
            elements.put(CONFIG.name, CONFIG);
            elements.put(DEFAULT_CONFIGS.name, DEFAULT_CONFIGS);
            elements.put(DEFAULT_PACKAGES.name, DEFAULT_PACKAGES);
            elements.put(DEPENDENCIES.name, DEPENDENCIES);
            elements.put(DEPENDENCY.name, DEPENDENCY);
            elements.put(EXCLUDE.name, EXCLUDE);
            elements.put(FEATURE_PACK.name, FEATURE_PACK);
            elements.put(INCLUDE.name, INCLUDE);
            elements.put(NAME.name, NAME);
            elements.put(PACKAGES.name, PACKAGES);
            elements.put(PACKAGE.name, PACKAGE);
            elements.put(PATCH.name, PATCH);
            elements.put(TRANSITIVE.name, TRANSITIVE);
            elements.put(UNIVERSES.name, UNIVERSES);
            elements.put(null, UNKNOWN);
        }

        static Element of(String name) {
            final Element element = elements.get(name);
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

    enum Attribute implements XmlNameProvider {

        CLASSIFIER("classifier"),
        COORDS("coords"),
        EXTENSION("extension"),
        FOR("for"),
        INHERIT("inherit"),
        LOCATION("location"),
        MODEL("model"),
        NAMED_CONFIGS_ONLY("named-configs-only"),
        NAME("name"),
        // default unknown attribute
        UNKNOWN(null);

        private static final Map<String, Attribute> attributes;

        static {
            attributes = new HashMap<>(10);
            attributes.put(CLASSIFIER.getLocalName(), CLASSIFIER);
            attributes.put(COORDS.getLocalName(), COORDS);
            attributes.put(EXTENSION.getLocalName(), EXTENSION);
            attributes.put(FOR.getLocalName(), FOR);
            attributes.put(INHERIT.getLocalName(), INHERIT);
            attributes.put(LOCATION.getLocalName(), LOCATION);
            attributes.put(MODEL.getLocalName(), MODEL);
            attributes.put(NAME.getLocalName(), NAME);
            attributes.put(NAMED_CONFIGS_ONLY.getLocalName(), NAMED_CONFIGS_ONLY);
            attributes.put(null, UNKNOWN);
        }

        static Attribute of(String name) {
            final Attribute attribute = attributes.get(name);
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

    @Override
    public QName getRoot() {
        return ROOT_1_0;
    }

    @Override
    public void readElement(XMLExtendedStreamReader reader, Builder fpBuilder) throws XMLStreamException {
        fpBuilder.setFPID(readSource(reader).getFPID());
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName().getLocalPart());
                    switch (element) {
                        case UNIVERSES:
                            ProvisioningXmlParser20.readUniverses(reader, fpBuilder);
                            break;
                        case DEPENDENCIES:
                            readFeaturePackDeps(reader, fpBuilder);
                            break;
                        case DEFAULT_CONFIGS:
                            ProvisioningXmlParser10.parseDefaultConfigs(reader, fpBuilder);
                            break;
                        case CONFIG:
                            final ConfigModel.Builder config = ConfigModel.builder();
                            ConfigXml.readConfig(reader, config);
                            try {
                                fpBuilder.addConfig(config.build());
                            } catch (ProvisioningDescriptionException e) {
                                throw new XMLStreamException("Failed to parse config element", reader.getLocation(), e);
                            }
                            break;
                        case DEFAULT_PACKAGES:
                            readDefaultPackages(reader, fpBuilder);
                            break;
                        case TRANSITIVE:
                            readTransitive(reader, fpBuilder);
                            break;
                        case PATCH:
                            parsePatchFor(reader, fpBuilder);
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

    private FeaturePackLocation readSource(XMLExtendedStreamReader reader) throws XMLStreamException {
        FeaturePackLocation location = null;
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i).getLocalPart());
            switch (attribute) {
                case LOCATION:
                    try {
                        location = FeaturePackLocation.fromString(reader.getAttributeValue(i));
                    } catch (IllegalArgumentException e) {
                        throw new XMLStreamException(ParsingUtils.error("Failed to parse feature-pack location", reader.getLocation()), e);
                    }
                    break;
                default:
                    throw ParsingUtils.unexpectedContent(reader);
            }
        }
        if (location == null) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(Attribute.LOCATION));
        }
        return location;
    }

    private static void readFeaturePackDeps(XMLExtendedStreamReader reader, FeaturePackDepsConfigBuilder<?> fpBuilder) throws XMLStreamException {
        ParsingUtils.parseNoAttributes(reader);
        boolean hasChildren = false;
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    if (!hasChildren) {
                        throw ParsingUtils.expectedAtLeastOneChild(reader, Element.DEPENDENCIES, Element.DEPENDENCY);
                    }
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName().getLocalPart());
                    switch (element) {
                        case DEPENDENCY:
                            ProvisioningXmlParser20.readFeaturePackDep(reader, fpBuilder);
                            hasChildren = true;
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

    private static void readTransitive(XMLExtendedStreamReader reader, FeaturePackDepsConfigBuilder<?> builder) throws XMLStreamException {
        ParsingUtils.parseNoAttributes(reader);
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getLocalName());
                    switch (element) {
                        case DEPENDENCY:
                            ProvisioningXmlParser20.readTransitiveFeaturePackDep(reader, builder);
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

    private void readDefaultPackages(XMLExtendedStreamReader reader, Builder fpBuilder) throws XMLStreamException {
        ParsingUtils.parseNoAttributes(reader);
        boolean hasChildren = false;
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    if (!hasChildren) {
                        throw ParsingUtils.expectedAtLeastOneChild(reader, Element.DEFAULT_PACKAGES, Element.PACKAGE);
                    }
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName().getLocalPart());
                    switch (element) {
                        case PACKAGE:
                            fpBuilder.addDefaultPackage(parseName(reader));
                            hasChildren = true;
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

    private void parsePatchFor(final XMLExtendedStreamReader reader, FeaturePackSpec.Builder builder) throws XMLStreamException {
        final int count = reader.getAttributeCount();
        FPID patchFor = null;
        for (int i = 0; i < count; i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i).getLocalPart());
            switch (attribute) {
                case FOR:
                    final FeaturePackLocation location;
                    try {
                        location = FeaturePackLocation.fromString(reader.getAttributeValue(i));
                    } catch(IllegalArgumentException e) {
                        throw new XMLStreamException("Failed to parse patch for FPID '" + reader.getAttributeValue(i) + "'", e);
                    }
                    patchFor = ProvisioningXmlParser20.resolveUniverse(builder, location).getFPID();
                    break;
                default:
                    throw ParsingUtils.unexpectedContent(reader);
            }
        }
        if (patchFor == null) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(Attribute.FOR));
        }
        ParsingUtils.parseNoContent(reader);

        if(patchFor.getBuild() == null) {
            throw new XMLStreamException("Build number is missing for the version the patch is applied to: " + patchFor);
        }

        builder.setPatchFor(patchFor);
    }

    private String parseName(final XMLExtendedStreamReader reader) throws XMLStreamException {
        final int count = reader.getAttributeCount();
        String path = null;
        for (int i = 0; i < count; i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i).getLocalPart());
            switch (attribute) {
                case NAME:
                    path = reader.getAttributeValue(i);
                    break;
                default:
                    throw ParsingUtils.unexpectedContent(reader);
            }
        }
        if (path == null) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(Attribute.NAME));
        }
        ParsingUtils.parseNoContent(reader);
        return path;
    }
}
