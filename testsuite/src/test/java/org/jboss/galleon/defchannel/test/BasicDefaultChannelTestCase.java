/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.defchannel.test;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.spec.PackageDependencySpec;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.ProvisionFromUniverseTestBase;
import org.jboss.galleon.xml.ProvisionedConfigBuilder;
import org.jboss.galleon.xml.ProvisionedFeatureBuilder;

/**
 *
 * @author Alexey Loubyansky
 */
public class BasicDefaultChannelTestCase extends ProvisionFromUniverseTestBase {

    private FeaturePackLocation prod1_1;
    private FeaturePackLocation prod1_2;
    private FeaturePackLocation prod1_3;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1", 2, 3);
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {

        prod1_1 = newFpl("prod1", "1", "1.0.0.Final");
        creator.newFeaturePack()
            .setFPID(prod1_1.getFPID())
            .addFeatureSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("id"))
                    .addPackageDep(PackageDependencySpec.required("p2"))
                    .build())
            .newPackage("p1", true)
                .getFeaturePack()
            .newPackage("p2");

        prod1_2 = newFpl("prod1", "2", "2.0.0.Final");
        creator.newFeaturePack()
            .setFPID(prod1_2.getFPID())
            .addFeatureSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("id"))
                    .addPackageDep(PackageDependencySpec.required("p2"))
                    .build())
            .newPackage("p1", true)
                .getFeaturePack()
            .newPackage("p2");

        prod1_3 = newFpl("prod1", "3", "3.0.0.Final");
        creator.newFeaturePack()
            .setFPID(prod1_3.getFPID())
            .addFeatureSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("id"))
                    .addPackageDep(PackageDependencySpec.required("p2"))
                    .build())
            .newPackage("p1", true)
                .getFeaturePack()
            .newPackage("p2");

    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.forLocation(newProducerFpl("prod1")))
                .addConfig(ConfigModel.builder("model1", "name1")
                        .addFeature(new FeatureConfig("specA").setParam("id", "1"))
                        .build())
                .build();
    }

    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.forLocation(prod1_2))
                .addConfig(ConfigModel.builder("model1", "name1")
                        .addFeature(new FeatureConfig("specA").setParam("id", "1"))
                        .build())
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(prod1_2.getFPID())
                        .addPackage("p1")
                        .addPackage("p2")
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setModel("model1")
                        .setName("name1")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(prod1_1.getProducer(), "specA", "id", "1")))
                        .build())
                .build();
    }
}