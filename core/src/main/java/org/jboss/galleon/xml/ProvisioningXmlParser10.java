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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.config.ConfigCustomizationsBuilder;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.FeaturePackDepsConfigBuilder;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.util.ParsingUtils;
import org.jboss.staxmapper.XMLExtendedStreamReader;

/**
 *
 * @author Alexey Loubyansky
 */
public class ProvisioningXmlParser10 implements PlugableXmlParser<ProvisioningConfig.Builder> {

    public static final String NAMESPACE_1_0 = "urn:jboss:galleon:provisioning:1.0";
    public static final QName ROOT_1_0 = new QName(NAMESPACE_1_0, Element.INSTALLATION.getLocalName());

    enum Element implements XmlNameProvider {

        CONFIG("config"),
        DEFAULT_CONFIGS("default-configs"),
        EXCLUDE("exclude"),
        FEATURE_PACK("feature-pack"),
        INCLUDE("include"),
        INSTALLATION("installation"),
        ORIGIN("origin"),
        PACKAGES("packages"),

        // default unknown element
        UNKNOWN(null);


        private static final Map<String, Element> elementsByLocal;

        static {
            elementsByLocal = new HashMap<>(9);
            elementsByLocal.put(CONFIG.name, CONFIG);
            elementsByLocal.put(DEFAULT_CONFIGS.name, DEFAULT_CONFIGS);
            elementsByLocal.put(EXCLUDE.name, EXCLUDE);
            elementsByLocal.put(FEATURE_PACK.name, FEATURE_PACK);
            elementsByLocal.put(INCLUDE.name, INCLUDE);
            elementsByLocal.put(INSTALLATION.name, INSTALLATION);
            elementsByLocal.put(ORIGIN.name, ORIGIN);
            elementsByLocal.put(PACKAGES.name, PACKAGES);
            elementsByLocal.put(null, UNKNOWN);
        }

