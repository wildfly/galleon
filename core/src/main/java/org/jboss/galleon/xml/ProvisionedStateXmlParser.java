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
