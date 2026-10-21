/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.config.feature.param.type.parser.object;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.galleon.config.feature.param.type.parser.TypeParserTestBase;
import org.jboss.galleon.util.formatparser.ParsingFormat;
import org.jboss.galleon.util.formatparser.formats.ObjectParsingFormat;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 * @author Alexey Loubyansky
 */
public class ObjectTypeParserTestCase extends TypeParserTestBase {

    private final ParsingFormat testFormat = ObjectParsingFormat.getInstance();

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
    public void testSimpleKeyValue() throws Exception {
        test("{a=b}", Collections.singletonMap("a", "b"));
    }

    @Test
    public void testSequenceOfKeyValuePairs() throws Exception {
        final Map<String, String> map = new HashMap<>(3);
        map.put("a", "b");
        map.put("c", "d");
        map.put("e", "f");
        test("{a=b, c = d , e =f }", map);
    }

    @Test
    public void testListAsValue() throws Exception {
        test("{a=[b,c , d ]}", Collections.singletonMap("a", Arrays.asList("b", "c", "d")));
    }

    @Test
    public void testNestedObjects() throws Exception {
        final Map<String, String> map = new HashMap<>(2);
        map.put("c", "d");
        map.put("e", "f");
        test("{a={ b = { c=d, e = f }}}", Collections.singletonMap("a", Collections.singletonMap("b", map)));
    }

    @Test
    public void testKeyAlwaysString() throws Exception {
        testFormat("{[a] = [b , c ]}", Collections.singletonMap("[a]", Arrays.asList("b", "c")));

        testFormat("{{a = c}", Collections.singletonMap("{a", "c"));
    }
}
