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
import org.jboss.galleon.spec.CapabilitySpec;
import org.jboss.galleon.spec.FeatureAnnotation;
import org.jboss.galleon.spec.FeatureDependencySpec;
import org.jboss.galleon.spec.FeatureId;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureReferenceSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.util.ParsingUtils;
import org.jboss.staxmapper.XMLExtendedStreamReader;

/**
 *
 * @author Alexey Loubyansky
 */
class FeatureSpecXmlParser10 implements PlugableXmlParser<FeatureSpec.Builder> {

    public static final String NAMESPACE_1_0 = "urn:jboss:galleon:feature-spec:1.0";
    public static final QName ROOT_1_0 = new QName(NAMESPACE_1_0, Element.FEATURE_SPEC.name);

    enum Element implements XmlNameProvider {

        ANNOTATION("annotation"),
        CAPABILITY("capability"),
        DEPENDENCIES("deps"),
        DEPENDENCY("dep"),
        ELEM("elem"),
        FEATURE_PACK("feature-pack"),
        FEATURE_SPEC("feature-spec"),
        PACKAGE("package"),
        PACKAGES("packages"),
        PARAMETER("param"),
        PARAMETERS("params"),
        PROVIDES("provides"),
        REFERENCE("ref"),
        REFERENCES("refs"),
        REQUIRES("requires"),

        // default unknown element
        UNKNOWN(null);


        private static final Map<QName, Element> elements;

