/*
 * Copyright 2016-2023 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.api.test.util;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.jboss.galleon.xml.XmlParser;
import org.junit.Assert;
import org.xml.sax.SAXException;

/**
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class XmlParserValidator<T> {

    private final Validator validator;

    private final XmlParser<T> parser;

    public XmlParserValidator(Path schemaPath, XmlParser<T> parser) {
        super();
        this.parser = parser;
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try (Reader r = Files.newBufferedReader(schemaPath, Charset.forName("utf-8"))) {
            Schema schema = schemaFactory.newSchema(new StreamSource(r));
            validator = schema.newValidator();
        } catch (IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }


    public void validate(Path p) throws SAXException, IOException {
        try(Reader reader = Files.newBufferedReader(p, Charset.forName("utf-8"))) {
            validator.validate(new StreamSource(reader));
        }
    }

    public T validateAndParse(String resourcePath) throws Exception {
        return validateAndParse(resourcePath, null, null);
    }

    public T validateAndParse(Path p) throws Exception {
        return validateAndParse(p, null, null);
    }

    public T validateAndParse(String resourcePath, String xsdValidationExceptionMessage,
            String parseExceptionMessage) throws Exception {

        final Path p = getResource(resourcePath);
        return validateAndParse(p, xsdValidationExceptionMessage, parseExceptionMessage);
    }

    public T validateAndParse(Path p, String xsdValidationExceptionMessage,
                              String parseExceptionMessage) throws Exception {
        try {
            validate(p);
            if(xsdValidationExceptionMessage != null) {
                Assert.fail("Schema validation passed while expected to fail with the error: " + xsdValidationExceptionMessage);
            }
        } catch (SAXException e) {
            if (xsdValidationExceptionMessage == null) {
                throw e;
            }
            Assert.assertTrue(e.getMessage().contains(xsdValidationExceptionMessage));
        }

        return parse(p, parseExceptionMessage);
    }

    public T parse(Path p) throws Exception {
        return parse(p, null);
    }

    public T parse(Path p, String parseExceptionMessage) throws Exception {
        T result = null;
        try (Reader reader = Files.newBufferedReader(p, Charset.forName("utf-8"))){
            result = parser.parse(reader);
            if(parseExceptionMessage != null) {
                Assert.fail("Parsing succeeded while expected to fail with the error: " + parseExceptionMessage);
            }
        } catch (XMLStreamException e) {
            String m = String.format("[%s] should contain [%s]", e.getMessage(), parseExceptionMessage);
            if(parseExceptionMessage == null) {
                Assert.fail(e.getMessage());
            } else {
                Assert.assertTrue(m, e.getMessage().contains(parseExceptionMessage));
            }
        }

        return result;
    }

    private static Path getResource(String path) {
        java.net.URL resUrl = Thread.currentThread().getContextClassLoader().getResource(path);
        Assert.assertNotNull("Resource " + path + " is not on the classpath", resUrl);
        try {
            return Paths.get(resUrl.toURI());
        } catch (java.net.URISyntaxException e) {
            throw new IllegalStateException("Failed to get URI from URL", e);
        }
    }
}
