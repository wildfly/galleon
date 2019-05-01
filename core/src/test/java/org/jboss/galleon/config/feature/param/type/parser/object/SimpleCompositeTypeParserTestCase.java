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
package org.jboss.galleon.config.feature.param.type.parser.object;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.galleon.config.feature.param.type.parser.TypeParserTestBase;
import org.jboss.galleon.util.formatparser.FormatErrors;
import org.jboss.galleon.util.formatparser.ParsingFormat;
import org.jboss.galleon.util.formatparser.formats.CompositeParsingFormat;
import org.jboss.galleon.util.formatparser.formats.StringParsingFormat;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 * @author Alexey Loubyansky
 */
public class SimpleCompositeTypeParserTestCase extends TypeParserTestBase {

    private final CompositeParsingFormat testFormat = CompositeParsingFormat.newInstance("FullName")
            .addElement("first-name", StringParsingFormat.getInstance())
            .addElement("last-name", StringParsingFormat.getInstance());

    @Override
    protected ParsingFormat getTestFormat() {
        return testFormat;
    }

    @Test
    public void testCharacteristics() {
        Assert.assertFalse(testFormat.isCollection());
        Assert.assertTrue(testFormat.isMap());
        Assert.assertFalse(testFormat.isOpeningChar('['));
        Assert.assertTrue(testFormat.isOpeningChar('{'));
        Assert.assertFalse(testFormat.isOpeningChar('s'));
    }

    @Test
    public void testEmpty() throws Exception {
        testFormat("{}", Collections.emptyMap());
    }

    @Test
    public void testFirstName() throws Exception {
        testFormat("{first-name=abc}", Collections.singletonMap("first-name", "abc"));
        testFormat("{ first-name = abc }", Collections.singletonMap("first-name", "abc"));
    }

    @Test
    public void testLastName() throws Exception {
        testFormat("{last-name=xyz}", Collections.singletonMap("last-name", "xyz"));
    }

    @Test
    public void testFullName() throws Exception {
        final Map<String, String> map = new HashMap<>();
        map.put("first-name", "abc");
        map.put("last-name", "xyz");

        testFormat("{first-name=abc,last-name=xyz}", map);
        testFormat("{last-name=xyz,first-name=abc}", map);
        testFormat("{ last-name = xyz , first-name = abc }", map);
    }

    @Test
    public void testUnexpectedElement() throws Exception {
        assertFailure("{abc=first-name}",
                FormatErrors.parsingFailed("{abc=first-name}", 1, testFormat, 0),
                FormatErrors.unexpectedCompositeFormatElement(testFormat, null));

        assertFailure("{first-named=abc}",
                FormatErrors.parsingFailed("{first-named=abc}", 16, testFormat, 0),
                FormatErrors.unexpectedCompositeFormatElement(testFormat, "first-named"));

    }
}
