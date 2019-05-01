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
package org.jboss.galleon.config.feature.param.type.parser.formatexpr;

import org.jboss.galleon.util.formatparser.FormatParser;
import org.jboss.galleon.util.formatparser.formats.CollectionParsingFormat;
import org.jboss.galleon.util.formatparser.formats.CompositeParsingFormat;
import org.jboss.galleon.util.formatparser.formats.MapParsingFormat;
import org.jboss.galleon.util.formatparser.formats.ObjectParsingFormat;
import org.jboss.galleon.util.formatparser.formats.StringParsingFormat;
import org.jboss.galleon.util.formatparser.formats.WildcardParsingFormat;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 * @author Alexey Loubyansky
 */
public class FormatExprParserTestCase {

    @Test
    public void testWildcard() throws Exception {
        Assert.assertEquals(WildcardParsingFormat.getInstance(), FormatParser.resolveFormat("?"));
        Assert.assertEquals(WildcardParsingFormat.getInstance(), FormatParser.resolveFormat(" ? "));
    }

    @Test
    public void testString() throws Exception {
        Assert.assertEquals(StringParsingFormat.getInstance(), FormatParser.resolveFormat("String"));
        Assert.assertEquals(StringParsingFormat.getInstance(), FormatParser.resolveFormat(" String "));
    }

    @Test
    public void testListOfWildcards() throws Exception {
        Assert.assertEquals(CollectionParsingFormat.list(), FormatParser.resolveFormat("List"));
        Assert.assertEquals(CollectionParsingFormat.list(), FormatParser.resolveFormat("List<?>"));
        Assert.assertEquals(CollectionParsingFormat.list(), FormatParser.resolveFormat(" List < ? > "));

        Assert.assertEquals(CollectionParsingFormat.list(), FormatParser.resolveFormat("[?]"));
        Assert.assertEquals(CollectionParsingFormat.list(), FormatParser.resolveFormat("[ ? ]"));

        Assert.assertEquals(CollectionParsingFormat.list(), FormatParser.resolveFormat("[]"));
        Assert.assertEquals(CollectionParsingFormat.list(), FormatParser.resolveFormat("[ ]"));
    }

    @Test
    public void testListOfStrings() throws Exception {
        Assert.assertEquals(CollectionParsingFormat.list(StringParsingFormat.getInstance()), FormatParser.resolveFormat("List<String>"));
        Assert.assertEquals(CollectionParsingFormat.list(StringParsingFormat.getInstance()), FormatParser.resolveFormat(" List < String > "));

        Assert.assertEquals(CollectionParsingFormat.list(StringParsingFormat.getInstance()), FormatParser.resolveFormat("[String]"));
        Assert.assertEquals(CollectionParsingFormat.list(StringParsingFormat.getInstance()), FormatParser.resolveFormat("[ String ]"));
    }

    @Test
    public void testListOfListsOfStrings() throws Exception {
        final CollectionParsingFormat theType = CollectionParsingFormat.list(
                CollectionParsingFormat.list(
                        StringParsingFormat.getInstance()));
        Assert.assertEquals(theType, FormatParser.resolveFormat("List<List<String>>"));
        Assert.assertEquals(theType, FormatParser.resolveFormat("[[String]]"));
    }

    @Test
    public void testSetOfWildcards() throws Exception {
        Assert.assertEquals(CollectionParsingFormat.set(), FormatParser.resolveFormat("Set"));
        Assert.assertEquals(CollectionParsingFormat.set(), FormatParser.resolveFormat("Set<?>"));
        Assert.assertEquals(CollectionParsingFormat.set(), FormatParser.resolveFormat(" Set < ? > "));
    }

    @Test
    public void testSetOfSetsOfStrings() throws Exception {
        Assert.assertEquals(CollectionParsingFormat.set(
                CollectionParsingFormat.set(StringParsingFormat.getInstance())
                ), FormatParser.resolveFormat("Set<Set<String>>"));
    }

    @Test
    public void testSimpleNamedComposite() throws Exception {
        Assert.assertEquals(CompositeParsingFormat.newInstance("FullName")
                .addElement("last-name", StringParsingFormat.getInstance())
                .addElement("first-name", StringParsingFormat.getInstance()), FormatParser.resolveFormat("{!name:FullName, first-name:String, last-name:String}"));

        Assert.assertEquals(CompositeParsingFormat.newInstance("FullName")
                .addElement("last-name", StringParsingFormat.getInstance())
                .addElement("first-name", StringParsingFormat.getInstance()), FormatParser.resolveFormat("{!name:FullName, !content-type:Map, first-name:String, last-name:String}"));
    }

