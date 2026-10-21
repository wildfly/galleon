/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.config.feature.param.type.parser.wildcard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.galleon.config.feature.param.type.parser.TypeParserTestBase;
import org.jboss.galleon.util.formatparser.ParsingFormat;
import org.jboss.galleon.util.formatparser.formats.WildcardParsingFormat;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 * @author Alexey Loubyansky
 */
public class WildcardTypeParserTestCase extends TypeParserTestBase {

    private final ParsingFormat testFormat = WildcardParsingFormat.getInstance();
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
        testFormat("", null);
    }

    @Test
    public void testString() throws Exception {
        testFormat("a b c", "a b c");
    }

    @Test
    public void testList() throws Exception {
        testFormat("[a b c]", Collections.singletonList("a b c"));
        testFormat("[a,b,c]", Arrays.asList("a", "b", "c"));
    }

    @Test
    public void testSimpleMap() throws Exception {
        testFormat("{a=b}", Collections.singletonMap("a", "b"));
        final Map<String, String> map = new HashMap<>(2);
        map.put("a", "b");
        map.put("c", "d");
        testFormat("{a=b, c = d }", map);
    }

    @Test
    public void testMapWithComplexKeys() throws Exception {
        testFormat("{[a]={b = c}}", Collections.singletonMap(Collections.singletonList("a"), Collections.singletonMap("b", "c")));

        final Map<Object, Object> map = new HashMap<>(2);

        List<Object> list = new ArrayList<>(2);
        list.add("b");
        list.add("c");
        final Map<?,?> key = Collections.singletonMap("a", list);

        list = new ArrayList<>(2);
        list.add("d");
        list.add(Collections.singletonMap("e", "f"));
        map.put(key, list);

        map.put("g", "h");
        testFormat("{{a=[b,c]} = [d, {e=f}], g = h}", map);
    }
}
