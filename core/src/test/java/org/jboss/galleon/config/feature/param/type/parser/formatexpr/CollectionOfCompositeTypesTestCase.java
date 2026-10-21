/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
