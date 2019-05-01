/*
 * Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
