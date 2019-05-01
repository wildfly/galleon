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
package org.jboss.galleon.config.feature.param.type.parser.collection;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
public class SetTypeParserTestCase extends TypeParserTestBase {

    private final ParsingFormat testFormat = CollectionParsingFormat.set();

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
    public void testEmptySet() throws Exception {
        testFormat("[]", Collections.emptySet());
    }

    @Test
    public void testIncompleteEmptySet() throws Exception {
        assertFailure("[",
                FormatErrors.parsingFailed("[", 1, testFormat, 0),
                FormatErrors.formatIncomplete(testFormat));
    }

    @Test
    public void testIncompleteSet() throws Exception {
        assertFailure("[a , b",
                FormatErrors.parsingFailed("[a , b", 6, testFormat, 0),
                FormatErrors.formatIncomplete(testFormat));
    }

    @Test
    public void testSimpleSetOfStrings() throws Exception {
        final Set<String> set = new HashSet<>();
        set.add("a");
        set.add("b");
        set.add("c");
        testFormat("[a,b , a ,b, c ]", set);
    }

    @Test
    public void testSetOfListsOfStrings() throws Exception {
        final Set<List<String>> set = new HashSet<>();
        set.add(Collections.singletonList("a"));
        set.add(Collections.singletonList("b"));
        set.add(Arrays.asList("b", "c"));
        testFormat("[[a],[b] , [a] ,[b, c ] ]", set);
    }
}
