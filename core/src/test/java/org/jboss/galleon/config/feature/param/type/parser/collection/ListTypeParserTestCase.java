/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.config.feature.param.type.parser.collection;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.galleon.config.feature.param.type.parser.TypeParserTestBase;
import org.jboss.galleon.util.formatparser.FormatErrors;
import org.jboss.galleon.util.formatparser.ParsingFormat;
import org.jboss.galleon.util.formatparser.formats.CollectionParsingFormat;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 * @author Alexey Loubyansky
 */
public class ListTypeParserTestCase extends TypeParserTestBase {

    private final ParsingFormat testFormat = CollectionParsingFormat.list();

    @Override
    protected ParsingFormat getTestFormat() {
        return testFormat;
    }

    @Test
    public void testCharacteristics() {
        Assert.assertTrue(testFormat.isCollection());
        Assert.assertFalse(testFormat.isMap());
        Assert.assertTrue(testFormat.isOpeningChar('['));
        Assert.assertFalse(testFormat.isOpeningChar('{'));
        Assert.assertFalse(testFormat.isOpeningChar('s'));
    }

    @Test
    public void testEmptyList() throws Exception {
        testFormat("[]", Collections.emptyList());
    }

    @Test
    public void testIncompleteEmptyList() throws Exception {
        assertFailure("[",
                FormatErrors.parsingFailed("[", 1, testFormat, 0),
                FormatErrors.formatIncomplete(testFormat));
    }

    @Test
    public void testIncompleteList() throws Exception {
        assertFailure("[a , b",
                FormatErrors.parsingFailed("[a , b", 6, testFormat, 0),
                FormatErrors.formatIncomplete(testFormat));
    }

    @Test
    public void testSimpleListOfStrings() throws Exception {
        testFormat("[a,b , c ]", Arrays.asList("a", "b", "c"));
    }

    @Test
    public void testNestedListsOfStrings() throws Exception {
        testFormat("[a,[b , [ c ,d]] ]", Arrays.asList("a", Arrays.asList("b", Arrays.asList("c", "d"))));
    }

    @Test
    public void testListsOfObjects() throws Exception {
        final Map<String, String> df = new HashMap<>(2);
        df.put("d", "e");
        df.put("f", "g");
        testFormat("[{a=b} , { d = e , f = g } ]", Arrays.asList(Collections.singletonMap("a", "b"), df));
    }

    @Test
    public void testListsOfVariousTypes() throws Exception {
        final Map<String, String> df = new HashMap<>(2);
        df.put("d", "e");
        df.put("f", "g");
        testFormat("[a, [b, c] , { d = e , f = g } ]", Arrays.asList("a", Arrays.asList("b", "c"), df));
    }
}
