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

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeatureGroup;
import org.jboss.galleon.spec.FeatureId;
import org.jboss.galleon.xml.FeatureConfigXmlParser;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeatureConfigXmlParsingTestCase {

    @Test
    public void testSimple() throws Exception {
        assertEquals(FeatureConfig.newConfig("feature-spec").setParam("param1", "value1").setParam("param2", "value2"),
                parseFeature("simple-feature.xml"));
    }

    @Test
    public void testFull() throws Exception {
        assertEquals(FeatureConfig.newConfig("feature-spec")
                .setParentRef("parent-spec")
                .addFeatureDep(FeatureId.create("spec1", "p1", "v1"))
                .addFeatureDep(FeatureId.fromString("spec2:p1=v1,p2=v2"))
                .setParam("param1", "value1")
                .setParam("param2", "value2")
                .addFeature(FeatureConfig.newConfig("child-spec")
                        .setParentRef("feature-spec-ref")
                        .addFeatureDep(FeatureId.fromString("spec3:p1=v1"))
                        .setParam("param3", "value3")
                        .addFeature(FeatureConfig.newConfig("grandchild-spec")
                                .setParam("param4", "value4")))
                .addFeatureGroup(FeatureGroup.builder("group1")
                        .includeFeature(FeatureId.create("spec3", "p1", "v1"),
                                new FeatureConfig("spec3")
                                .setParam("p1", "v1")
                                .setParam("p2", "v2")
                                .addFeatureDep(FeatureId.builder("spec4").setParam("p1", "v1").setParam("p2", "v2").build())
                                .addFeatureDep(FeatureId.builder("spec5").setParam("p1", "v1").setParam("p2", "v2").build())
                                .addFeature(
                                        new FeatureConfig("spec9")
                                        .setParam("p1", "v1")
                                        .addFeature(
                                                new FeatureConfig("spec10")
                                                .addFeature(new FeatureConfig("spec11")
                                                        .setParentRef("spec10-ref")
                                                        .setParam("p1", "v1")))))
                        .excludeSpec("spec6")
                        .excludeFeature(FeatureId.create("spec8", "p1", "v1"))
                        .build())
                .addFeature(FeatureConfig.newConfig("child-spec")
                        .setParentRef("feature-spec-ref")
                        .setParam("param5", "value5"))
                .addFeatureGroup(FeatureGroup.builder("group2")
                        .setOrigin("fp2")
                        .includeFeature(FeatureId.create("spec1", "p1", "v1"),
                                new FeatureConfig("spec1")
                                .setParentRef("parent1")
                                .setParam("p1", "v1")
                                .setParam("p2", "v2")
                                .addFeatureDep(FeatureId.builder("spec4").setParam("p1", "v1").setParam("p2", "v2").build()))
                        .excludeFeature(FeatureId.create("spec2", "p1", "v1"), "parent2")
                        .build())
                .addFeature(new FeatureConfig("spec2")
                                .setOrigin("fp2")
                                .setParam("p1", "v1")
                                .setParam("p2", "v2"))
                .addFeature(FeatureConfig.newConfig("another-spec")
                        .setParam("param6", "value6")), parseFeature("full-feature.xml"));
    }

    private static FeatureConfig parseFeature(String xml) throws Exception {
        final Path path = getResource("xml/feature/config/" + xml);
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            return FeatureConfigXmlParser.getInstance().parse(reader);
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
