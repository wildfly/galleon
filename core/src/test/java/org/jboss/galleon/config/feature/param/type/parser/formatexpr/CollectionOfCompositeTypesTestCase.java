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
import org.jboss.galleon.util.formatparser.formats.StringParsingFormat;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 * @author Alexey Loubyansky
 */
public class CollectionOfCompositeTypesTestCase {

    final CompositeParsingFormat theType = CompositeParsingFormat.newInstance()
            .addElement("full-name", CompositeParsingFormat.newInstance()
                    .addElement("first-name", StringParsingFormat.getInstance())
                    .addElement("last-name", StringParsingFormat.getInstance()))
            .addElement("addresses", CollectionParsingFormat.list(
                    CompositeParsingFormat.newInstance()
                    .addElement("street", StringParsingFormat.getInstance())
                    .addElement("city", StringParsingFormat.getInstance())));

    static final String typeStr = "{\n"
            + "  full-name : {\n"
            + "    first-name : String,\n"
            + "    last-name : String\n"
            + "  },\n"
            + "  addresses : [{\n"
            + "    street : String,\n"
            + "    city : String\n"
            + "  }]\n"
            + "}";

    @Test
    public void testList() throws Exception {
        Assert.assertEquals(CollectionParsingFormat.list(theType), FormatParser.resolveFormat("List<" + typeStr + ">"));
        Assert.assertEquals(CollectionParsingFormat.list(theType), FormatParser.resolveFormat("[" + typeStr + "]"));
    }

    @Test
    public void testSet() throws Exception {
        Assert.assertEquals(CollectionParsingFormat.set(theType), FormatParser.resolveFormat("Set<" + typeStr + ">"));
    }
}
