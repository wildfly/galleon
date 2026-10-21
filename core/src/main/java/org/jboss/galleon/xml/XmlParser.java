/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.xml;

import java.io.Reader;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.ProvisioningDescriptionException;

/**
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 *
 * @param <T> the type of the object returned by the parse method
 */
public interface XmlParser<T> {
    T parse(Reader input) throws XMLStreamException, ProvisioningDescriptionException;
}
