/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.xml;

import java.io.Reader;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.config.FeatureConfig;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeatureConfigXmlParser implements XmlParser<FeatureConfig> {

    private static final FeatureConfigXmlParser INSTANCE = new FeatureConfigXmlParser();

    public static FeatureConfigXmlParser getInstance() {
        return INSTANCE;
    }

    private FeatureConfigXmlParser() {
    }

    @Override
    public FeatureConfig parse(final Reader input) throws XMLStreamException {
        final FeatureConfig config = new FeatureConfig();
        XmlParsers.parse(input, config);
        return config;
    }
}
