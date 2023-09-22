/*
 * Copyright 2016-2023 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.api;

import java.nio.file.Files;
import java.nio.file.Path;
import org.jboss.galleon.CoreVersion;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.api.config.GalleonProvisioningConfig;
import org.jboss.galleon.api.test.FeaturePackRepoTestBase;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.spec.ConfigLayerSpec;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureReferenceSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.util.ZipUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jdenise
 */
public class GreaterVersionTestCase extends FeaturePackRepoTestBase {

    private static final FeaturePackLocation.FPID FP1_100_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");

    @Override
    protected void doBefore() throws Exception {
        super.doBefore();
        final FeaturePackCreator fpCreator = initCreator();
        createFeaturePacks(fpCreator);
        fpCreator.install();
    }

    @Test
    public void testOlderVersion() throws Exception {
        GalleonBuilder builder = new GalleonBuilder();
        builder.addArtifactResolver(repo);
        //Unzip and patch
        Path fp = workDir.resolve("repo/org/jboss/pm/test/fp1/1.0.0.Final/fp1-1.0.0.Final.zip");
        Path unzipped = workDir.resolve("unzipped");
        Files.createDirectory(unzipped);
        ZipUtils.unzip(fp, unzipped);
        Path fpDesc = unzipped.resolve("feature-pack.xml");
        String content = Files.readString(fpDesc);
        String patched = content.replace("galleon-min-version=\"" + CoreVersion.getVersion() + "\"", "galleon-min-version=\"9999.0.0.Final\"");
        Files.write(fpDesc, patched.getBytes());
        Files.delete(fp);
        ZipUtils.zip(unzipped, fp);

        // Use current version to provision it.
        GalleonProvisioningConfig config;
        try (Provisioning p = builder.newProvisioningBuilder().setInstallationHome(installHome).build()) {
            p.provision(GalleonProvisioningConfig.builder().addFeaturePackDep(FP1_100_GAV.getLocation()).build());
            config = p.getProvisioningConfig();
        }

        // Retrieve the provisioned config to build a new Provisioning
        try {
            try (Provisioning p2 = builder.newProvisioningBuilder(config).build()) {
                throw new Exception("Should have failed");
            }
        } catch (ProvisioningException ex) {
            Assert.assertTrue(ex.
                    getLocalizedMessage().contains("Artifact org.jboss.galleon:galleon-core:jar:9999.0.0.Final not found in the repository"));
        }
    }

    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
                .newFeaturePack(FP1_100_GAV)
                .addFeatureSpec(FeatureSpec.builder("specA")
                        .addParam(FeatureParameterSpec.createId("name"))
                        .addParam(FeatureParameterSpec.create("a", true))
                        .addPackageDep("specA.pkg")
                        .build())
                .addFeatureSpec(FeatureSpec.builder("specB")
                        .addParam(FeatureParameterSpec.createId("name"))
                        .addParam(FeatureParameterSpec.create("a", true))
                        .addFeatureRef(FeatureReferenceSpec.builder("specA")
                                .setName("specA")
                                .setNillable(false)
                                .mapParam("a", "name")
                                .build())
                        .addPackageDep("specB.pkg")
                        .build())
                .addConfigLayer(ConfigLayerSpec.builder()
                        .setModel("model1").setName("base")
                        .addFeature(new FeatureConfig("specA")
                                .setParam("name", "base-prod1")
                                .setParam("a", "base"))
                        .build())
                .addConfig(ConfigModel.builder()
                        .setName("main")
                        .setModel("model1")
                        .setProperty("prop1", "value1")
                        .setProperty("prop2", "value2")
                        .addFeature(
                                new FeatureConfig("specB")
                                        .setParam("name", "b")
                                        .setParam("a", "a"))
                        .addFeature(
                                new FeatureConfig("specA")
                                        .setParam("name", "a"))
                        .build(), true)
                .newPackage("p1", true)
                .getFeaturePack()
                .newPackage("specA.pkg")
                .getFeaturePack()
                .newPackage("specB.pkg")
                .addDependency("p2")
                .getFeaturePack()
                .newPackage("p2")
                .getFeaturePack();
    }
}
