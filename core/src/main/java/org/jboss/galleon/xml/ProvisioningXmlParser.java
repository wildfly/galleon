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
import org.jboss.galleon.config.ProvisioningConfig;

/**
 *
 * @author Alexey Loubyansky
 */
public class ProvisioningXmlParser implements XmlParser<ProvisioningConfig> {

    private static final ProvisioningXmlParser INSTANCE = new ProvisioningXmlParser();

    public static ProvisioningXmlParser getInstance() {
        return INSTANCE;
    }

    public static ProvisioningConfig parse(Path path) throws ProvisioningException {
        if (!Files.exists(path)) {
            return null;
        }
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            return getInstance().parse(reader);
        } catch (IOException | XMLStreamException e) {
            throw new ProvisioningException(Errors.parseXml(path), e);
        }
    }

    private ProvisioningXmlParser() {
    }

    @Override
    public ProvisioningConfig parse(final Reader input) throws XMLStreamException, ProvisioningDescriptionException {
        final ProvisioningConfig.Builder builder = ProvisioningConfig.builder();
        XmlParsers.parse(input, builder);
        return builder.build();
    }
}
