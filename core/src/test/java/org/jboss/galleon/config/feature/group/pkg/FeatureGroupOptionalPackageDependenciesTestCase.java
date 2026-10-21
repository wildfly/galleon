/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.config.feature.group.pkg;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeatureGroup;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.PmInstallFeaturePackTestBase;
import org.jboss.galleon.xml.ProvisionedConfigBuilder;
import org.jboss.galleon.xml.ProvisionedFeatureBuilder;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeatureGroupOptionalPackageDependenciesTestCase extends PmInstallFeaturePackTestBase {

    private static final FPID FP_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(FP_GAV)
            .addFeatureSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addParam(FeatureParameterSpec.create("a", true))
                    .build())
            .addFeatureGroup(FeatureGroup.builder("fg1")
                    .addFeature(new FeatureConfig("specA")
                            .setParam("name", "afg1"))
                    .addPackageDep("fg1.pkg1")
                    .addPackageDep("fg1.pkg2", true)
                    .addPackageDep("fg1.pkg3", true)
                    .build())
            .addFeatureGroup(FeatureGroup.builder("fg2")
                    .addFeatureGroup(FeatureGroup.forGroup("fg1"))
                    .addFeature(new FeatureConfig("specA")
                            .setParam("name", "afg2"))
                    .addPackageDep("fg2.pkg1")
                    .addPackageDep("fg2.pkg2", true)
                    .addPackageDep("fg2.pkg3", true)
                    .build())
            .addConfig(ConfigModel.builder()
                    .setName("main")
                    .setProperty("prop1", "value1")
                    .setProperty("prop2", "value2")
                    .addFeatureGroup(FeatureGroup.forGroup("fg2"))
                    .build())
            .newPackage("fg1.pkg1")
                .getFeaturePack()
            .newPackage("fg1.pkg2")
                .getFeaturePack()
            .newPackage("fg1.pkg3")
                .getFeaturePack()
            .newPackage("fg2.pkg1")
                .getFeaturePack()
            .newPackage("fg2.pkg2")
                .getFeaturePack()
            .newPackage("fg2.pkg3");
    }

    @Override
    protected FeaturePackConfig featurePackConfig() throws ProvisioningDescriptionException {
        return FeaturePackConfig.builder(FP_GAV.getLocation())
                .excludePackage("fg1.pkg2")
                .excludePackage("fg2.pkg3")
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(FP_GAV)
                        .addPackage("fg1.pkg1")
                        .addPackage("fg1.pkg3")
                        .addPackage("fg2.pkg1")
                        .addPackage("fg2.pkg2")
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setName("main")
                        .setProperty("prop1", "value1")
                        .setProperty("prop2", "value2")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specA", "name", "afg1")).build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "specA", "name", "afg2")).build())
                        .build())
                .build();
    }
}
