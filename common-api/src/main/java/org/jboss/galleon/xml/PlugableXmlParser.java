/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.xml;

import javax.xml.namespace.QName;

import org.jboss.staxmapper.XMLElementReader;

/**
 *
 * @author Alexey Loubyansky
 */
public interface PlugableXmlParser<T> extends XMLElementReader<T> {

    default void plugin(XmlBaseParsers parsers) {
        parsers.plugin(getRoot(), this);
    }

    QName getRoot();
}
