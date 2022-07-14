/*
 * Copyright 2016-2022 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.featurepack.xml.test;

import java.nio.file.Paths;
import java.util.Locale;

import org.jboss.galleon.spec.FeaturePackSpec;
import org.jboss.galleon.test.util.XmlParserValidator;
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
        validator.validateAndParse("xml/feature-pack/feature-pack-3.0-absolute-system-path.xml",
                                   null,
                                   "The content of 'system-path' element should be a path relative to installation base.");
    }
}
