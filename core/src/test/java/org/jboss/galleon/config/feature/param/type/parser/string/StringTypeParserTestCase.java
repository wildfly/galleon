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
package org.jboss.galleon.config.feature.param.type.parser.string;

import org.jboss.galleon.config.feature.param.type.parser.TypeParserTestBase;
import org.jboss.galleon.util.formatparser.ParsingFormat;
import org.jboss.galleon.util.formatparser.formats.StringParsingFormat;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 * @author Alexey Loubyansky
 */
public class StringTypeParserTestCase extends TypeParserTestBase {

    private final ParsingFormat testFormat = StringParsingFormat.getInstance();

    @Override
    protected ParsingFormat getTestFormat() {
        return testFormat;
    }

    @Test
    public void testCharacteristics() {
        Assert.assertFalse(testFormat.isCollection());
        Assert.assertFalse(testFormat.isMap());
        Assert.assertTrue(testFormat.isOpeningChar('['));
        Assert.assertTrue(testFormat.isOpeningChar('{'));
        Assert.assertTrue(testFormat.isOpeningChar('s'));
    }

    @Test
    public void testEmptyString() throws Exception {
        testFormat("", "");
    }

    @Test
    public void testSimpleText() throws Exception {
        test("text", "text");
    }

    @Test
    public void testWhitespacesAroundTheText() throws Exception {
        test(" text ", "text");
    }

    @Test
    public void testWhitespacesAroundAndInTheText() throws Exception {
        test(" t e x t ", "t e x t");
    }

    @Test
    public void testListAsString() throws Exception {
        assertParsed("[a, b, c]", getTestFormat(), "[a, b, c]");
    }

    @Test
    public void testObjectAsString() throws Exception {
        assertParsed("{a=b, c=[1, 2, 3], d={z=x,v=y}]", getTestFormat(), "{a=b, c=[1, 2, 3], d={z=x,v=y}]");
    }
}
