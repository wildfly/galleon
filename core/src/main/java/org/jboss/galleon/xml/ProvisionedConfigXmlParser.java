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
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.state.ProvisionedConfig;

/**
 *
 * @author Alexey Loubyansky
 */
public class ProvisionedConfigXmlParser implements XmlParser<ProvisionedConfig> {

    private static ProvisionedConfigXmlParser INSTANCE;

    public static ProvisionedConfigXmlParser getInstance() {
        return INSTANCE == null ? INSTANCE = new ProvisionedConfigXmlParser() : INSTANCE;
    }

    public static ProvisionedConfig parse(Path p) throws ProvisioningException {
        try(BufferedReader reader = Files.newBufferedReader(p)) {
            return getInstance().parse(reader);
        } catch (IOException | XMLStreamException e) {
            throw new ProvisioningException(Errors.parseXml(p), e);
        }
    }

    private ProvisionedConfigXmlParser() {
    }

    @Override
    public ProvisionedConfig parse(Reader input) throws XMLStreamException, ProvisioningDescriptionException {
        final ProvisionedConfigBuilder builder = ProvisionedConfigBuilder.builder();
        XmlParsers.parse(input, builder);
        return builder.build();
    }
}
