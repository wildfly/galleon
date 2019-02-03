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
package org.jboss.galleon.xml.test;

import java.nio.file.Paths;

import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.test.util.XmlParserValidator;
import org.jboss.galleon.xml.ProvisioningXmlParser;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Alexey Loubyansky
 */
public class IncludeDefaultTestCase {

    private static final XmlParserValidator<ProvisioningConfig> validator = new XmlParserValidator<>(
            Paths.get("src/main/resources/schema/galleon-provisioning-3_0.xsd"), ProvisioningXmlParser.getInstance());

    @Test
    public void testMain() throws Exception {
        ProvisioningConfig found = validator
                .validateAndParse("xml/provisioning/packages-inherit.xml", null, null);
        ProvisioningConfig expected = ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig
                        .forLocation(FeaturePackLocation.fromString("fp1@maven(universe):0#0.0.1")))
                .addFeaturePackDep(FeaturePackConfig
                        .forLocation(FeaturePackLocation.fromString("fp2@maven(universe):0#0.0.2")))
                .addFeaturePackDep(FeaturePackConfig
                        .forLocation(FeaturePackLocation.fromString("fp3@maven(universe):0#0.0.3")))
                .addFeaturePackDep(FeaturePackConfig
                        .builder(FeaturePackLocation.fromString("fp4@maven(universe):0#0.0.3"), false).build())
                .build();
        Assert.assertEquals(expected, found);
    }
}
