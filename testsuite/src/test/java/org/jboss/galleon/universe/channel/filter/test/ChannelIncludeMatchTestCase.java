/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.universe.channel.filter.test;

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
 * @author Brian Stansberry
 */
public class ChannelIncludeMatchTestCase extends ProvisionConfigMvnTestBase {

    private static final String EXT_REGEX = ".*-ext-[0-9][0-9][0-9][0-9][0-9]";
    private static final FeaturePackLocation STD_FPL = FeaturePackLocation.fromString("producer1:1#1.1.1.Final");
    private static final FeaturePackLocation EXT_FPL = FeaturePackLocation.fromString("producer1:1#1.1.0.Final-ext-00001");

    private MavenArtifact universe1Art;
    private FPID stdFpid;
    private FPID extFpid;

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {

        universe1Art = newMvnUniverse("universe1")
                .createProducer("producer1", "fp1", "final", EXT_REGEX, null)
                .install();

        stdFpid = mvnFPID(STD_FPL, universe1Art);
        extFpid = mvnFPID(EXT_FPL, universe1Art);

        creator.newFeaturePack()
            .setFPID(stdFpid)
            .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "p1 std");

        creator.newFeaturePack()
            .setFPID(extFpid)
            .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "p1 ext");

    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .setDefaultUniverse(MavenUniverseFactory.ID, universe1Art.getCoordsAsString())
                .addFeaturePackDep(FeaturePackLocation.fromString("producer1:1"))
                .build();
    }

    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
        .setDefaultUniverse(MavenUniverseFactory.ID, universe1Art.getCoordsAsString())
        .addFeaturePackDep(FeaturePackLocation.fromString("producer1:1#1.1.0.Final-ext-00001"))
        .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(extFpid)
                        .addPackage("p1")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1/p1.txt", "p1 ext")
                .build();
    }
}