/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
            Paths.get("src/main/resources/schema/galleon-provisioned-state-3_0.xsd"), ProvisionedStateXmlParser.getInstance());

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
