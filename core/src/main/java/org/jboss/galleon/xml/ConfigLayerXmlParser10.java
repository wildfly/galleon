/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.spec.ConfigLayerSpec;
import org.jboss.staxmapper.XMLExtendedStreamReader;

/**
 *
 * @author Alexey Loubyansky
 */
class ConfigLayerXmlParser10 implements PlugableXmlParser<ConfigLayerSpec.Builder> {

    public static final QName ROOT_1_0 = new QName(ConfigLayerXml.NAMESPACE_1_0, ConfigLayerXml.Element.LAYER_SPEC.getLocalName());

    @Override
    public QName getRoot() {
        return ROOT_1_0;
    }

    @Override
    public void readElement(XMLExtendedStreamReader reader, ConfigLayerSpec.Builder builder) throws XMLStreamException {
        ConfigLayerXml.readConfigLayer(reader, builder);
    }
}