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
package org.jboss.galleon.state.xml.test;

import java.nio.file.Paths;

import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.util.XmlParserValidator;
import org.jboss.galleon.xml.ProvisionedStateXmlParser;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Alexey Loubyansky
 */
public class ProvisionedXmlParserTestCase {

    private static final XmlParserValidator<ProvisionedState> validator = new XmlParserValidator<>(
            Paths.get("src/main/resources/schema/galleon-provisioned-state-2_0.xsd"), ProvisionedStateXmlParser.getInstance());

    @Test
    public void readValid() throws Exception {
        final ProvisionedState found = validator
                .validateAndParse("xml/provisioned/provisioned-state.xml", null, null);
        ProvisionedState expected = ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.forFPID(FeaturePackLocation.fromString("fp1@maven(universe):0#0.0.1.Final").getFPID()))
                .addFeaturePack(ProvisionedFeaturePack.builder(FeaturePackLocation.fromString("fp2@maven(universe):0#0.0.2.Final").getFPID())
                        .addPackage("p1")
                        .addPackage("p2")
                        .build())
                .build();
        Assert.assertEquals(expected, found);
    }

}
