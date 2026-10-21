/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.xml.test;

import java.nio.file.Paths;
import java.util.Locale;
import org.jboss.galleon.config.FeatureGroup;

import org.jboss.galleon.test.util.XmlParserValidator;
import org.jboss.galleon.xml.FeatureGroupXmlParser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class FeatureGroupXmlParserTestCase {

    private static final XmlParserValidator<FeatureGroup> validator = new XmlParserValidator<>(
            Paths.get("src/main/resources/schema/galleon-feature-group-1_0.xsd"), FeatureGroupXmlParser.getInstance());

    private static final Locale defaultLocale = Locale.getDefault();

    @BeforeClass
    public static void setLocale() {
        Locale.setDefault(Locale.US);
    }
    @AfterClass
    public static void resetLocale() {
        Locale.setDefault(defaultLocale);
    }

    @Test
    public void readValid() throws Exception {
        validator.validateAndParse("xml/config/feature-group.xml");
    }

}
