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
package org.jboss.galleon.featurepack.xml.test;

import java.nio.file.Paths;
import java.util.Locale;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeatureGroup;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.spec.FeatureId;
import org.jboss.galleon.spec.FeaturePackSpec;
import org.jboss.galleon.test.util.XmlParserValidator;
import org.jboss.galleon.xml.FeaturePackXmlParser;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class FeaturePackXmlParserTestCase  {

    private static final XmlParserValidator<FeaturePackSpec> validator = new XmlParserValidator<>(
            Paths.get("src/main/resources/schema/pm-feature-pack-1_0.xsd"), FeaturePackXmlParser.getInstance());

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
         * urn:wildfly:pm-feature-pack:1.0.1 used in feature-pack-1.0.1.xml is not registered in ProvisioningXmlParser
         */
        validator.validateAndParse("xml/feature-pack/feature-pack-1.0.1.xml",
                "Cannot find the declaration of element 'feature-pack'.",
                "Message: Unexpected element '{urn:wildfly:pm-feature-pack:1.0.1}feature-pack'");
    }

    @Test
    public void readFeaturePackGroupIdMissing() throws Exception {
        validator.validateAndParse("xml/feature-pack/feature-pack-1.0-feature-pack-groupId-missing.xml",
                "cvc-complex-type.4: Attribute 'groupId' must appear on element 'feature-pack'.",
                "Message: Missing required attributes groupId");
    }
    @Test
    public void readFeaturePackArtifactIdMissing() throws Exception {
        validator.validateAndParse("xml/feature-pack/feature-pack-1.0-feature-pack-artifactId-missing.xml",
                "cvc-complex-type.4: Attribute 'artifactId' must appear on element 'feature-pack'.",
                "Message: Missing required attributes artifactId");
    }

    @Test
    public void readPackageNameMissing() throws Exception {
        validator.validateAndParse("xml/feature-pack/feature-pack-1.0-package-name-missing.xml",
                "cvc-complex-type.4: Attribute 'name' must appear on element 'package'.",
                "Message: Missing required attributes name");
    }

    @Test
    public void readEmptyDependencies() throws Exception {
        validator.validateAndParse("xml/feature-pack/feature-pack-1.0-empty-dependencies.xml",
                "cvc-complex-type.2.4.b: The content of element 'dependencies' is not complete. One of '{\"urn:wildfly:pm-feature-pack:1.0\":dependency}' is expected.",
                "The content of element 'dependencies' is not complete. One of 'dependency' is expected.");
    }

    @Test
    public void readEmptyPackages() throws Exception {
        validator.validateAndParse("xml/feature-pack/feature-pack-1.0-empty-packages.xml",
                "cvc-complex-type.2.4.b: The content of element 'default-packages' is not complete. One of '{\"urn:wildfly:pm-feature-pack:1.0\":package}' is expected.",
                "The content of element 'default-packages' is not complete. One of 'package' is expected.");
    }

    @Test
    public void readEmpty() throws Exception {
        FeaturePackSpec found = validator.validateAndParse("xml/feature-pack/feature-pack-1.0-empty.xml", null, null);
        FeaturePackSpec expected = FeaturePackSpec.builder()
                .setGav(ArtifactCoords.newGav("org.jboss.fp.group1", "fp1", "1.0.0")).build();
        Assert.assertEquals(expected, found);
    }

    @Test
    public void readDependencyWithOrigin() throws Exception {
        FeaturePackSpec found = validator.validateAndParse("xml/feature-pack/feature-pack-deps-with-origin.xml", null, null);
        FeaturePackSpec expected = FeaturePackSpec.builder()
                .setGav(ArtifactCoords.newGav("org.jboss.fp.group1", "fp1", "1.0.0"))
                .addFeaturePackDep("dep1", FeaturePackConfig.forGav(ArtifactCoords.newGav("org.jboss.dep.group1", "dep1", "0.0.1")))
                .addFeaturePackDep("deptwo", FeaturePackConfig
                        .builder(ArtifactCoords.newGav("org.jboss.dep.group2", "dep2", "0.0.2"))
                        .excludePackage("excluded-package1")
                        .excludePackage("excluded-package2")
                        .includePackage("included-package1")
                        .includePackage("included-package2")
                        .build())
                .addDefaultPackage("package1")
                .addDefaultPackage("package2")
                .build();
        Assert.assertEquals(expected, found);
    }

    @Test
    public void readValid() throws Exception {
        FeaturePackSpec found = validator.validateAndParse("xml/feature-pack/feature-pack-1.0.xml", null, null);
        FeaturePackSpec expected = FeaturePackSpec.builder()
                .setGav(ArtifactCoords.newGav("org.jboss.fp.group1", "fp1", "1.0.0"))
                .addFeaturePackDep(FeaturePackConfig.forGav(ArtifactCoords.newGav("org.jboss.dep.group1", "dep1", "0.0.1")))
                .addFeaturePackDep(FeaturePackConfig
                        .builder(ArtifactCoords.newGav("org.jboss.dep.group2", "dep2", "0.0.2"))
                        .excludePackage("excluded-package1")
                        .excludePackage("excluded-package2")
                        .includePackage("included-package1")
                        .includePackage("included-package2")
                        .build())
                .addFeaturePackDep(FeaturePackConfig
                        .builder(ArtifactCoords.newGav("org.jboss.dep.group2", "dep3", "0.0.2"), false)
                        .excludePackage("excluded-package1")
                        .includePackage("included-package1")
                        .build())
                .addDefaultPackage("package1")
                .addDefaultPackage("package2")
                .build();
        Assert.assertEquals(expected, found);
    }

    @Test
    public void readVersionOptional() throws Exception {
        FeaturePackSpec found = validator.validateAndParse("xml/feature-pack/feature-pack-1.0-version-optional.xml", null, null);
        FeaturePackSpec expected = FeaturePackSpec.builder()
                .setGav(ArtifactCoords.newGav("org.jboss.fp.group1", "fp1", null))
                .addFeaturePackDep(FeaturePackConfig.forGav(ArtifactCoords.newGav("org.jboss.dep.group1", "dep1", null)))
                .addFeaturePackDep(FeaturePackConfig.forGav(ArtifactCoords.newGav("org.jboss.dep.group2", "dep2", null)))
                .addDefaultPackage("package1")
                .addDefaultPackage("package2")
                .build();
        Assert.assertEquals(expected, found);
    }

    @Test
    public void readDefaultConfigs() throws Exception {
        FeaturePackSpec found = validator.validateAndParse("xml/feature-pack/feature-pack-default-configs.xml", null, null);
        FeaturePackSpec expected = FeaturePackSpec.builder()
                .setGav(ArtifactCoords.newGav("org.jboss.fp.group1", "fp1", "1.0.0"))
                .addConfig(ConfigModel.builder().setName("config1").setModel("model1")
                    .setProperty("prop1", "value1")
                    .setProperty("prop2", "value2")
                    .addFeatureGroup(FeatureGroup.builder("fg1").build())
                    .addFeatureGroup(FeatureGroup.builder("fg2")
                            .excludeFeature(FeatureId.create("spec1", "p1", "v1"))
                            .build())
                    .addFeature(new FeatureConfig("spec1")
                        .addFeatureDep(FeatureId.fromString("spec2:p1=v1,p2=v2"))
                        .addFeatureDep(FeatureId.create("spec3", "p3", "v3"))
                        .setParam("p1", "v1")
                        .setParam("p2", "v2"))
                    .build())
                .addConfig(ConfigModel.builder().setModel("model2")
                    .setProperty("prop3", "value3")
                    .setProperty("prop4", "value4")
                    .addFeatureGroup(FeatureGroup.builder("fg3").build())
                    .addFeatureGroup(FeatureGroup.builder("fg4")
                            .excludeFeature(FeatureId.create("spec4", "p1", "v1"))
                            .build())
                    .addFeature(new FeatureConfig("spec5")
                        .addFeatureDep(FeatureId.fromString("spec6:p1=v1,p2=v2"))
                        .addFeatureDep(FeatureId.create("spec7", "p3", "v3"))
                        .setParam("p1", "v1")
                        .setParam("p2", "v2"))
                    .build())
                .addDefaultPackage("package1")
                .addDefaultPackage("package2")
                .build();
        Assert.assertEquals(expected, found);
    }

    @Test
    public void readUnnamedConfigs() throws Exception {
        FeaturePackSpec found = validator.validateAndParse("xml/feature-pack/feature-pack-unnamed-config.xml", null, null);
        FeaturePackSpec expected = FeaturePackSpec.builder()
                .setGav(ArtifactCoords.newGav("org.jboss.fp.group1", "fp1", "1.0.0"))
                .addConfig(ConfigModel.builder()
                        .setProperty("prop1", "value1")
                        .setProperty("prop1", "value1")
                        .setProperty("prop2", "value2")
                        .addFeatureGroup(FeatureGroup.builder("dep1").build())
                        .addFeatureGroup(FeatureGroup.builder("dep2").setInheritFeatures(false).build())
                        .addFeatureGroup(FeatureGroup.builder("dep3")
                                .setInheritFeatures(false)
                                .includeSpec("spec1")
                                .includeFeature(FeatureId.fromString("spec2:p1=v1,p2=v2"))
                                .includeFeature(FeatureId.fromString("spec3:p1=v1"), new FeatureConfig("spec3")
                                .addFeatureDep(FeatureId.fromString("spec4:p1=v1,p2=v2"))
                                .addFeatureDep(FeatureId.fromString("spec5:p1=v1,p2=v2"))
                                .setParam("p1", "v1")
                                .setParam("p2", "v2"))
                                .excludeSpec("spec6")
                                .excludeSpec("spec7")
                                .excludeFeature(FeatureId.fromString("spec8:p1=v1"))
                                .excludeFeature(FeatureId.fromString("spec8:p1=v2"))
                                .build())
                        .addFeatureGroup(FeatureGroup.builder("dep4").setOrigin("source4").build())
                        .addFeature(new FeatureConfig("spec1")
                                .addFeatureDep(FeatureId.fromString("spec2:p1=v1,p2=v2"))
                                .addFeatureDep(FeatureId.fromString("spec3:p3=v3"))
                                .setParam("p1", "v1")
                                .setParam("p2", "v2"))
                        .addFeature(new FeatureConfig("spec4")
                                .setParam("p1", "v1")
                                .addFeature(new FeatureConfig("spec5")
                                        .addFeature(new FeatureConfig("spec6")
                                                .setParentRef("spec5-ref")
                                                .setParam("p1", "v1"))))
                        .addPackageDep("p1")
                        .addPackageDep("p2", true)
                        .addPackageDep("fp1", "p2")
                        .addPackageDep("fp1", "p3", true)
                        .build())
                .addDefaultPackage("package1")
                .addDefaultPackage("package2")
                .build();
        Assert.assertEquals(expected, found);
    }
}
