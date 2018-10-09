/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
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
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.spec.ConfigLayerSpec;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLMapper;

/**
 *
 * @author Alexey Loubyansky
 */
public class XmlParsers {

    private static final XmlParsers INSTANCE = new XmlParsers();

    private static final XMLInputFactory inputFactory;
    static {
        final XMLInputFactory tmpIF = XMLInputFactory.newInstance();
        setIfSupported(tmpIF, XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
        setIfSupported(tmpIF, XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        inputFactory = tmpIF;
    }

    private static void setIfSupported(final XMLInputFactory inputFactory, final String property, final Object value) {
        if (inputFactory.isPropertySupported(property)) {
            inputFactory.setProperty(property, value);
        }
    }

    public static XMLStreamReader createXMLStreamReader(Reader reader) throws XMLStreamException {
        return inputFactory.createXMLStreamReader(reader);
    }

    public static XmlParsers getInstance() {
        return INSTANCE;
    }

    public static void parse(final Reader reader, Object builder) throws XMLStreamException {
        INSTANCE.doParse(reader, builder);
    }

    public static ConfigLayerSpec parseConfigLayerSpec(Path p, String model) throws ProvisioningException {
        try(BufferedReader reader = Files.newBufferedReader(p)) {
            return parseConfigLayerSpec(reader, model);
        } catch (Exception e) {
            throw new ProvisioningException(Errors.parseXml(p), e);
        }
    }

    public static ConfigLayerSpec parseConfigLayerSpec(Reader reader, String model) throws ProvisioningException {
        ConfigLayerSpec.Builder builder = ConfigLayerSpec.builder();
        builder.setModel(model);
        try {
            parse(reader, builder);
        } catch (XMLStreamException e) {
            throw new ProvisioningException("Failed to parse config layer spec", e);
        }
        return builder.build();
    }

    private final XMLMapper mapper;

    private XmlParsers() {
        mapper = XMLMapper.Factory.create();
        new ConfigLayerXmlParser10().plugin(this);
        new ConfigXmlParser10().plugin(this);
        new FeatureConfigXmlParser10().plugin(this);
        new FeatureGroupXmlParser10().plugin(this);
        new FeaturePackXmlParser10().plugin(this);
        new FeaturePackXmlParser20().plugin(this);
        new FeatureSpecXmlParser10().plugin(this);
        new FileSystemDiffResultParser10().plugin(this);
        new PackageXmlParser10().plugin(this);
        new PackageXmlParser20().plugin(this);
        new ProvisionedStateXmlParser20().plugin(this);
        new ProvisionedStateXmlParser30().plugin(this);
        new ProvisioningXmlParser20().plugin(this);
        new ProvisioningXmlParser30().plugin(this);
    }

    public void plugin(QName root, XMLElementReader<?> reader) {
        mapper.registerRootElement(root, reader);
    }

    public void doParse(final Reader reader, Object builder) throws XMLStreamException {
        mapper.parseDocument(builder, inputFactory.createXMLStreamReader(reader));
    }
}