    @Test
    public void testSimpleUnnamedComposite() throws Exception {
        Assert.assertEquals(CompositeParsingFormat.newInstance()
                .addElement("last-name", StringParsingFormat.getInstance())
                .addElement("first-name", StringParsingFormat.getInstance()), FormatParser.resolveFormat("{first-name:String, last-name:String}"));
    }

    @Test
    public void testCompositeWithAttrListOfWildcards() throws Exception {
        final CompositeParsingFormat theType = CompositeParsingFormat.newInstance()
                .addElement("str", StringParsingFormat.getInstance())
                .addElement("list", CollectionParsingFormat.list());
        Assert.assertEquals(theType, FormatParser.resolveFormat("{str:String, list:List}"));

        Assert.assertEquals(theType, FormatParser.resolveFormat("{str: String, list: []}"));
    }

    @Test
    public void testCompositeWithAttrListOfStrings() throws Exception {
        final CompositeParsingFormat theType = CompositeParsingFormat.newInstance()
                .addElement("str", StringParsingFormat.getInstance())
                .addElement("list", CollectionParsingFormat.list(StringParsingFormat.getInstance()));
        Assert.assertEquals(theType, FormatParser.resolveFormat("{str:String, list:List<String>}"));
        Assert.assertEquals(theType, FormatParser.resolveFormat("{str:String, list:[String]}"));
    }

    @Test
    public void testCompositeWithAttrWildcard() throws Exception {
        Assert.assertEquals(CompositeParsingFormat.newInstance()
                .addElement("wildcard", WildcardParsingFormat.getInstance()), FormatParser.resolveFormat("{wildcard:?}"));
    }

    @Test
    public void testCompositeWithAttrObject() throws Exception {
        Assert.assertEquals(CompositeParsingFormat.newInstance()
                .addElement("o", ObjectParsingFormat.getInstance()), FormatParser.resolveFormat("{o:Object}"));
    }

    @Test
    public void testCompositeWithAttrComposite() throws Exception {
        final CompositeParsingFormat theType = CompositeParsingFormat.newInstance()
                .addElement("str", StringParsingFormat.getInstance())
                .addElement("full-name", CompositeParsingFormat.newInstance()
                        .addElement("first-name", StringParsingFormat.getInstance())
                        .addElement("last-name", StringParsingFormat.getInstance()));
        Assert.assertEquals(theType, FormatParser.resolveFormat("{str:String, full-name:{first-name:String,last-name:String}}"));

        Assert.assertEquals(theType, FormatParser.resolveFormat(
                "{\n"
                + "  str : String,\n"
                + "  full-name : {\n"
                + "    first-name : String,\n"
                + "    last-name : String\n"
                + "  }\n"
                + "}"));
    }

    @Test
    public void testCompositeWithAttrList() throws Exception {
        final CompositeParsingFormat theType = CompositeParsingFormat.newInstance()
                .addElement("full-name", CompositeParsingFormat.newInstance()
                        .addElement("first-name", StringParsingFormat.getInstance())
                        .addElement("last-name", StringParsingFormat.getInstance()))
                .addElement("addresses", CollectionParsingFormat.list(
                        CompositeParsingFormat.newInstance()
                        .addElement("street", StringParsingFormat.getInstance())
                        .addElement("city", StringParsingFormat.getInstance())));
        Assert.assertEquals(theType, FormatParser.resolveFormat("{full-name: {first-name: String, last-name: String}, addresses: List<{street: String, city: String}>}"));

        Assert.assertEquals(theType, FormatParser.resolveFormat("{\n"
                + "  full-name : {\n"
                + "    first-name : String,\n"
                + "    last-name : String\n"
                + "  },\n"
                + "  addresses : [{\n"
                + "    street : String,\n"
                + "    city : String\n"
                + "  }]\n"
                + "}"));
    }

    @Test
    public void testMap() throws Exception {
        Assert.assertEquals(MapParsingFormat.getInstance(), FormatParser.resolveFormat("Map"));

        Assert.assertEquals(MapParsingFormat.getInstance(), FormatParser.resolveFormat("Map<>"));

        Assert.assertEquals(MapParsingFormat.getInstance(StringParsingFormat.getInstance(), StringParsingFormat.getInstance()), FormatParser.resolveFormat("Map<String, String>"));

        Assert.assertEquals(MapParsingFormat.getInstance(StringParsingFormat.getInstance(), CollectionParsingFormat.list(StringParsingFormat.getInstance())), FormatParser.resolveFormat("Map<String, List<String>>"));
    }
}
