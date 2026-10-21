/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.universe.channel.filter.test;

import org.jboss.galleon.BaseErrors;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.ProvisionConfigMvnTestBase;
import org.jboss.galleon.universe.maven.MavenArtifact;
import org.jboss.galleon.universe.maven.MavenUniverseFactory;

/**
 *
 * @author Brian Stansberry
 */
public class ChannelIncludeMatchFailTestCase extends ProvisionConfigMvnTestBase {

    private static final String ALT_REGEX = ".*-alt-[0-9][0-9][0-9][0-9][0-9]";
    private static final FeaturePackLocation EXT_FPL = FeaturePackLocation.fromString("producer1:1#1.1.1.Final-ext-00001");
    private static final FeaturePackLocation EXT2_FPL = FeaturePackLocation.fromString("producer1:1#1.1.1.Final-ext-00002");

    private MavenArtifact universe1Art;
    private FPID extFpid;
    private FPID ext2Fpid;

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {

        universe1Art = newMvnUniverse("universe1")
                .createProducer("producer1", "fp1", "final", ALT_REGEX, null)
                .install();

        extFpid = mvnFPID(EXT_FPL, universe1Art);
        ext2Fpid = mvnFPID(EXT2_FPL, universe1Art);

        creator.newFeaturePack()
            .setFPID(extFpid)
            .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "p1 ext");

        creator.newFeaturePack()
            .setFPID(ext2Fpid)
            .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "p1 ext2");

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
        .addFeaturePackDep(FeaturePackLocation.fromString("producer1:1#1.1.0.Final"))
        .build();
    }


    @Override
    protected String[] pmErrors() {
        return new String[] {
                BaseErrors.noVersionAvailable(FeaturePackLocation.fromString("producer1@maven(org.jboss.galleon.universe.test:universe1:jar:1.0.0.Final):1/final"))
        };
    }
}