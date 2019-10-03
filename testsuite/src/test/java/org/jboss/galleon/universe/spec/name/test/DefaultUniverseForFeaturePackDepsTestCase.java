/*
 * Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.universe.spec.name.test;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.util.fs.state.DirState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.ProvisionConfigMvnTestBase;
import org.jboss.galleon.universe.maven.MavenArtifact;
import org.jboss.galleon.universe.maven.MavenUniverseFactory;

/**
 *
 * @author Alexey Loubyansky
 */
public class DefaultUniverseForFeaturePackDepsTestCase extends ProvisionConfigMvnTestBase {

    private static final FeaturePackLocation FP1_FPL = FeaturePackLocation.fromString("producer1@universe1:1#1.0.0.Final");
    private static final FeaturePackLocation FP2_FPL = FeaturePackLocation.fromString("producer2:1#1.0.0.Final");
    private static final FeaturePackLocation FP3_FPL = FeaturePackLocation.fromString("producer3:1#1.0.0.Final");

    private MavenArtifact universe1Art;
    private FPID fp1Fpid;
    private FPID fp2Fpid;
    private FPID fp3Fpid;

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {

        universe1Art = newMvnUniverse("universe1")
                .createProducer("producer1", "fp1")
                .createProducer("producer2", "fp1")
                .createProducer("producer3", "fp1")
                .install();

        fp1Fpid = mvnFPID(FP1_FPL, universe1Art);
        fp2Fpid = mvnFPID(FP2_FPL, universe1Art);
        fp3Fpid = mvnFPID(FP3_FPL, universe1Art);

        creator
        .newFeaturePack()
            .setFPID(fp1Fpid)
            .addDependency(FP2_FPL)
            .addDependency(FP3_FPL)
            .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "fp1 p1")
                .getFeaturePack()
        .getCreator()
        .newFeaturePack()
            .setFPID(fp2Fpid)
            .newPackage("p1", true)
                .writeContent("fp2/p1.txt", "fp2 p1")
                .getFeaturePack()
        .getCreator()
        .newFeaturePack()
            .setFPID(fp3Fpid)
            .newPackage("p1", true)
                .writeContent("fp3/p1.txt", "fp3 p1")
                .getFeaturePack();
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addUniverse("universe1", MavenUniverseFactory.ID, universe1Art.getCoordsAsString())
                .addFeaturePackDep(FP1_FPL)
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(fp2Fpid)
                        .addPackage("p1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(fp3Fpid)
                        .addPackage("p1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(fp1Fpid)
                        .addPackage("p1")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1/p1.txt", "fp1 p1")
                .addFile("fp2/p1.txt", "fp2 p1")
                .addFile("fp3/p1.txt", "fp3 p1")
                .build();
    }
}