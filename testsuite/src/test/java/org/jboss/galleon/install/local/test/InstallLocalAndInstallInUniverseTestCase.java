/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.install.local.test;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.util.fs.state.DirState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.xml.ProvisionedConfigBuilder;
import org.jboss.galleon.xml.ProvisionedFeatureBuilder;

/**
 *
 * @author Alexey Loubyansky
 */
public class InstallLocalAndInstallInUniverseTestCase extends InstallLocalTestBase {

    private FeaturePackLocation fp1;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {

        fp1 = newFpl("prod1", "1", "1.0.0.Final");

        creator.newFeaturePack()
        .setFPID(fp1.getFPID())
        .addFeatureSpec(FeatureSpec.builder("specA")
                .addParam(FeatureParameterSpec.createId("p1"))
                .build())
        .addConfig(ConfigModel.builder("model1", "name1")
                .addFeature(new FeatureConfig("specA").setParam("p1", "1")).build())
        .newPackage("p1", true)
        .writeContent("fp1/p1.txt", "fp1");

    }

    @Override
    protected FPID installLocalFPID() {
        return fp1.getFPID();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(fp1.getFPID())
                        .addPackage("p1")
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setModel("model1")
                        .setName("name1")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(fp1.getFPID().getProducer(), "specA", "p1", "1")))
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1/p1.txt", "fp1")
                .build();
    }
}