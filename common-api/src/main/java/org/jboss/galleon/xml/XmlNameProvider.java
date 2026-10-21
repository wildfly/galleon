/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.xml;

/**
 * A common parent for Element and Attribute enums.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public interface XmlNameProvider {
    String getNamespace();
    String getLocalName();
}
