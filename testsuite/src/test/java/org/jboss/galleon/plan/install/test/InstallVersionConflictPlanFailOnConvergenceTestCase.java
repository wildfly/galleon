/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.plan.install.test;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jboss.galleon.Constants;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningOption;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.layout.ProvisioningPlan;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.ProvisioningPlanTestBase;

/**
 *
 * @author Alexey Loubyansky
 */
public class InstallVersionConflictPlanFailOnConvergenceTestCase extends ProvisioningPlanTestBase {

    private FeaturePackLocation a100;
    private FeaturePackLocation b100;
    private FeaturePackLocation b101;
    private FeaturePackLocation c100;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("a");
        universe.createProducer("b");
        universe.createProducer("c");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {

        b100 = newFpl("b", "1", "1.0.0.Final");
        creator.newFeaturePack()
        .setFPID(b100.getFPID())
        .addFeatureSpec(FeatureSpec.builder("specB")
                .addParam(FeatureParameterSpec.createId("p1"))
                .build())
        .addConfig(ConfigModel.builder("model1", "name1")
                .addFeature(new FeatureConfig("specB").setParam("p1", "1")).build())
        .newPackage("p1", true)
        .writeContent("b/p1.txt", "b100");

        b101 = newFpl("b", "1", "1.0.1.Final");
        creator.newFeaturePack()
        .setFPID(b101.getFPID())
        .addFeatureSpec(FeatureSpec.builder("specB")
                .addParam(FeatureParameterSpec.createId("p1"))
                .build())
        .addConfig(ConfigModel.builder("model1", "name1")
                .addFeature(new FeatureConfig("specB").setParam("p1", "1")).build())
        .newPackage("p1", true)
        .writeContent("b/p1.txt", "b101");

        a100 = newFpl("a", "1", "1.0.0.Final");
        creator.newFeaturePack()
        .setFPID(a100.getFPID())
        .addDependency(b100)
        .addFeatureSpec(FeatureSpec.builder("specA")
                .addParam(FeatureParameterSpec.createId("p1"))
                .build())
        .addConfig(ConfigModel.builder("model1", "name1")
                .addFeature(new FeatureConfig("specA").setParam("p1", "1")).build())
        .newPackage("p1", true)
        .writeContent("a/p1.txt", "a100");

        c100 = newFpl("c", "1", "1.0.0.Final");
        creator.newFeaturePack()
        .setFPID(c100.getFPID())
        .addDependency(b101)
        .addFeatureSpec(FeatureSpec.builder("specC")
                .addParam(FeatureParameterSpec.createId("p1"))
                .build())
        .addConfig(ConfigModel.builder("model1", "name1")
                .addFeature(new FeatureConfig("specC").setParam("p1", "1")).build())
        .newPackage("p1", true)
        .writeContent("c/p1.txt", "c100");
    }

    @Override
    protected ProvisioningConfig initialState() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(a100)
                .addOption(ProvisioningOption.VERSION_CONVERGENCE.getName(), Constants.FAIL)
                .build();
    }

    @Override
    protected ProvisioningPlan getPlan() throws ProvisioningDescriptionException {
        return ProvisioningPlan.builder()
                .install(c100);
    }

    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(a100)
                        .setInheritConfigs(false)
                        .build())
                .addFeaturePackDep(c100)
                .build();
    }

    @Override
    protected String[] pmErrors() throws ProvisioningException {
        final Set<FPID> set = new LinkedHashSet<>(2);
        set.add(b100.getFPID());
        set.add(b101.getFPID());
        return new String[] {Errors.fpVersionCheckFailed(Collections.singletonList(set))};
    }
}