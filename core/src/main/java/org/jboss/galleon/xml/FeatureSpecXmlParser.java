/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.xml;

import java.io.Reader;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.spec.FeatureSpec;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeatureSpecXmlParser implements XmlParser<FeatureSpec> {

    private static final FeatureSpecXmlParser INSTANCE = new FeatureSpecXmlParser();

    public static FeatureSpecXmlParser getInstance() {
        return INSTANCE;
    }

    private FeatureSpecXmlParser() {
    }

    @Override
    public FeatureSpec parse(final Reader input) throws XMLStreamException, ProvisioningDescriptionException {
        final FeatureSpec.Builder builder = FeatureSpec.builder();
        XmlParsers.parse(input, builder);
        return builder.build();
    }
}
