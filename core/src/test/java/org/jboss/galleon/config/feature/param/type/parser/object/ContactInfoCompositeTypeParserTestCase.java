/*
 * Copyright ${license.git.copyrightYears} Red Hat, Inc. and/or its affiliates
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.galleon.config.feature.param.type.parser.TypeParserTestBase;
import org.jboss.galleon.util.formatparser.FormatErrors;
import org.jboss.galleon.util.formatparser.FormatParser;
import org.jboss.galleon.util.formatparser.ParsingFormat;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 * @author Alexey Loubyansky
 */
public class ContactInfoCompositeTypeParserTestCase extends TypeParserTestBase {

    private final ParsingFormat testFormat;

    public ContactInfoCompositeTypeParserTestCase() throws Exception {
        testFormat = FormatParser.resolveFormat(
                "{"
                + "  first-name : String,"
                + "  last-name : String,"
                + "  addresses : [{"
                + "    street : String,"
                + "    city : String"
                + "  }]"
                + "}");
    }

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

    @Test
    public void testStreetAddress() throws Exception {
        testFormat("{addresses=[{street=long 48}]}", Collections.singletonMap("addresses", Collections.singletonList(Collections.singletonMap("street", "long 48"))));
    }

    @Test
    public void testCityAddress() throws Exception {
        testFormat("{addresses=[{city=Greenburg}]}", Collections.singletonMap("addresses", Collections.singletonList(Collections.singletonMap("city", "Greenburg"))));
    }

    @Test
    public void testFullAddress() throws Exception {
        final Map<String, String> addr = new HashMap<>(2);
        addr.put("street", "long 48");
        addr.put("city", "Greenburg");
        testFormat("{addresses=[{street=long 48, city=Greenburg}]}", Collections.singletonMap("addresses", Collections.singletonList(addr)));
    }

    @Test
    public void testMultipleAddress() throws Exception {
        final Map<String, String> addr1 = new HashMap<>(2);
        addr1.put("street", "long 48");
        addr1.put("city", "Greenburg");
        final Map<String, String> addr2 = new HashMap<>(2);
        addr2.put("street", "short 4");
        addr2.put("city", "Greenville");
        testFormat("{\n"
                + "  addresses=[\n"
                + "    {street = long 48,\n"
                + "    city    = Greenburg},\n"
                + "    {street = short 4,\n"
                + "    city    = Greenville}\n"
                + "  ]\n"
                + "}", Collections.singletonMap("addresses", Arrays.asList(addr1, addr2)));
    }

    @Test
    public void testFullInfo() throws Exception {
        final Map<String, Object> expected = new HashMap<>(3);
        expected.put("first-name", "First");
        expected.put("last-name", "Last");
        final Map<String, String> addr1 = new HashMap<>(2);
        addr1.put("street", "long 48");
        addr1.put("city", "Greenburg");
        final Map<String, String> addr2 = new HashMap<>(2);
        addr2.put("street", "short 4");
        addr2.put("city", "Greenville");
        expected.put("addresses", Arrays.asList(addr1, addr2));
        testFormat("{\n"
                + "  first-name = First,\n"
                + "  last-name  = Last,\n"
                + "  addresses=[\n"
                + "    {street = long 48,\n"
                + "    city    = Greenburg},\n"
                + "    {street = short 4,\n"
                + "    city    = Greenville}\n"
                + "  ]\n"
                + "}", expected);
    }
}
