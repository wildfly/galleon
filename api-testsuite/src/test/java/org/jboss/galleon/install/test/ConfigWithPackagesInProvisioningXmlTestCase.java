/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.install.test;

import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.api.config.GalleonProvisioningConfig;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.ProvisionFromUniverseTestBase;

/**
 *
 * @author Alexey Loubyansky
 */
public class ConfigWithPackagesInProvisioningXmlTestCase extends ProvisionFromUniverseTestBase {

    private FeaturePackLocation fp1;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        fp1 = newFpl("prod1", "1", "1.0.0.Final");
        creator.newFeaturePack(fp1.getFPID())
            .addFeatureSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("id"))
                    .build())
            .newPackage("p1")
                .writeContent("fp1/p1.txt", "fp1 p1");
    }

    @Override
    protected GalleonProvisioningConfig provisioningConfig() throws ProvisioningDescriptionException {
        return GalleonProvisioningConfig.builder()
                .addFeaturePackDep(fp1)
                .addConfig(ConfigModel.builder("model1", "name1")
                        .addFeature(new FeatureConfig("specA").setParam("id", "1"))
                        .addPackageDep("p1")
                        .build())
                .build();
    }

    @Override
    protected String[] pmErrors() {
        return new String[] {
                Errors.failedToResolveConfigSpec("model1", "name1"),
                Errors.topConfigsCantDefinePackageDeps(new ConfigId("model1", "name1"))
        };
    }
}
