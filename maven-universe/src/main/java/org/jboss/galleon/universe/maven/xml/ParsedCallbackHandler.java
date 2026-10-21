/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.universe.maven.xml;

import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Alexey Loubyansky
 */
public interface ParsedCallbackHandler<P, T> {

    P getParent();

    void parsed(T t) throws XMLStreamException;
}
