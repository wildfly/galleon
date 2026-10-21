/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.xml;

import java.io.Reader;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.spec.FeaturePackSpec;


/**
 *
 * @author Alexey Loubyansky
 */
public class FeaturePackXmlParser implements XmlParser<FeaturePackSpec>{

    private static final FeaturePackXmlParser INSTANCE = new FeaturePackXmlParser();

    public static FeaturePackXmlParser getInstance() {
        return INSTANCE;
    }

    private FeaturePackXmlParser() {
    }

    public void parse(final Reader input, final FeaturePackSpec.Builder fpBuilder) throws XMLStreamException {
        XmlParsers.parse(input, fpBuilder);
    }

    @Override
    public FeaturePackSpec parse(Reader input) throws XMLStreamException, ProvisioningDescriptionException {
        final FeaturePackSpec.Builder fpBuilder = FeaturePackSpec.builder();
        parse(input, fpBuilder);
        return fpBuilder.build();
    }
}
