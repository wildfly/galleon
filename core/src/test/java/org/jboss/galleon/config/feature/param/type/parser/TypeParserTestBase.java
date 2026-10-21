/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
