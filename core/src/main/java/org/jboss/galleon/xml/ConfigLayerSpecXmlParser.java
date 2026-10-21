/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.xml;

import java.io.Reader;

import javax.xml.stream.XMLStreamException;
import org.jboss.galleon.ProvisioningDescriptionException;

import org.jboss.galleon.spec.ConfigLayerSpec;

/**
 *
 * @author Alexey Loubyansky
 */
public class ConfigLayerSpecXmlParser implements XmlParser<ConfigLayerSpec> {

    private static final ConfigLayerSpecXmlParser INSTANCE = new ConfigLayerSpecXmlParser();

    public static ConfigLayerSpecXmlParser getInstance() {
        return INSTANCE;
    }

    private ConfigLayerSpecXmlParser() {
    }

    @Override
    public ConfigLayerSpec parse(final Reader input) throws XMLStreamException,
            ProvisioningDescriptionException {
        final ConfigLayerSpec.Builder builder = ConfigLayerSpec.builder();
        XmlParsers.parse(input, builder);
        return builder.build();
    }
}
