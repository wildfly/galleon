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
package org.jboss.galleon.config.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeatureGroup;
import org.jboss.galleon.spec.FeatureId;
import org.jboss.galleon.xml.FeatureGroupXmlParser;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeatureGroupParsingTestCase {

    @Test
    public void testMain() throws Exception {
        final FeatureGroup xmlConfig = parseConfig("feature-group.xml");
        final FeatureGroup expected = FeatureGroup.builder("groupName")
                .addFeatureGroup(FeatureGroup.builder("dep1").setInheritFeatures(true).build())
                .addFeatureGroup(FeatureGroup.builder("dep2").setInheritFeatures(false).build())
                .addFeatureGroup(FeatureGroup.builder("dep3")
                        .setInheritFeatures(false)
                        .includeSpec("spec1")
                        .includeFeature(FeatureId.fromString("spec2:p1=v1,p2=v2"),
                                new FeatureConfig("spec2")
                               .setParam("p1", "v1")
                               .setParam("p2", "v2"))
                        .includeFeature(
                                FeatureId.fromString("spec3:p1=v1"),
                                new FeatureConfig("spec3")
                                .addFeatureDep(FeatureId.fromString("spec4:p1=v1,p2=v2"))
                                .addFeatureDep(FeatureId.fromString("spec5:p1=v1,p2=v2"))
                                .setParam("p1", "v1")
                                .setParam("p2", "v2")
                                .addFeature(
                                        new FeatureConfig("spec9")
                                        .setParam("p1", "v1")
                                        .addFeature(FeatureConfig.newConfig("spec10")
                                                .addFeature(FeatureConfig.newConfig("spec11")
                                                        .setParentRef("spec10-ref")
                                                        .setParam("p1", "v1")))))
                        .excludeSpec("spec6")
                        .excludeSpec("spec7")
                        .excludeFeature(FeatureId.fromString("spec8:p1=v1"))
                        .excludeFeature(FeatureId.fromString("spec8:p1=v2"))
                        .build())
                .addFeatureGroup(FeatureGroup.forGroup("source4", "dep4"))
                .addFeature(
                        new FeatureConfig("spec1")
                        .setOrigin("source4")
                        .setParam("p1", "v1")
                        .setParam("p2", "v2")
                        .addFeatureDep(FeatureId.builder("spec2").setParam("p1", "v1").setParam("p2", "v2").build())
                        .addFeatureDep(FeatureId.create("spec3", "p3", "v3")))
                .addFeature(
                        new FeatureConfig("spec1")
                        .addFeatureDep(FeatureId.fromString("spec2:p1=v1,p2=v2"))
                        .addFeatureDep(FeatureId.fromString("spec3:p3=v3"))
                        .setParam("p1", "v1")
                        .setParam("p2", "v2"))
                .addFeature(
                        new FeatureConfig("spec4")
                        .setParam("p1", "v1")
                        .addFeature(FeatureConfig.newConfig("spec5")
                                .addFeature(FeatureConfig.newConfig("spec6")
                                        .setParentRef("spec5-ref")
                                        .setParam("p1", "v1"))))
                .addPackageDep("p1")
                .addPackageDep("p2", true)
                .addPackageDep("fp1", "p2")
                .addPackageDep("fp1", "p3", true)
                .build();
        assertEquals(expected, xmlConfig);
    }

    @Test
    public void testFeatureIdParameterInIncludeConflict() throws Exception {
        try {
            parseConfig("feature-id-parameter-in-include-conflict.xml");
        } catch(XMLStreamException e) {
            Assert.assertEquals("Failed to parse config", e.getMessage());
            Throwable cause = e.getCause();
            assertNotNull(cause);
            assertEquals("Parameter p2 has value 'v2' in feature ID and value 'v22' in the feature body", cause.getMessage());
        }
    }

    private static FeatureGroup parseConfig(String xml) throws Exception {
        final Path path = getResource("xml/config/" + xml);
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            return FeatureGroupXmlParser.getInstance().parse(reader);
        }
    }

    private static Path getResource(String path) {
        java.net.URL resUrl = Thread.currentThread().getContextClassLoader().getResource(path);
        Assert.assertNotNull("Resource " + path + " is not on the classpath", resUrl);
        try {
            return Paths.get(resUrl.toURI());
        } catch (java.net.URISyntaxException e) {
            throw new IllegalStateException("Failed to get URI from URL", e);
        }
    }
}
