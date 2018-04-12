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

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.config.FeaturePackConfig;
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
            Paths.get("src/main/resources/schema/pm-provisioning-1_0.xsd"), ProvisioningXmlParser.getInstance());

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
         * urn:wildfly:pm-provisioning:1.0.1 used in provisioning-1.0.1.xml is not registered in ProvisioningXmlParser
         */
        validator.validateAndParse("xml/provisioning/provisioning-1.0.1.xml",
                "Cannot find the declaration of element 'installation'.",
                "Message: Unexpected element '{urn:wildfly:pm-provisioning:1.0.1}installation'");
    }

    @Test
    public void readMissingGroupId() throws Exception {
        validator.validateAndParse("xml/provisioning/provisioning-1.0-missing-groupId.xml",
                "cvc-complex-type.4: Attribute 'groupId' must appear on element 'feature-pack'.",
                "Message: Missing required attributes groupId");
    }

    @Test
    public void readMissingArtifactId() throws Exception {
        validator.validateAndParse("xml/provisioning/provisioning-1.0-missing-artifactId.xml",
                "cvc-complex-type.4: Attribute 'artifactId' must appear on element 'feature-pack'.",
                "Message: Missing required attributes artifactId");
    }

    @Test
    public void readNoFp() throws Exception {
        validator.validateAndParse("xml/provisioning/provisioning-1.0-no-fp.xml",
                "cvc-complex-type.2.4.b: The content of element 'installation' is not complete. One of '{\"urn:wildfly:pm-provisioning:1.0\":feature-pack}' is expected.",
                "The content of element 'installation' is not complete. One of 'feature-pack' is expected.");
    }

    @Test
    public void readValid() throws Exception {
        ProvisioningConfig found = validator
                .validateAndParse("xml/provisioning/provisioning-1.0.xml", null, null);
        ProvisioningConfig expected = ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.forGav(ArtifactCoords.newGav("org.jboss.group1", "fp1", "0.0.1")))
                .addFeaturePackDep(FeaturePackConfig.forGav(ArtifactCoords.newGav("org.jboss.group1", "fp2", "0.0.2")))
                .addFeaturePackDep(FeaturePackConfig.forGav(ArtifactCoords.newGav("org.jboss.group2", "fp3", "0.0.3")))
                .build();
        Assert.assertEquals(expected, found);
    }

}
