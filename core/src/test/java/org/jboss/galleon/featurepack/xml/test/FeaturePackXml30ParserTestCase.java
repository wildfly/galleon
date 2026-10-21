/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.featurepack.xml.test;

import java.nio.file.Paths;
import java.util.Locale;

import org.jboss.galleon.spec.FeaturePackSpec;
import org.jboss.galleon.test.util.XmlParserValidator;
import org.jboss.galleon.util.PropertyUtils;
import org.jboss.galleon.xml.FeaturePackXmlParser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class FeaturePackXml30ParserTestCase {

    private static final XmlParserValidator<FeaturePackSpec> validator = new XmlParserValidator<>(
            Paths.get("src/main/resources/schema/galleon-feature-pack-3_0.xsd"), FeaturePackXmlParser.getInstance());

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
    public void readAbsoluteSystemPath() throws Exception {
        String path = "xml/feature-pack/feature-pack-3.0-absolute-system-path.xml";
        if (PropertyUtils.isWindows()) {
            path = "xml/feature-pack/feature-pack-3.0-absolute-system-path-win.xml";
        }
        validator.validateAndParse(path,
                                   null,
                                   "The content of 'system-path' element should be a path relative to installation base.");
    }
}
