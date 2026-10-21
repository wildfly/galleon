/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.xml;

import java.io.Reader;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.config.FeatureGroup;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeatureGroupXmlParser implements XmlParser<FeatureGroup> {

    private static final FeatureGroupXmlParser INSTANCE = new FeatureGroupXmlParser();

    public static FeatureGroupXmlParser getInstance() {
        return INSTANCE;
    }

    private FeatureGroupXmlParser() {
    }

    @Override
    public FeatureGroup parse(final Reader input) throws XMLStreamException, ProvisioningDescriptionException {
        final FeatureGroup.Builder configBuilder = FeatureGroup.builder();
        XmlParsers.parse(input, configBuilder);
        return configBuilder.build();
    }
}