        static Element of(String localName) {
            final Element element = elementsByLocal.get(localName);
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
        INHERIT("inherit"),
        INHERIT_UNNAMED_MODELS("inherit-unnamed-models"),
        MODEL("model"),
        NAME("name"),
        NAMED_MODELS_ONLY("named-models-only"),
        VERSION("version"),

        // default unknown attribute
        UNKNOWN(null);

        private static final Map<String, Attribute> attributes;

        static {
            attributes = new HashMap<>(9);
            attributes.put(ARTIFACT_ID.name, ARTIFACT_ID);
            attributes.put(GROUP_ID.name, GROUP_ID);
            attributes.put(INHERIT.name, INHERIT);
            attributes.put(INHERIT_UNNAMED_MODELS.name, INHERIT_UNNAMED_MODELS);
            attributes.put(MODEL.name, MODEL);
            attributes.put(NAME.name, NAME);
            attributes.put(NAMED_MODELS_ONLY.name, NAMED_MODELS_ONLY);
            attributes.put(VERSION.name, VERSION);
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
    public void readElement(XMLExtendedStreamReader reader, ProvisioningConfig.Builder builder) throws XMLStreamException {
        ParsingUtils.parseNoAttributes(reader);
        boolean hasFp = false;
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    if (!hasFp) {
                        throw ParsingUtils.expectedAtLeastOneChild(reader, Element.INSTALLATION, Element.FEATURE_PACK);
                    }
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getLocalName());
                    switch (element) {
                        case FEATURE_PACK:
                            hasFp = true;
                            readFeaturePackDep(reader, builder);
                            break;
                        case DEFAULT_CONFIGS:
                            ProvisioningXmlParser10.parseDefaultConfigs(reader, builder);
                            break;
                        case CONFIG:
                            final ConfigModel.Builder config = ConfigModel.builder();
                            ConfigXml.readConfig(reader, config);
                            try {
                                builder.addConfig(config.build());
                            } catch (ProvisioningDescriptionException e) {
                                throw new XMLStreamException("Failed to parse config element", reader.getLocation(), e);
                            }
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

    static void readFeaturePackDep(XMLExtendedStreamReader reader, FeaturePackDepsConfigBuilder<?> fpBuilder) throws XMLStreamException {
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
        String origin = null;
        final FeaturePackConfig.Builder depBuilder = FeaturePackConfig.builder(LegacyGalleon1Universe.toFpl(groupId, artifactId, version));
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    try {
                        fpBuilder.addFeaturePackDep(origin, depBuilder.build());
                    } catch (ProvisioningDescriptionException e) {
                        throw new XMLStreamException("Failed to add feature-pack configuration dependency", e);
                    }
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName().getLocalPart());
                    switch (element) {
                        case PACKAGES:
                            try {
                                FeaturePackPackagesConfigParser10.readPackages(reader, depBuilder);
                            } catch (ProvisioningDescriptionException e) {
                                throw new XMLStreamException("Failed to parse " + Element.PACKAGES.getLocalName() + ": " + e.getLocalizedMessage(), reader.getLocation(), e);
                            }
                            break;
                        case ORIGIN:
                            origin = reader.getElementText();
                            break;
                        case DEFAULT_CONFIGS:
                            ProvisioningXmlParser10.parseDefaultConfigs(reader, depBuilder);
                            break;
                        case CONFIG:
                            final ConfigModel.Builder config = ConfigModel.builder();
                            ConfigXml.readConfig(reader, config);
                            try {
                                depBuilder.addConfig(config.build());
                            } catch (ProvisioningDescriptionException e) {
                                throw new XMLStreamException("Failed to parse config element", reader.getLocation(), e);
                            }
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
    }

    public static void parseDefaultConfigs(XMLExtendedStreamReader reader, ConfigCustomizationsBuilder<?> fpBuilder) throws XMLStreamException {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i).getLocalPart());
            switch (attribute) {
                case INHERIT:
                    fpBuilder.setInheritConfigs(Boolean.parseBoolean(reader.getAttributeValue(i)));
                    break;
                case INHERIT_UNNAMED_MODELS:
                    fpBuilder.setInheritModelOnlyConfigs(Boolean.parseBoolean(reader.getAttributeValue(i)));
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
                    final Element element = Element.of(reader.getLocalName());
                    switch (element) {
                        case INCLUDE:
                            parseConfigModelRef(reader, fpBuilder, true);
                            break;
                        case EXCLUDE:
                            parseConfigModelRef(reader, fpBuilder, false);
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

    private static void parseConfigModelRef(XMLExtendedStreamReader reader, ConfigCustomizationsBuilder<?> fpBuilder, boolean include) throws XMLStreamException {
        String name = null;
        String model = null;
        Boolean namedConfigsOnly = null;
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i).getLocalPart());
            switch (attribute) {
                case NAME:
                    name = reader.getAttributeValue(i);
                    break;
                case MODEL:
                    model = reader.getAttributeValue(i);
                    break;
                case NAMED_MODELS_ONLY:
                    namedConfigsOnly = Boolean.parseBoolean(reader.getAttributeValue(i));
                    break;
                default:
                    throw ParsingUtils.unexpectedContent(reader);
            }
        }

        try {
            if (include) {
                if (name == null) {
                    fpBuilder.includeConfigModel(model);
                } else {
                    fpBuilder.includeDefaultConfig(new ConfigId(model, name));
                }
            } else if (name == null) {
                if(namedConfigsOnly != null) {
                    fpBuilder.excludeConfigModel(model, namedConfigsOnly);
                } else {
                    fpBuilder.excludeConfigModel(model);
                }
            } else {
                fpBuilder.excludeDefaultConfig(model, name);
            }
        } catch(ProvisioningDescriptionException e) {
            throw new XMLStreamException(e);
        }
        ParsingUtils.parseNoContent(reader);
    }
}
