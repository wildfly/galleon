/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.config.FeatureConfig;
import org.jboss.staxmapper.XMLExtendedStreamReader;

/**
 *
 * @author Alexey Loubyansky
 */
class FeatureConfigXmlParser10 implements PlugableXmlParser<FeatureConfig> {

    public static final QName ROOT_1_0 = new QName(FeatureGroupXml.NAMESPACE_1_0, FeatureGroupXml.Element.FEATURE.getLocalName());

    public QName getRoot() {
        return ROOT_1_0;
    }

    @Override
    public void readElement(XMLExtendedStreamReader reader, FeatureConfig config) throws XMLStreamException {
        FeatureGroupXml.readFeatureConfig(reader, config);
    }
}