/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.config.feature.pkg;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.api.config.GalleonFeaturePackConfig;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureReferenceSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.api.test.PmInstallFeaturePackTestBase;
import org.jboss.galleon.xml.ProvisionedConfigBuilder;
import org.jboss.galleon.xml.ProvisionedFeatureBuilder;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeatureSimplePackageDependenciesTestCase extends PmInstallFeaturePackTestBase {

    private static final FPID FP_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(FP_GAV)
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
            .addConfig(ConfigModel.builder()
                    .setName("main")
                    .setProperty("prop1", "value1")
                    .setProperty("prop2", "value2")
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("name", "b")
                            .setParam("a", "a"))
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("name", "a"))
                    .build())
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

    @Override
    protected GalleonFeaturePackConfig featurePackConfig() {
        return GalleonFeaturePackConfig.forLocation(FP_GAV.getLocation());
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(FP_GAV)
                        .addPackage("p1")
                        .addPackage("specA.pkg")
                        .addPackage("specB.pkg")
                        .addPackage("p2")
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setName("main")
                        .setProperty("prop1", "value1")
                        .setProperty("prop2", "value2")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specA", "name", "a")).build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specB", "name", "b"))
                                .setConfigParam("a", "a")
                                .build())
                        .build())
                .build();
    }
}
