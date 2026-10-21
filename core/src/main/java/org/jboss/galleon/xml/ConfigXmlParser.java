/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.xml;

import java.io.BufferedReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;

/**
 *
 * @author Emmanuel Hugonnet (c) 2018 Red Hat, inc.
 */
public class ConfigXmlParser implements XmlParser<ConfigModel> {

    private static final ConfigXmlParser INSTANCE = new ConfigXmlParser();

    public static ConfigXmlParser getInstance() {
        return INSTANCE;
    }

    public static ConfigModel parse(Path p) throws ProvisioningException {
        try(BufferedReader reader = Files.newBufferedReader(p)) {
            return INSTANCE.parse(reader);
        } catch (Exception e) {
            throw new ProvisioningException(Errors.parseXml(p), e);
        }
    }

    private ConfigXmlParser() {
    }

    @Override
    public ConfigModel parse(final Reader input) throws XMLStreamException, ProvisioningDescriptionException {
        final ConfigModel.Builder builder = ConfigModel.builder();
        XmlParsers.parse(input, builder);
        return builder.build();
    }
}
