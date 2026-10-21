/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.config.ConfigModel;
import org.jboss.staxmapper.XMLExtendedStreamReader;

/**
 *
 * @author Emmanuel Hugonnet (c) 2018 Red Hat, inc.
 */
class ConfigXmlParser10 implements PlugableXmlParser<ConfigModel.Builder> {

    public static final QName ROOT_1_0 = new QName(ConfigXml.NAMESPACE_1_0, ConfigXml.Element.CONFIG.getLocalName());

    @Override
    public QName getRoot() {
        return ROOT_1_0;
    }

    @Override
    public void readElement(XMLExtendedStreamReader reader, ConfigModel.Builder builder) throws XMLStreamException {
        ConfigXml.readConfig(reader, builder);
    }
}