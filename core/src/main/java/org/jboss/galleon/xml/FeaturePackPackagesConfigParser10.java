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

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.util.ParsingUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeaturePackPackagesConfigParser10 {

    private static final String EXCLUDE = "exclude";
    private static final String INCLUDE = "include";
    private static final String INHERIT = "inherit";
    private static final String NAME = "name";

    public static void readPackages(XMLStreamReader reader, FeaturePackConfig.Builder builder)
            throws XMLStreamException, ProvisioningDescriptionException {
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            if(reader.getAttributeLocalName(i).equals(INHERIT)) {
                builder.setInheritPackages(Boolean.parseBoolean(reader.getAttributeValue(i)));
            } else {
                throw ParsingUtils.unexpectedContent(reader);
            }
        }

        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final String localName = reader.getLocalName();
                    if (EXCLUDE.equals(localName)) {
                        builder.excludePackage(parseName(reader));
                    } else if (INCLUDE.equals(localName)) {
                        builder.includePackage(parseInclude(reader));
                    } else {
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

    private static String parseInclude(final XMLStreamReader reader) throws XMLStreamException {

        String name = null;
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            if(reader.getAttributeName(i).getLocalPart().equals(NAME)) {
                name = reader.getAttributeValue(i);
            } else {
                throw ParsingUtils.unexpectedContent(reader);
            }
        }
        if (name == null) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(new XmlNameProvider() {
                @Override
                public String getNamespace() {
                    return "";
                }
                @Override
                public String getLocalName() {
                    return NAME;
                }}));
        }
        ParsingUtils.parseNoContent(reader);
        return name;
    }

    private static String parseName(final XMLStreamReader reader) throws XMLStreamException {
        final int count = reader.getAttributeCount();
        String name = null;
        for (int i = 0; i < count; i++) {
            final String localName = reader.getAttributeLocalName(i);
            if (localName.equals(NAME)) {
                name = reader.getAttributeValue(i);
            } else {
                throw ParsingUtils.unexpectedContent(reader);
            }
        }
        if (name == null) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(new XmlNameProvider() {
                @Override
                public String getNamespace() {
                    return null;
                }

                @Override
                public String getLocalName() {
                    return NAME;
                }}));
        }
        ParsingUtils.parseNoContent(reader);
        return name;
    }
}
