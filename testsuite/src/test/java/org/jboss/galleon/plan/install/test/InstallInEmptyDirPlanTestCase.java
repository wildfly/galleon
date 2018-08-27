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

package org.jboss.galleon.plan.install.test;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
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
public class InstallInEmptyDirPlanTestCase extends ProvisioningPlanTestBase {

    private FeaturePackLocation a100;
    private FeaturePackLocation b100;
    private FeaturePackLocation c100;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("a");
        universe.createProducer("b");
        universe.createProducer("c");
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

        c100 = newFpl("c", "1", "1.0.0.Final");
        creator.newFeaturePack()
        .setFPID(c100.getFPID())
        .addFeatureSpec(FeatureSpec.builder("specC")
                .addParam(FeatureParameterSpec.createId("p1"))
                .build())
        .addConfig(ConfigModel.builder("model1", "name1")
                .addFeature(new FeatureConfig("specC").setParam("p1", "1")).build())
        .newPackage("p1", true)
        .writeContent("c/p1.txt", "c100");

        creator.install();
    }

    @Override
    protected ProvisioningConfig initialState() throws ProvisioningDescriptionException {
        return null;
    }

    @Override
    protected ProvisioningPlan getPlan() throws ProvisioningDescriptionException {
        return ProvisioningPlan.builder()
                .install(a100)
                .install(b100)
                .install(c100);
    }

    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(a100)
                .addFeaturePackDep(b100)
                .addFeaturePackDep(c100)
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(a100.getFPID())
                        .addPackage("p1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(b100.getFPID())
                        .addPackage("p1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(c100.getFPID())
                        .addPackage("p1")
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setModel("model1")
                        .setName("name1")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(a100.getFPID().getProducer(), "specA", "p1", "1")))
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(b100.getFPID().getProducer(), "specB", "p1", "1")))
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(c100.getFPID().getProducer(), "specC", "p1", "1")))
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("a/p1.txt", "a100")
                .addFile("b/p1.txt", "b100")
                .addFile("c/p1.txt", "c100")
                .build();
    }
}