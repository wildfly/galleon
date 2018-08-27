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

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.plugin.ProvisionedConfigHandler;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.runtime.ResolvedSpecId;
import org.jboss.galleon.state.ProvisionedConfig;
import org.jboss.galleon.state.ProvisionedFeature;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedPackage;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.util.XmlParserValidator;
import org.jboss.galleon.util.IoUtils;
import org.jboss.galleon.xml.ProvisionedConfigBuilder;
import org.jboss.galleon.xml.ProvisionedFeatureBuilder;
import org.jboss.galleon.xml.ProvisionedStateXmlParser;
import org.jboss.galleon.xml.ProvisionedStateXmlWriter;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class ProvisionedStateXmlWriterTestCase {

    private static final String SCHEMA = "schema/galleon-provisioned-state-3_0.xsd";

    private static XmlParserValidator<ProvisionedState> validator;
    private static Path tmpDir;

    @BeforeClass
    public static void before() throws Exception {
        URL xsd = ProvisionedStateXmlWriterTestCase.class.getClassLoader().getResource(SCHEMA);
        Assert.assertNotNull(xsd);
        validator = new XmlParserValidator<>(Paths.get(xsd.toURI()), ProvisionedStateXmlParser.getInstance());
        tmpDir = IoUtils.createRandomTmpDir();
    }

    @AfterClass
    public static void after() throws URISyntaxException {
        IoUtils.recursiveDelete(tmpDir);
    }

    @Test
    public void testMarshallUnmarshall() throws Exception {
        ProvisionedState originalState = ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(LegacyGalleon1Universe.newFPID("org.jboss.group", "fp1", "1.0"))
                        .addPackage("package1")
                        .addPackage(ProvisionedPackage.newInstance("package2"))
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(LegacyGalleon1Universe.newFPID("org.jboss.group", "fp2", "1.0"))
                        .addPackage("package3")
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.create(LegacyGalleon1Universe.newFPID("org.jboss.group", "fp1", "1.0").getProducer(), "spec1", "create-param", "a"))
                                .setConfigParam("param", "config")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.create(LegacyGalleon1Universe.newFPID("org.jboss.group", "fp1", "1.0").getProducer(), "spec1", "create-param", "b"))
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.create(LegacyGalleon1Universe.newFPID("org.jboss.group", "fp1", "1.0").getProducer(), "spec2", "create-param", "c"))
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                new ResolvedSpecId(LegacyGalleon1Universe.newFPID("org.jboss.group", "fp2", "1.0").getProducer(), "spec3"))
                                .setIdParam("id-param", "config")
                                .build())
                        .setModel("model")
                        .setName("name")
                        .setProperty("prop", "value")
                        .setProperty("prop2", "value2")
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .build())
                .build();

        // marshall to XML and then parse again
        Path path = marshallToTempFile(originalState);
        ProvisionedState newState = validator.validateAndParse(path);

        // compare parsed state with the original
        Assert.assertEquals(originalState, newState);
    }

    @Test
    public void testFeatureOrder() throws Exception {
        ProvisionedState originalState = ProvisionedState.builder()
                .addConfig(ProvisionedConfigBuilder.builder()
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.create(LegacyGalleon1Universe.newFPID("org.jboss.group", "fp1", "1.0").getProducer(), "spec1", "create-param", "a"))
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.create(LegacyGalleon1Universe.newFPID("org.jboss.group", "fp1", "1.0").getProducer(), "spec2", "create-param", "b"))
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.create(LegacyGalleon1Universe.newFPID("org.jboss.group", "fp1", "1.0").getProducer(), "spec1", "create-param", "c"))
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                new ResolvedSpecId(LegacyGalleon1Universe.newFPID("org.jboss.group", "fp2", "1.0").getProducer(), "spec3"))
                                .setIdParam("id-param", "b")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                new ResolvedSpecId(LegacyGalleon1Universe.newFPID("org.jboss.group", "fp2", "1.0").getProducer(), "spec3"))
                                .setIdParam("id-param", "a")
                                .build()
                        )
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.create(LegacyGalleon1Universe.newFPID("org.jboss.group", "fp1", "1.0").getProducer(), "spec1", "create-param", "c"))
                                .build())
                        .build())
                .build();

        // marshall and unmarshall
        Path path = marshallToTempFile(originalState);
        ProvisionedState newState = validator.validateAndParse(path);

        // read features and check the order
        ReadFeaturesHandler handler = new ReadFeaturesHandler();
        newState.getConfigs().iterator().next().handle(handler);
        Iterator<ProvisionedFeature> iterator = handler.features.iterator();

        Assert.assertEquals("{org.jboss.group@galleon1}spec1:create-param=a", iterator.next().getId().toString());
        Assert.assertEquals("{org.jboss.group@galleon1}spec2:create-param=b", iterator.next().getId().toString());
        Assert.assertEquals("{org.jboss.group@galleon1}spec1:create-param=c", iterator.next().getId().toString());
        Assert.assertEquals("{org.jboss.group@galleon1}spec3:id-param=b", iterator.next().getId().toString());
        Assert.assertEquals("{org.jboss.group@galleon1}spec3:id-param=a", iterator.next().getId().toString());
        Assert.assertEquals("{org.jboss.group@galleon1}spec1:create-param=c", iterator.next().getId().toString());
    }

    @Test
    public void testFeatureParams() throws Exception {
        ProvisionedState originalState = ProvisionedState.builder()
                .addConfig(ProvisionedConfigBuilder.builder()
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                new ResolvedSpecId(LegacyGalleon1Universe.newFPID("org.jboss.group", "fp", "1.0").getProducer(), "spec"))
                                .setIdParam("id-param", "config")
                                .setIdParam("id-param2", "config")
                                .setConfigParam("param", "config")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.builder(LegacyGalleon1Universe.newFPID("org.jboss.group", "fp", "1.0").getProducer(), "spec2")
                                        .setParam("id-param", "resolved") // TODO: this resolved value will not have config representation
                                        .build())
                                .setConfigParam("param", "config")
                                .build()))
                .build();

        // marshall and unmarshall
        Path path = marshallToTempFile(originalState);
        ProvisionedState newState = validator.validateAndParse(path);

        // retrieve the parsedFeature
        ReadFeaturesHandler readFeaturesHandler = new ReadFeaturesHandler();
        newState.getConfigs().get(0).handle(readFeaturesHandler);

        ProvisionedFeature parsedFeature = readFeaturesHandler.features.get(0);
        Assert.assertEquals("config", parsedFeature.getConfigParam("id-param"));
        Assert.assertEquals("config", parsedFeature.getConfigParam("id-param2"));
        Assert.assertEquals("config", parsedFeature.getConfigParam("param"));

        parsedFeature = readFeaturesHandler.features.get(1);
        Assert.assertEquals("resolved", parsedFeature.getConfigParam("id-param"));
        Assert.assertEquals("config", parsedFeature.getConfigParam("param"));
    }

    @Test
    public void testEmpty() throws Exception {
        ProvisionedState originalState = ProvisionedState.builder().build();

        Path path = marshallToTempFile(originalState);

        ProvisionedState newState = validator.parse(path);
        Assert.assertEquals(originalState, newState);
    }

    private Path marshallToTempFile(ProvisionedState state) throws Exception {
        final Path path = tmpDir.resolve("test-provisioned-state.xml");
        ProvisionedStateXmlWriter.getInstance().write(state, path);
        return path;
    }

    /**
     * Simple handler that extracts list of {@link ProvisionedFeature}s from a {@link ProvisionedConfig}.
     */
    private static class ReadFeaturesHandler implements ProvisionedConfigHandler {

        private List<ProvisionedFeature> features = new ArrayList<>();

        @Override
        public void nextFeature(ProvisionedFeature feature) {
            features.add(feature);
        }

    }
}
