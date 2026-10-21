/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.xml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.state.ProvisionedState;

/**
 *
 * @author Alexey Loubyansky
 */
public class ProvisionedStateXmlParser implements XmlParser<ProvisionedState> {

    private static final ProvisionedStateXmlParser INSTANCE = new ProvisionedStateXmlParser();

    public static ProvisionedStateXmlParser getInstance() {
        return INSTANCE;
    }

    public static ProvisionedState parse(Path path) throws ProvisioningException {
        if (!Files.exists(path)) {
            return null;
        }
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            return getInstance().parse(reader);
        } catch (IOException | XMLStreamException e) {
            throw new ProvisioningException(Errors.parseXml(path), e);
        }
    }

    private ProvisionedStateXmlParser() {
    }

    @Override
    public ProvisionedState parse(final Reader input) throws XMLStreamException {
        final ProvisionedState.Builder builder = ProvisionedState.builder();
        XmlParsers.parse(input, builder);
        return builder.build();
    }
}
