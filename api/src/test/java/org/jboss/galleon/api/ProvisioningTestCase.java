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

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.api.config.GalleonConfigurationWithLayersBuilder;
import org.jboss.galleon.api.config.GalleonFeaturePackConfig;
import org.jboss.galleon.api.config.GalleonProvisioningConfig;
import org.jboss.galleon.api.test.FeaturePackRepoTestBase;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.spec.ConfigLayerSpec;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureReferenceSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.UniverseSpec;
import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;

/**
 *
 * @author jdenise
 */
public class ProvisioningTestCase extends FeaturePackRepoTestBase {

    private static final FeaturePackLocation.FPID FP1_100_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");

    @Override
    protected void doBefore() throws Exception {
        super.doBefore();
        final FeaturePackCreator fpCreator = initCreator();
        createFeaturePacks(fpCreator);
        fpCreator.install();
    }

    @Test
    public void test() throws Exception {

        GalleonBuilder builder = new GalleonBuilder();
        builder.addArtifactResolver(repo);

        GalleonFeaturePackConfig fpConfig = GalleonFeaturePackConfig.builder(FP1_100_GAV.getLocation()).
                includeDefaultConfig("model1", "main").build();
        GalleonProvisioningConfig config = GalleonProvisioningConfig.builder().addFeaturePackDep(fpConfig)
                .addConfig(ConfigModel.builder()
                        .setModel("model1")
                        .setName("config1")
                        .addFeature(new FeatureConfig("specA").setParam("name", "13"))
                        .build()).
                build();

        assertEquals(config.getFeaturePackDep(FP1_100_GAV.getProducer()).getLocation(), FP1_100_GAV.getLocation());
        Path prov1 = workDir.resolve("prov1.xml");
        try (Provisioning p = builder.newProvisioningBuilder().setInstallationHome(installHome).build()) {
            p.provision(config);
            assertEquals(p.getProvisioningConfig(), config);
            assertNotNull(p.getProvisioningConfig().getDefinedConfig(new ConfigId("model1", "config1")));

            assertEquals(p.getProvisioningConfig().getFeaturePackDep(FP1_100_GAV.getProducer()).getLocation(), FP1_100_GAV.getLocation());

            p.storeProvisioningConfig(config, prov1);

            GalleonProvisioningConfig config2 = p.loadProvisioningConfig(prov1);
            assertEquals(config, config2);

            Provisioning p2 = builder.newProvisioningBuilder().setInstallationHome(installHome).build();
            assertEquals(p2.getProvisioningConfig(), config);
        }

        assertEquals(APIVersion.getVersion(), builder.getCoreVersion(prov1));

        try (Provisioning p3 = builder.newProvisioningBuilder(prov1).setInstallationHome(installHome).build()) {
            assertEquals(p3.getProvisioningConfig(), config);
            assertEquals(p3.getProvisioningConfig().getFeaturePackDeps().size(), 1);

            p3.addUniverse("foo", new UniverseSpec("factory"));
            assertEquals(p3.getProvisioningConfig().getUniverseSpec("foo"), new UniverseSpec("factory"));

            p3.removeUniverse("foo");
            assertEquals(p3.getProvisioningConfig().getUniverseNamedSpecs().size(), 0);

            assertNull(p3.getProvisioningConfig().getDefaultUniverse());
            p3.setDefaultUniverse(new UniverseSpec("def"));
            assertEquals(p3.getProvisioningConfig().getDefaultUniverse(), new UniverseSpec("def"));

            //Uninstall
            p3.uninstall(FP1_100_GAV);
            assertEquals(p3.getProvisioningConfig().getFeaturePackDeps().size(), 0);

            p3.install(fpConfig);
            assertEquals(p3.getProvisioningConfig().getFeaturePackDeps().size(), 1);

            p3.uninstall(FP1_100_GAV);
            assertEquals(p3.getProvisioningConfig().getFeaturePackDeps().size(), 0);

            p3.install(fpConfig);
            assertEquals(p3.getProvisioningConfig().getFeaturePackDeps().size(), 1);
        }
        //Configuration transform
        ProvisioningConfig pc = ProvisioningConfig.toConfig(config);
        GalleonProvisioningConfig pc2 = ProvisioningConfig.toConfig(pc);
        assertEquals(config, pc2);

        // Configuration with layers
        GalleonProvisioningConfig configLayers = GalleonProvisioningConfig.builder().addFeaturePackDep(fpConfig)
                .addConfig(GalleonConfigurationWithLayersBuilder.builder().setModel("model1").setName("withlayers").
                        includeLayer("base").build()).
                build();
        try (Provisioning p4 = builder.newProvisioningBuilder().setInstallationHome(installHome).build()) {
            p4.provision(configLayers);
            Set<String> layers = new HashSet<>();
            layers.add("base");
            assertEquals(p4.getProvisioningConfig().getDefinedConfig(new ConfigId("model1", "withlayers")).getIncludedLayers(), layers);
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
