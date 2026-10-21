/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.xml.test;

import java.nio.file.Paths;
import java.util.Locale;

import org.jboss.galleon.spec.ConfigLayerSpec;
import org.jboss.galleon.test.util.XmlParserValidator;
import org.jboss.galleon.xml.ConfigLayerSpecXmlParser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class LayerXmlParserTestCase {

    private static final XmlParserValidator<ConfigLayerSpec> validator = new XmlParserValidator<>(
            Paths.get("src/main/resources/schema/galleon-layer-2_0.xsd"), ConfigLayerSpecXmlParser.getInstance());

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
        validator.validateAndParse("xml/layer/layer-spec-test.xml");
    }

}
