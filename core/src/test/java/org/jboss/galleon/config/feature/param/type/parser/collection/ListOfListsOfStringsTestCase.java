/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
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

import org.jboss.galleon.config.feature.param.type.parser.TypeParserTestBase;
import org.jboss.galleon.util.formatparser.FormatErrors;
import org.jboss.galleon.util.formatparser.ParsingFormat;
import org.jboss.galleon.util.formatparser.formats.CollectionParsingFormat;
import org.jboss.galleon.util.formatparser.formats.StringParsingFormat;
import org.junit.Assert;
import org.junit.Test;



/**
 *
 * @author Alexey Loubyansky
 */
public class ListOfListsOfStringsTestCase extends TypeParserTestBase {

    private final CollectionParsingFormat listOfStrings = CollectionParsingFormat.list(StringParsingFormat.getInstance());
    private final CollectionParsingFormat listOfLists = CollectionParsingFormat.list(listOfStrings);

    @Override
    protected ParsingFormat getTestFormat() {
        return listOfLists;
    }

    @Test
    public void testCharacteristics() {
        Assert.assertTrue(listOfLists.isCollection());
        Assert.assertFalse(listOfLists.isMap());
        Assert.assertTrue(listOfLists.isOpeningChar('['));
        Assert.assertFalse(listOfLists.isOpeningChar('{'));
        Assert.assertFalse(listOfLists.isOpeningChar('s'));
    }

    @Test
    public void testEmptyList() throws Exception {
        testFormat("[]", Collections.emptyList());
    }

    @Test
    public void testSimpleListOfStrings() throws Exception {
        assertFailure("[a,b , c ]",
                FormatErrors.parsingFailed("[a,b , c ]", 1, listOfStrings, 1),
                FormatErrors.unexpectedStartingCharacter(listOfStrings, '[', 'a'));
    }

    @Test
    public void testListOfEmptyList() throws Exception {
        testFormat("[[]]", Arrays.asList(Collections.emptyList()));
    }

    @Test
    public void testListOfEmptyLists() throws Exception {
        testFormat("[[] , [ ] ]", Arrays.asList(Collections.emptyList(), Collections.emptyList()));
    }

    @Test
    public void testListOfListsOfStrings() throws Exception {
        testFormat("[[a] , [ b,c ], [ a , b ,c] ]", Arrays.asList(Collections.singletonList("a"),
                Arrays.asList("b", "c"),
                Arrays.asList("a", "b", "c")));
    }

    @Test
    public void testListOfNestedLists() throws Exception {
        testFormat("[[a , [ b,c ], [ a , b ,c] ]", Arrays.asList(
                Arrays.asList("a", "[ b", "c"),
                Arrays.asList("a", "b", "c")));
    }
}
