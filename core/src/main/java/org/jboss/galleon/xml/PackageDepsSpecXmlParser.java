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

import org.jboss.galleon.spec.PackageDependencySpec;
import org.jboss.galleon.spec.PackageDepsSpecBuilder;
import org.jboss.galleon.util.ParsingUtils;
import org.jboss.staxmapper.XMLExtendedStreamReader;

/**
 *
 * @author Alexey Loubyansky
 */
public class PackageDepsSpecXmlParser {

    public static final String ORIGIN = "origin";
    public static final String PACKAGE = "package";

    private static final PackageDepsSpecXmlParser INSTANCE = new PackageDepsSpecXmlParser();

    public static PackageDepsSpecXmlParser getInstance() {
        return INSTANCE;
    }

    protected enum Attribute implements XmlNameProvider {

        NAME("name"),
        OPTIONAL("optional"),

        // default unknown attribute
        UNKNOWN(null);

        private static final Map<QName, Attribute> attributes;

        static {
            attributes = new HashMap<>(3);
            attributes.put(new QName(NAME.name), NAME);
            attributes.put(new QName(OPTIONAL.name), OPTIONAL);
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

    public PackageDepsSpecXmlParser() {
        super();
    }

    public static void parsePackageDeps(XmlNameProvider parent, XMLExtendedStreamReader reader, PackageDepsSpecBuilder<?> pkgDeps) throws XMLStreamException {
        ParsingUtils.parseNoAttributes(reader);
        boolean empty = true;
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    if(empty) {
                        throw ParsingUtils.expectedAtLeastOneChild(reader, parent, xmlNameProvider(parent.getNamespace(), PACKAGE), xmlNameProvider(parent.getNamespace(), ORIGIN));
                    }
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    empty = false;
                    switch (reader.getLocalName()) {
                        case PACKAGE:
                            pkgDeps.addPackageDep(parsePackageDependency(reader));
                            break;
                        case ORIGIN:
                            parseOrigin(reader, pkgDeps);
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

    private static PackageDependencySpec parsePackageDependency(XMLExtendedStreamReader reader) throws XMLStreamException {
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
        return PackageDependencySpec.forPackage(name, optional);
    }

    private static void parseOrigin(XMLExtendedStreamReader reader, PackageDepsSpecBuilder<?> pkgDeps) throws XMLStreamException {
        String origin = null;
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i));
            switch (attribute) {
                case NAME:
                    origin = reader.getAttributeValue(i);
                    break;
                default:
                    throw ParsingUtils.unexpectedAttribute(reader, i);
            }
        }
        if (origin == null) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(Attribute.NAME));
        }

        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    switch (reader.getLocalName()) {
                        case PACKAGE:
                            pkgDeps.addPackageDep(origin, parsePackageDependency(reader));
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

    private static XmlNameProvider xmlNameProvider(String ns, String name) {
        return new XmlNameProvider() {
            public String getNamespace() {
                return ns;
            }
            public String getLocalName() {
                return name;
            }
        };
    }
}
