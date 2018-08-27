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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeaturePackDepsConfigBuilder;
import org.jboss.galleon.spec.FeaturePackSpec;
import org.jboss.galleon.spec.FeaturePackSpec.Builder;
import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.util.ParsingUtils;
import org.jboss.staxmapper.XMLExtendedStreamReader;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeaturePackXmlParser10 implements PlugableXmlParser<FeaturePackSpec.Builder> {

    public static final String NAMESPACE_1_0 = "urn:jboss:galleon:feature-pack:1.0";
    public static final QName ROOT_1_0 = new QName(NAMESPACE_1_0, Element.FEATURE_PACK.getLocalName());

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

        // default unknown element
        UNKNOWN(null);

        private static final Map<String, Element> elements;

        static {
            elements = new HashMap<>(12);
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
            elements.put(null, UNKNOWN);
        }

        static Element of(String name) {
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
    }

    enum Attribute implements XmlNameProvider {

        ARTIFACT_ID("artifactId"),
        GROUP_ID("groupId"),
        CLASSIFIER("classifier"),
        COORDS("coords"),
        EXTENSION("extension"),
        INHERIT("inherit"),
        MODEL("model"),
        NAMED_CONFIGS_ONLY("named-configs-only"),
        VERSION("version"),
        NAME("name"),
        // default unknown attribute
        UNKNOWN(null);

        private static final Map<String, Attribute> attributes;

        static {
            attributes = new HashMap<>(11);
            attributes.put(ARTIFACT_ID.getLocalName(), ARTIFACT_ID);
            attributes.put(GROUP_ID.getLocalName(), GROUP_ID);
            attributes.put(CLASSIFIER.getLocalName(), CLASSIFIER);
            attributes.put(COORDS.getLocalName(), COORDS);
            attributes.put(EXTENSION.getLocalName(), EXTENSION);
            attributes.put(INHERIT.getLocalName(), INHERIT);
            attributes.put(MODEL.getLocalName(), MODEL);
            attributes.put(NAME.getLocalName(), NAME);
            attributes.put(NAMED_CONFIGS_ONLY.getLocalName(), NAMED_CONFIGS_ONLY);
            attributes.put(VERSION.getLocalName(), VERSION);
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
        String groupId = null;
        String artifactId = null;
        String version = null;
        final int count = reader.getAttributeCount();
        final Set<Attribute> required = EnumSet.of(Attribute.GROUP_ID, Attribute.ARTIFACT_ID);
        for (int i = 0; i < count; i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i).getLocalPart());
            required.remove(attribute);
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
        if (!required.isEmpty()) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), required);
        }
        fpBuilder.setFPID(LegacyGalleon1Universe.toFpl(groupId, artifactId, version).getFPID());
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName().getLocalPart());
                    switch (element) {
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
                            ProvisioningXmlParser10.readFeaturePackDep(reader, fpBuilder);
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

    private String parseName(final XMLExtendedStreamReader reader) throws XMLStreamException {
        final int count = reader.getAttributeCount();
        String path = null;
        boolean parsedTarget = false;
        for (int i = 0; i < count; i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i).getLocalPart());
            switch (attribute) {
                case NAME:
                    path = reader.getAttributeValue(i);
                    parsedTarget = true;
                    break;
                default:
                    throw ParsingUtils.unexpectedContent(reader);
            }
        }
        if (!parsedTarget) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(Attribute.NAME));
        }
        ParsingUtils.parseNoContent(reader);
        return path;
    }
}
