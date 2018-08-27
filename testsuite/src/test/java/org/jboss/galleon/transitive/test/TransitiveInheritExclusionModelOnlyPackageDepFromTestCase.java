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
public class TransitiveInheritExclusionModelOnlyPackageDepFromTestCase extends ProvisionFromUniverseTestBase {

    private FeaturePackLocation fp1;
    private FeaturePackLocation fp2;
    private FeaturePackLocation fp3;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1");
        universe.createProducer("prod2");
        universe.createProducer("prod3");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {

        fp1 = newFpl("prod1", "1", "1.0.0.Final");
        fp2 = newFpl("prod2", "1", "1.0.0.Final");
        fp3 = newFpl("prod3", "1", "1.0.0.Final");

        creator.newFeaturePack()
        .setFPID(fp1.getFPID())
        .addDependency(fp2)
        .addDependency(FeaturePackConfig.transitiveBuilder(fp3)
                .excludePackage("p2")
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
            .setFPID(fp2.getFPID())
            .addDependency(FeaturePackConfig.builder(fp3)
                    .excludePackage("p4")
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
            .setFPID(fp3.getFPID())
            .addFeatureSpec(FeatureSpec.builder("specC")
                .addParam(FeatureParameterSpec.createId("p1"))
                .build())
            .addConfig(ConfigModel.builder("model1", null)
                    .addPackageDep("p4", true)
                    .addPackageDep("p5", true)
                    .build())
            .addConfig(ConfigModel.builder("model1", "name1")
                .addFeature(new FeatureConfig("specC").setParam("p1", "1"))
                .build())
            .newPackage("p1", true)
                .addDependency("p2", true)
                .writeContent("fp3/p1.txt", "fp3 100 p1")
                .getFeaturePack()
            .newPackage("p2")
                .writeContent("fp3/p2.txt", "fp3 100 p2")
                .getFeaturePack()
            .newPackage("p3")
                .writeContent("fp3/p3.txt", "fp3 100 p3")
                .getFeaturePack()
            .newPackage("p4")
                .writeContent("fp3/p4.txt", "fp3 100 p4")
            .getFeaturePack()
                .newPackage("p5")
                    .writeContent("fp3/p5.txt", "fp3 100 p5");

        creator.install();
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(fp1)
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(fp3.getFPID())
                        .addPackage("p3")
                        .addPackage("p5")
                        .addPackage("p1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(fp2.getFPID())
                        .addPackage("p1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(fp1.getFPID())
                        .addPackage("p1")
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setModel("model1")
                        .setName("name1")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(fp3.getFPID().getProducer(), "specC", "p1", "1")))
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(fp2.getFPID().getProducer(), "specB", "p1", "1")))
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(fp1.getFPID().getProducer(), "specA", "p1", "1")))
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1/p1.txt", "fp1")
                .addFile("fp2/p1.txt", "fp2")
                .addFile("fp3/p1.txt", "fp3 100 p1")
                .addFile("fp3/p3.txt", "fp3 100 p3")
                .addFile("fp3/p5.txt", "fp3 100 p5")
                .build();
    }
}