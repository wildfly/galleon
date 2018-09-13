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

package org.jboss.galleon.transitive.test;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.spec.FeatureId;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.util.fs.state.DirState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.ProvisionFromUniverseTestBase;
import org.jboss.galleon.xml.ProvisionedConfigBuilder;
import org.jboss.galleon.xml.ProvisionedFeatureBuilder;

/**
 *
 * @author Alexey Loubyansky
 */
public class TransitiveDepResolvesVersionConflictTestCase extends ProvisionFromUniverseTestBase {

    private FeaturePackLocation fp1Fpl;
    private FeaturePackLocation fp2Fpl;
    private FeaturePackLocation fp3_100_fpl;
    private FeaturePackLocation fp3_101_fpl;
    private FeaturePackLocation fp3_102_fpl;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("producer1");
        universe.createProducer("producer2");
        universe.createProducer("producer3");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {

        fp1Fpl = newFpl("producer1", "1", "1.0.0.Final");
        fp2Fpl = newFpl("producer2", "1", "1.0.0.Final");
        fp3_100_fpl = newFpl("producer3", "1", "1.0.0.Final");
        fp3_101_fpl = newFpl("producer3", "1", "1.0.1.Final");
        fp3_102_fpl = newFpl("producer3", "1", "1.0.2.Final");

        creator.newFeaturePack()
            .setFPID(fp1Fpl.getFPID())
            .addDependency(FeaturePackConfig.builder(fp3_100_fpl, false)
                .includePackage("p2")
                .includePackage("p3")
                .build())
            .addFeatureSpec(FeatureSpec.builder("specA")
                .addParam(FeatureParameterSpec.createId("p1"))
                .build())
            .addConfig(ConfigModel.builder("model1", "name1")
                .addFeature(new FeatureConfig("specA").setParam("p1", "1")).build())
            .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "fp1");

        creator.newFeaturePack()
            .setFPID(fp2Fpl.getFPID())
            .addDependency(FeaturePackConfig.builder(fp3_101_fpl)
                .includePackage("p2")
                .build())
            .addFeatureSpec(FeatureSpec.builder("specB")
                .addParam(FeatureParameterSpec.createId("p1"))
                .build())
            .addConfig(ConfigModel.builder("model1", "name1")
                .addFeature(new FeatureConfig("specB").setParam("p1", "1"))
                .build())
            .newPackage("p1", true)
                .writeContent("fp2/p1.txt", "fp2");

        creator.newFeaturePack()
            .setFPID(fp3_100_fpl.getFPID())
            .addFeatureSpec(FeatureSpec.builder("specC")
                .addParam(FeatureParameterSpec.createId("p1"))
                .addParam(FeatureParameterSpec.create("p2", "100"))
                .build())
            .addConfig(ConfigModel.builder("model1", "name1")
                .addFeature(new FeatureConfig("specC").setParam("p1", "1"))
                .build())
            .newPackage("p1", true)
                .writeContent("fp3/p1.txt", "fp3 p1 100")
                .getFeaturePack()
            .newPackage("p2")
                .writeContent("fp3/p2.txt", "fp3 p2 100")
                .getFeaturePack()
            .newPackage("p3")
                .writeContent("fp3/p3.txt", "fp3 p3 100")
                .getFeaturePack()
            .newPackage("p4")
                .writeContent("fp3/p4.txt", "fp3 p4 100");

        creator.newFeaturePack()
            .setFPID(fp3_101_fpl.getFPID())
            .addFeatureSpec(FeatureSpec.builder("specC")
                .addParam(FeatureParameterSpec.createId("p1"))
                .addParam(FeatureParameterSpec.create("p2", "101"))
                .addParam(FeatureParameterSpec.create("p3", "101"))
                .build())
            .addConfig(ConfigModel.builder("model1", "name1")
                .addFeature(new FeatureConfig("specC").setParam("p1", "2"))
                .build())
            .newPackage("p1", true)
                .writeContent("fp3/p1.txt", "fp3 101")
                .getFeaturePack()
            .newPackage("p2")
                .writeContent("fp3/p2.txt", "fp3 p2 101")
                .getFeaturePack()
            .newPackage("p3")
                .writeContent("fp3/p3.txt", "fp3 p3 101")
                .getFeaturePack()
            .newPackage("p4")
                .writeContent("fp3/p4.txt", "fp3 p4 101");

        creator.newFeaturePack()
            .setFPID(fp3_102_fpl.getFPID())
            .addFeatureSpec(FeatureSpec.builder("specC")
                .addParam(FeatureParameterSpec.createId("p1"))
                .addParam(FeatureParameterSpec.create("p2", "102"))
                .addParam(FeatureParameterSpec.create("p4", "102"))
                .build())
            .addConfig(ConfigModel.builder("model1", "name1")
                .addFeature(new FeatureConfig("specC").setParam("p1", "2"))
            .build())
            .newPackage("p1", true)
                .writeContent("fp3/p1.txt", "fp3 p1 102")
                .getFeaturePack()
            .newPackage("p2")
                .writeContent("fp3/p2.txt", "fp3 p2 102")
                .getFeaturePack()
            .newPackage("p3")
                .writeContent("fp3/p3.txt", "fp3 p3 102")
                .getFeaturePack()
            .newPackage("p4")
                .writeContent("fp3/p4.txt", "fp3 p4 102");

        creator.install();
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.transitiveBuilder(fp3_102_fpl)
                        .excludePackage("p2")
                        .includePackage("p4")
                        .build())
                .addFeaturePackDep(fp1Fpl)
                .addFeaturePackDep(fp2Fpl)
                .addConfig(ConfigModel.builder("model1", "name1")
                        .includeFeature(FeatureId.create("specC", "p1", "2"), new FeatureConfig().setParam("p4", "custom"))
                        .build())
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(fp3_102_fpl.getFPID())
                        .addPackage("p3")
                        .addPackage("p1")
                        .addPackage("p4")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(fp1Fpl.getFPID())
                        .addPackage("p1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(fp2Fpl.getFPID())
                        .addPackage("p1")
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setModel("model1")
                        .setName("name1")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(fp3_102_fpl.getFPID().getProducer(), "specC", "p1", "2"))
                                .setConfigParam("p2", "102")
                                .setConfigParam("p4", "custom")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(fp1Fpl.getFPID().getProducer(), "specA", "p1", "1")).build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(fp2Fpl.getFPID().getProducer(), "specB", "p1", "1")).build())
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1/p1.txt", "fp1")
                .addFile("fp2/p1.txt", "fp2")
                .addFile("fp3/p1.txt", "fp3 p1 102")
                .addFile("fp3/p3.txt", "fp3 p3 102")
                .addFile("fp3/p4.txt", "fp3 p4 102")
                .build();
    }
}