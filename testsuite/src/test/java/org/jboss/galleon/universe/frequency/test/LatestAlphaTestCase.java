/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
public class LatestAlphaTestCase extends ProvisionConfigMvnTestBase {

    private static final FeaturePackLocation ALPHA1_FPL = FeaturePackLocation.fromString("producer1:1#1.0.0.Alpha1");
    private static final FeaturePackLocation ALPHA2_FPL = FeaturePackLocation.fromString("producer1:1#1.0.0.Alpha2");
    private static final FeaturePackLocation FINAL2_FPL = FeaturePackLocation.fromString("producer1:1#2.0.0.Final");

    private MavenArtifact universe1Art;
    private FPID alpha1Fpid;
    private FPID alpha2Fpid;
    private FPID final2Fpid;

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {

        universe1Art = newMvnUniverse("universe1")
                .createProducer("producer1", "fp1")
                .install();

        alpha1Fpid = mvnFPID(ALPHA1_FPL, universe1Art);
        alpha2Fpid = mvnFPID(ALPHA2_FPL, universe1Art);
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
                .setFPID(final2Fpid)
                .newPackage("p1", true)
                    .writeContent("fp1/p1.txt", "p1 final2")
                    .getFeaturePack();
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .setDefaultUniverse(MavenUniverseFactory.ID, universe1Art.getCoordsAsString())
                .addFeaturePackDep(FeaturePackLocation.fromString("producer1:1/alpha"))
                .build();
    }

    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
        .setDefaultUniverse(MavenUniverseFactory.ID, universe1Art.getCoordsAsString())
        .addFeaturePackDep(FeaturePackLocation.fromString("producer1:1/alpha#1.0.0.Alpha2"))
        .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(alpha2Fpid)
                        .addPackage("p1")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1/p1.txt", "p1 alpha2")
                .build();
    }
}