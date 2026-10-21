/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.pkg.stability.test;

import org.jboss.galleon.Constants;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.spec.ConfigLayerSpec;
import org.jboss.galleon.spec.PackageDependencySpec;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.ProvisionFromUniverseTestBase;
import org.jboss.galleon.xml.ProvisionedConfigBuilder;

/**
 * A test for a reference to a package that is not included in the FP.
 *
 * @author jfdenise
 */
public class SimpleNonExistingPackageInLayerTestCase extends ProvisionFromUniverseTestBase {

    private FeaturePackLocation prod1;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {

        prod1 = newFpl("prod1", "1", "1.0.0.Final");

        creator.newFeaturePack()
                .setFPID(prod1.getFPID())
                .addConfigLayer(ConfigLayerSpec.builder().setModel("model1").setName("layerA")
                        .addPackageDep(PackageDependencySpec.required("p1"))
                        .addPackageDep(PackageDependencySpec.optional("p2", Constants.STABILITY_EXPERIMENTAL))
                        .addPackageDep(PackageDependencySpec.passive("p3", Constants.STABILITY_COMMUNITY))
                        .build())
                .addConfig(ConfigModel.builder("model1", "name1")
                        .includeLayer("layerA")
                        .build())
                .newPackage("p1", true);

    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(prod1)
                        .build())
                .addOption(Constants.STABILITY_LEVEL, Constants.STABILITY_DEFAULT)
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(prod1.getFPID())
                        .addPackage("p1")
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setModel("model1")
                        .setName("name1")
                        .addLayer("model1", "layerA")
                        .build())
                .build();
    }
}