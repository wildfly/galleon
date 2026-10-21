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
public interface MavenParsedProducerCallbackHandler {

    void parsedName(String name) throws XMLStreamException;
    void parsedFrequency(String frequency, boolean isDefault) throws XMLStreamException;
    void parsedDefaultChannel(String defaultChannel) throws XMLStreamException;
    void parsedFpGroupId(String groupId) throws XMLStreamException;
    void parsedFpArtifactId(String artifactId) throws XMLStreamException;
}
