/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.config.FeatureGroup;
import org.jboss.staxmapper.XMLExtendedStreamReader;

/**
 *
 * @author Alexey Loubyansky
 */
class FeatureGroupXmlParser10 implements PlugableXmlParser<FeatureGroup.Builder> {

    public static final QName ROOT_1_0 = new QName(FeatureGroupXml.NAMESPACE_1_0, FeatureGroupXml.Element.FEATURE_GROUP_SPEC.getLocalName());

    public QName getRoot() {
        return ROOT_1_0;
    }

    @Override
    public void readElement(XMLExtendedStreamReader reader, FeatureGroup.Builder builder) throws XMLStreamException {
        FeatureGroupXml.readFeatureGroupSpec(reader, builder);
    }
}