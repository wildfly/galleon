/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
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

package org.jboss.galleon.plan.test;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.layout.FeaturePackUpdatePlan;
import org.jboss.galleon.layout.ProvisioningPlan;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.util.fs.state.DirState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.ProvisioningPlanTestBase;
import org.jboss.galleon.xml.ProvisionedConfigBuilder;
import org.jboss.galleon.xml.ProvisionedFeatureBuilder;

/**
 *
 * @author Alexey Loubyansky
 */
public class BasicPlanTestCase extends ProvisioningPlanTestBase {

    private FeaturePackLocation a100;
    private FeaturePackLocation b100;
    private FeaturePackLocation b100Patch1;
    private FeaturePackLocation b100Patch2;
    private FeaturePackLocation c100;
    private FeaturePackLocation d100;
    private FeaturePackLocation d101;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("a");
        universe.createProducer("b");
        universe.createProducer("c");
        universe.createProducer("d");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {

        a100 = newFpl("a", "1", "1.0.0.Final");
        creator.newFeaturePack()
        .setFPID(a100.getFPID())
        .addFeatureSpec(FeatureSpec.builder("specA")
                .addParam(FeatureParameterSpec.createId("p1"))
                .build())
        .addConfig(ConfigModel.builder("model1", "name1")
                .addFeature(new FeatureConfig("specA").setParam("p1", "1")).build())
        .newPackage("p1", true)
        .writeContent("a/p1.txt", "a100");

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

        b100Patch1 = newFpl("b", "1", "1.0.0.Patch1");
        creator.newFeaturePack()
        .setFPID(b100Patch1.getFPID())
        .setPatchFor(b100.getFPID())
        .newPackage("p1", true)
            .addDependency("p2")
            .addDependency("p3")
            .writeContent("b/p1.txt", "b100")
            .getFeaturePack()
        .newPackage("p2")
            .writeContent("b/p2.txt", "b100 patch1")
            .getFeaturePack()
        .newPackage("p3")
            .writeContent("b/p3.txt", "b100 patch1");

        b100Patch2 = newFpl("b", "1", "1.0.0.Patch2");
        creator.newFeaturePack()
        .setFPID(b100Patch2.getFPID())
        .setPatchFor(b100.getFPID())
        .addDependency(b100Patch1)
        .newPackage("p3")
            .writeContent("b/p3.txt", "b100 patch2");

        c100 = newFpl("c", "1", "1.0.0.Final");
        creator.newFeaturePack()
        .setFPID(c100.getFPID())
        .addFeatureSpec(FeatureSpec.builder("specC")
                .addParam(FeatureParameterSpec.createId("p1"))
                .build())
        .addConfig(ConfigModel.builder("model1", "name1")
                .addFeature(new FeatureConfig("specC").setParam("p1", "1")).build())
        .newPackage("p1", true)
        .writeContent("c/p1.txt", "b100");

        d100 = newFpl("d", "1", "1.0.0.Final");
        creator.newFeaturePack()
        .setFPID(d100.getFPID())
        .addFeatureSpec(FeatureSpec.builder("specD")
                .addParam(FeatureParameterSpec.createId("p1"))
                .build())
        .addConfig(ConfigModel.builder("model1", "name1")
                .addFeature(new FeatureConfig("specD").setParam("p1", "100")).build())
        .newPackage("p1", true)
        .writeContent("d/p1.txt", "d100");

        d101 = newFpl("d", "1", "1.0.1.Final");
        creator.newFeaturePack()
        .setFPID(d101.getFPID())
        .addFeatureSpec(FeatureSpec.builder("specD")
                .addParam(FeatureParameterSpec.createId("p1"))
                .build())
        .addConfig(ConfigModel.builder("model1", "name1")
                .addFeature(new FeatureConfig("specD").setParam("p1", "101")).build())
        .newPackage("p1", true)
        .writeContent("d/p1.txt", "d101");

        creator.install();
    }

    @Override
    protected ProvisioningConfig initialState() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(b100).build())
                .addFeaturePackDep(c100)
                .addFeaturePackDep(d100)
                .build();
    }

    @Override
    protected ProvisioningPlan getPlan() throws ProvisioningDescriptionException {
        return ProvisioningPlan.builder()
                .install(a100)
                .update(FeaturePackUpdatePlan.request(b100).addNewPatch(b100Patch2.getFPID()).buildPlan())
                .update(FeaturePackUpdatePlan.request(d100).setNewLocation(d101).buildPlan())
                .uninstall(c100.getProducer());
    }

    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(b100)
                        .addPatch(b100Patch2.getFPID())
                        .build())
                .addFeaturePackDep(d101)
                .addFeaturePackDep(a100)
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(b100.getFPID())
                        .addPackage("p1")
                        .addPackage("p2")
                        .addPackage("p3")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(d101.getFPID())
                        .addPackage("p1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(a100.getFPID())
                        .addPackage("p1")
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setModel("model1")
                        .setName("name1")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(b100.getFPID().getProducer(), "specB", "p1", "1")))
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(d101.getFPID().getProducer(), "specD", "p1", "101")))
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(a100.getFPID().getProducer(), "specA", "p1", "1")))
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("a/p1.txt", "a100")
                .addFile("b/p1.txt", "b100")
                .addFile("b/p2.txt", "b100 patch1")
                .addFile("b/p3.txt", "b100 patch2")
                .addFile("d/p1.txt", "d101")
                .build();
    }
}