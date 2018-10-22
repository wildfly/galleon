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
package org.jboss.galleon.xml.test;

import java.nio.file.Paths;
import java.util.Locale;

import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.test.util.XmlParserValidator;
import org.jboss.galleon.xml.ProvisioningXmlParser;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class ProvisioningXmlParserTestCase {

    private static final XmlParserValidator<ProvisioningConfig> validator = new XmlParserValidator<>(
            Paths.get("src/main/resources/schema/galleon-provisioning-3_0.xsd"), ProvisioningXmlParser.getInstance());

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
    public void readBadNamespace() throws Exception {
        /*
         * urn:jboss:galleon:provisioning:1.0.1 used in provisioning-1.0.1.xml is not registered in ProvisioningXmlParser
         */
        validator.validateAndParse("xml/provisioning/provisioning-1.0.1.xml",
                "Cannot find the declaration of element 'installation'.",
                "Message: Unexpected element '{urn:jboss:galleon:provisioning:1.0.1}installation'");
    }

    @Test
    public void readNoFp() throws Exception {
        validator.validateAndParse("xml/provisioning/provisioning-1.0-no-fp.xml", null, null);
    }

    @Test
    public void readValid() throws Exception {
        ProvisioningConfig found = validator
                .validateAndParse("xml/provisioning/provisioning-1.0.xml", null, null);
        ProvisioningConfig expected = ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackLocation.fromString("fp1@maven(universe):0#0.0.1"))
                .addFeaturePackDep(FeaturePackLocation.fromString("fp2@maven(universe):0#0.0.2"))
                .addFeaturePackDep(FeaturePackLocation.fromString("fp3@maven(universe):0#0.0.3"))
                .build();
        Assert.assertEquals(expected, found);
    }

}
