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
package org.jboss.galleon.universe.frequency.test;

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
public class LatestSnapshotTestCase extends ProvisionConfigMvnTestBase {

    private static final FeaturePackLocation ALPHA1_FPL = FeaturePackLocation.fromString("producer1:1#1.0.0.Alpha1");
    private static final FeaturePackLocation ALPHA2_FPL = FeaturePackLocation.fromString("producer1:1#1.0.0.Alpha2");
    private static final FeaturePackLocation BETA1_FPL = FeaturePackLocation.fromString("producer1:1#1.0.0.Beta1");
    private static final FeaturePackLocation BETA2_FPL = FeaturePackLocation.fromString("producer1:1#1.0.0.Beta2");
    private static final FeaturePackLocation FINAL_FPL = FeaturePackLocation.fromString("producer1:1#1.0.0.Final");
    private static final FeaturePackLocation FINAL1_FPL = FeaturePackLocation.fromString("producer1:1#1.0.1.Final");
    private static final FeaturePackLocation FINAL1_SNAPSHOT_FPL = FeaturePackLocation.fromString("producer1:1#1.0.2.Final-SNAPSHOT");
    private static final FeaturePackLocation FINAL2_FPL = FeaturePackLocation.fromString("producer1:1#2.0.0.Final");

    private MavenArtifact universe1Art;
    private FPID alpha1Fpid;
    private FPID alpha2Fpid;
    private FPID beta1Fpid;
    private FPID beta2Fpid;
    private FPID finalFpid;
    private FPID final1Fpid;
    private FPID final1SnapshotFpid;
    private FPID final2Fpid;

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {

        universe1Art = newMvnUniverse("universe1")
                .createProducer("producer1", "fp1")
                .install();

        alpha1Fpid = mvnFPID(ALPHA1_FPL, universe1Art);
        alpha2Fpid = mvnFPID(ALPHA2_FPL, universe1Art);
        beta1Fpid = mvnFPID(BETA1_FPL, universe1Art);
        beta2Fpid = mvnFPID(BETA2_FPL, universe1Art);
        finalFpid = mvnFPID(FINAL_FPL, universe1Art);
        final1Fpid = mvnFPID(FINAL1_FPL, universe1Art);
        final1SnapshotFpid = mvnFPID(FINAL1_SNAPSHOT_FPL, universe1Art);
        final2Fpid = mvnFPID(FINAL2_FPL, universe1Art);

        creator
        .newFeaturePack()
            .setFPID(alpha1Fpid)
            .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "p1 alpha1")
                .getFeaturePack()
        .getCreator()
        .newFeaturePack()
            .setFPID(alpha2Fpid)
            .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "p1 alpha2")
                .getFeaturePack()
        .getCreator()
        .newFeaturePack()
            .setFPID(beta1Fpid)
            .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "p1 beta1")
                .getFeaturePack()
        .getCreator()
        .newFeaturePack()
            .setFPID(beta2Fpid)
            .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "p1 beta2")
                .getFeaturePack()
        .getCreator()
        .newFeaturePack()
            .setFPID(finalFpid)
            .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "p1 final")
                .getFeaturePack()
        .getCreator()
        .newFeaturePack()
            .setFPID(final1Fpid)
            .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "p1 final 1.0.1")
                .getFeaturePack()
        .getCreator()
        .newFeaturePack()
            .setFPID(final1SnapshotFpid)
            .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "p1 final 1.0.2 snapshot")
                .getFeaturePack()
        .getCreator()
        .newFeaturePack()
                .setFPID(final2Fpid)
                .newPackage("p1", true)
                    .writeContent("fp1/p1.txt", "p1 final2")
                    .getFeaturePack()
        .getCreator()
        .install();
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .setDefaultUniverse(MavenUniverseFactory.ID, universe1Art.getCoordsAsString())
                .addFeaturePackDep(FeaturePackLocation.fromString("producer1:1/snapshot"))
                .build();
    }

    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
        .setDefaultUniverse(MavenUniverseFactory.ID, universe1Art.getCoordsAsString())
        .addFeaturePackDep(FeaturePackLocation.fromString("producer1:1/snapshot#1.0.2.Final-SNAPSHOT"))
        .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(final1SnapshotFpid)
                        .addPackage("p1")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1/p1.txt", "p1 final 1.0.2 snapshot")
                .build();
    }
}