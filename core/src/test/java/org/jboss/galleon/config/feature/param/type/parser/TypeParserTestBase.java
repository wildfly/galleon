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
package org.jboss.galleon.config.feature.param.type.parser;

import org.jboss.galleon.util.formatparser.FormatParser;
import org.jboss.galleon.util.formatparser.FormatParsingException;
import org.jboss.galleon.util.formatparser.ParsingFormat;
import org.junit.Assert;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class TypeParserTestBase {

    protected abstract ParsingFormat getTestFormat();

    protected void test(String original, Object result) throws FormatParsingException {
        testWildcard(original, result);
        testFormat(original, result);
    }

    protected void testWildcard(String original, Object result) throws FormatParsingException {
        assertParsed(original, result);
    }

    protected void testFormat(String original, Object result) throws FormatParsingException {
        assertParsed(original, getTestFormat(), result);
    }

    protected void assertParsed(String str, Object result) throws FormatParsingException {
        Assert.assertEquals(result, FormatParser.parse(str));
    }

    protected void assertParsed(String str, ParsingFormat format, Object result) throws FormatParsingException {
        Assert.assertEquals(result, FormatParser.parse(format, str));
    }

    protected Object parseFormat(String str) throws FormatParsingException {
        return FormatParser.parse(getTestFormat(), str);
    }

    protected void assertFailure(String str, String... msgs) {
        try {
            parseFormat(str);
            Assert.fail("Successfully parsed");
        } catch(Throwable t) {
            int i = 0;
            while(t != null && i < msgs.length) {
                Assert.assertEquals(msgs[i++], t.getLocalizedMessage());
                t = t.getCause();
            }
            if(t != null) {
                Assert.fail("Unexpected error: " + t.getLocalizedMessage());
            }
            if(i < msgs.length - 1) {
                Assert.fail("Not reported error: " + msgs[i]);
            }
        }
    }
}
