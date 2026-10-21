/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.provision.config.layers.test;

import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.spec.ConfigLayerSpec;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.ProvisionFromUniverseTestBase;

/**
 *
 * @author Alexey Loubyansky
 */
public class ExcludeRequiredPackageFromConfigLayerTestCase extends ProvisionFromUniverseTestBase {

    private FeaturePackLocation prod2;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod2");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {

        prod2 = newFpl("prod2", "1", "1.0.0.Final");

        creator.newFeaturePack()
            .setFPID(prod2.getFPID())
            .addConfigLayer(ConfigLayerSpec.builder()
                .setModel("model1").setName("base")
                .addFeature(new FeatureConfig("specB").setParam("id", "1"))
                .addPackageDep("prod2.layer.base")
                .build())
            .newPackage("prod2.layer.base");

    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(prod2)
                        .excludePackage("prod2.layer.base")
                        .build())
                .addConfig(ConfigModel.builder("model1", "name1")
                        .includeLayer("base")
                        .build())
                .build();
    }

    @Override
    protected String[] pmErrors() {
        return new String[] {
                Errors.failedToResolveConfigLayer("model1", "base"),
                Errors.unsatisfiedPackageDependency(prod2.getFPID(), "prod2.layer.base")};
    }
}