        static {
            elements = new HashMap<>(16);
            elements.put(new QName(NAMESPACE_1_0, ANNOTATION.name), ANNOTATION);
            elements.put(new QName(NAMESPACE_1_0, CAPABILITY.name), CAPABILITY);
            elements.put(new QName(NAMESPACE_1_0, DEPENDENCIES.name), DEPENDENCIES);
            elements.put(new QName(NAMESPACE_1_0, DEPENDENCY.name), DEPENDENCY);
            elements.put(new QName(NAMESPACE_1_0, ELEM.name), ELEM);
            elements.put(new QName(NAMESPACE_1_0, FEATURE_PACK.name), FEATURE_PACK);
            elements.put(new QName(NAMESPACE_1_0, FEATURE_SPEC.name), FEATURE_SPEC);
            elements.put(new QName(NAMESPACE_1_0, PACKAGE.name), PACKAGE);
            elements.put(new QName(NAMESPACE_1_0, PACKAGES.name), PACKAGES);
            elements.put(new QName(NAMESPACE_1_0, PARAMETER.name), PARAMETER);
            elements.put(new QName(NAMESPACE_1_0, PARAMETERS.name), PARAMETERS);
            elements.put(new QName(NAMESPACE_1_0, PROVIDES.name), PROVIDES);
            elements.put(new QName(NAMESPACE_1_0, REFERENCE.name), REFERENCE);
            elements.put(new QName(NAMESPACE_1_0, REFERENCES.name), REFERENCES);
            elements.put(new QName(NAMESPACE_1_0, REQUIRES.name), REQUIRES);
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

        DEFAULT("default"),
        DEPENDENCY("dependency"),
        FEATURE("feature"),
        FEATURE_ID("feature-id"),
        INCLUDE("include"),
        MAPS_TO("maps-to"),
        NAME("name"),
        NILLABLE("nillable"),
        OPTIONAL("optional"),
        TYPE("type"),
        VALUE("value"),

        // default unknown attribute
        UNKNOWN(null);

        private static final Map<QName, Attribute> attributes;

        static {
            attributes = new HashMap<>(12);
            attributes.put(new QName(DEFAULT.name), DEFAULT);
            attributes.put(new QName(DEPENDENCY.name), DEPENDENCY);
            attributes.put(new QName(FEATURE.name), FEATURE);
            attributes.put(new QName(FEATURE_ID.name), FEATURE_ID);
            attributes.put(new QName(INCLUDE.name), INCLUDE);
            attributes.put(new QName(MAPS_TO.name), MAPS_TO);
            attributes.put(new QName(NAME.name), NAME);
            attributes.put(new QName(NILLABLE.name), NILLABLE);
            attributes.put(new QName(OPTIONAL.name), OPTIONAL);
            attributes.put(new QName(TYPE.name), TYPE);
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
        return ROOT_1_0;
    }

    @Override
    public void readElement(XMLExtendedStreamReader reader, FeatureSpec.Builder featureBuilder) throws XMLStreamException {
        final int count = reader.getAttributeCount();
        String specName = null;
        for (int i = 0; i < count; i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i));
            switch (attribute) {
                case NAME:
                    specName = reader.getAttributeValue(i);
                    break;
                default:
                    throw ParsingUtils.unexpectedAttribute(reader, i);
            }
        }
        if (specName == null) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(Attribute.NAME));
        }
        featureBuilder.setName(specName);

        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    switch (element) {
                        case ANNOTATION:
                            parseAnnotation(reader, featureBuilder);
                            break;
                        case DEPENDENCIES:
                            parseDependencies(reader, featureBuilder);
                            break;
                        case REFERENCES:
                            parseReferences(reader, featureBuilder);
                            break;
                        case PARAMETERS:
                            parseParameters(reader, featureBuilder);
                            break;
                        case PACKAGES:
                            PackageDepsSpecXmlParser.parsePackageDeps(Element.PACKAGES, reader, featureBuilder);
                            break;
                        case PROVIDES:
                            parseCapabilities(reader, featureBuilder, true);
                            break;
                        case REQUIRES:
                            parseCapabilities(reader, featureBuilder, false);
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

    private void parseAnnotation(XMLExtendedStreamReader reader, FeatureSpec.Builder builder) throws XMLStreamException {
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
        final FeatureAnnotation fa = new FeatureAnnotation(name);
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    builder.addAnnotation(fa);
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    switch (element) {
                        case ELEM:
                            parseAnnotationElem(reader, fa);
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

    private void parseAnnotationElem(XMLExtendedStreamReader reader, FeatureAnnotation fa) throws XMLStreamException {
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
                    throw ParsingUtils.unexpectedAttribute(reader, i);
            }
        }
        if(name == null) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(Attribute.NAME));
        }
        ParsingUtils.parseNoContent(reader);
        fa.setElement(name, value);
    }

    private void parseDependencies(XMLExtendedStreamReader reader, FeatureSpec.Builder specBuilder) throws XMLStreamException {
        ParsingUtils.parseNoAttributes(reader);
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    switch (element) {
                        case DEPENDENCY:
                            try {
                                specBuilder.addFeatureDep(parseDependency(reader));
                            } catch (ProvisioningDescriptionException e) {
                                throw new XMLStreamException("Failed to parse feature reference", e);
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

    private FeatureDependencySpec parseDependency(XMLExtendedStreamReader reader) throws XMLStreamException {
        String dependency = null;
        boolean include = false;
        String featureId = null;
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i));
            switch (attribute) {
                case DEPENDENCY:
                    dependency = reader.getAttributeValue(i);
                    break;
                case FEATURE_ID:
                    featureId = reader.getAttributeValue(i);
                    break;
                case INCLUDE:
                    include = Boolean.parseBoolean(reader.getAttributeValue(i));
                    break;
                default:
                    throw ParsingUtils.unexpectedAttribute(reader, i);
            }
        }
        if(featureId == null) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(Attribute.FEATURE_ID));
        }
        ParsingUtils.parseNoContent(reader);
        try {
            return FeatureDependencySpec.create(FeatureId.fromString(featureId), dependency, include);
        } catch (ProvisioningDescriptionException e) {
            throw new XMLStreamException("Failed to parse feature dependency", reader.getLocation(), e);
        }
    }

    private void parseReferences(XMLExtendedStreamReader reader, FeatureSpec.Builder specBuilder) throws XMLStreamException {
        ParsingUtils.parseNoAttributes(reader);
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    switch (element) {
                        case REFERENCE:
                            try {
                                specBuilder.addFeatureRef(parseReference(reader));
                            } catch (ProvisioningDescriptionException e) {
                                throw new XMLStreamException("Failed to parse feature reference", e);
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

    private FeatureReferenceSpec parseReference(XMLExtendedStreamReader reader) throws XMLStreamException {
        String dependency = null;
        String name = null;
        String feature = null;
        boolean nillable = false;
        boolean include = false;
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i));
            switch (attribute) {
                case DEPENDENCY:
                    dependency = reader.getAttributeValue(i);
                    break;
                case NAME:
                    name = reader.getAttributeValue(i);
                    break;
                case FEATURE:
                    feature = reader.getAttributeValue(i);
                    break;
                case NILLABLE:
                    nillable = Boolean.parseBoolean(reader.getAttributeValue(i));
                    break;
                case INCLUDE:
                    include = Boolean.parseBoolean(reader.getAttributeValue(i));
                    break;
                default:
                    throw ParsingUtils.unexpectedAttribute(reader, i);
            }
        }
        if(feature == null) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(Attribute.FEATURE));
        }
        if(name == null) {
            name = feature;
        }
        final FeatureReferenceSpec.Builder refBuilder = FeatureReferenceSpec.builder(feature).setOrigin(dependency).setName(name).setNillable(nillable).setInclude(include);

        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    try {
                        return refBuilder.build();
                    } catch (ProvisioningDescriptionException e) {
                        throw new XMLStreamException("Failed to parse feature reference", e);
                    }
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    switch (element) {
                        case PARAMETER:
                            parseRefParameter(reader, refBuilder);
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

    private void parseRefParameter(XMLExtendedStreamReader reader, FeatureReferenceSpec.Builder refBuilder) throws XMLStreamException {
        String name = null;
        String mapsTo = null;
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i));
            switch (attribute) {
                case NAME:
                    name = reader.getAttributeValue(i);
                    break;
                case MAPS_TO:
                    mapsTo = reader.getAttributeValue(i);
                    break;
                default:
                    throw ParsingUtils.unexpectedAttribute(reader, i);
            }
        }
        if(name == null) {
            final Set<Attribute> set;
            if(mapsTo == null) {
                set = new HashSet<>();
                set.add(Attribute.NAME);
                set.add(Attribute.MAPS_TO);
            } else {
                set = Collections.singleton(Attribute.NAME);
            }
            throw ParsingUtils.missingAttributes(reader.getLocation(), set);
        } else if(mapsTo == null) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(Attribute.MAPS_TO));
        }
        refBuilder.mapParam(name, mapsTo);
        ParsingUtils.parseNoContent(reader);
    }

    private void parseParameters(XMLExtendedStreamReader reader, FeatureSpec.Builder specBuilder) throws XMLStreamException {
        ParsingUtils.parseNoAttributes(reader);
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    switch (element) {
                        case PARAMETER:
                            try {
                                specBuilder.addParam(parseParameter(reader));
                            } catch (ProvisioningDescriptionException e) {
                                throw new XMLStreamException("Failed to add parameter to the spec", reader.getLocation(), e);
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

    private FeatureParameterSpec parseParameter(XMLExtendedStreamReader reader) throws XMLStreamException {
        final FeatureParameterSpec.Builder builder = FeatureParameterSpec.builder();
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i));
            switch (attribute) {
                case NAME:
                    builder.setName(reader.getAttributeValue(i));
                    break;
                case FEATURE_ID:
                    if(Boolean.parseBoolean(reader.getAttributeValue(i))) {
                        builder.setFeatureId();
                    }
                    break;
                case DEFAULT:
                    builder.setDefaultValue(reader.getAttributeValue(i));
                    break;
                case NILLABLE:
                    if(Boolean.parseBoolean(reader.getAttributeValue(i))) {
                        builder.setNillable();
                    }
                    break;
                case TYPE:
                    builder.setType(reader.getAttributeValue(i));
                    break;
                default:
                    throw ParsingUtils.unexpectedAttribute(reader, i);
            }
        }
        if(builder.getName() == null) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(Attribute.NAME));
        }
        ParsingUtils.parseNoContent(reader);
        try {
            return builder.build();
        } catch (ProvisioningDescriptionException e) {
            throw new XMLStreamException("Failed to create feature parameter", reader.getLocation(), e);
        }
    }

    private void parseCapabilities(XMLExtendedStreamReader reader, FeatureSpec.Builder spec, boolean provides) throws XMLStreamException {
        ParsingUtils.parseNoAttributes(reader);
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    switch (element) {
                        case CAPABILITY:
                            final CapabilitySpec cap = parseCapabilityName(reader);
                                if (provides) {
                                    spec.providesCapability(cap);
                                } else {
                                    spec.requiresCapability(cap);
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

    private CapabilitySpec parseCapabilityName(XMLExtendedStreamReader reader) throws XMLStreamException {
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
                    throw ParsingUtils.unexpectedAttribute(reader, i);
            }
        }
        if (name == null) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(Attribute.NAME));
        }
        ParsingUtils.parseNoContent(reader);
        try {
            return CapabilitySpec.fromString(name, optional);
        } catch (ProvisioningDescriptionException e) {
            throw new XMLStreamException("Failed to parse capability '" + name + "'", reader.getLocation(), e);
       }
    }
}