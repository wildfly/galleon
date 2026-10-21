/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.universe.frequency.test;

import java.util.Arrays;

import org.jboss.galleon.BaseErrors;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.ProvisionConfigMvnTestBase;
import org.jboss.galleon.universe.maven.MavenArtifact;
import org.jboss.galleon.universe.maven.MavenUniverseFactory;

/**
 *
 * @author Alexey Loubyansky
 */
public class UnsupportedFrequencyTestCase extends ProvisionConfigMvnTestBase {

    private static final FeaturePackLocation ALPHA1_FPL = FeaturePackLocation.fromString("producer1:1#1.0.0.Alpha1");

    private MavenArtifact universe1Art;
    private FPID alpha1Fpid;

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {

        universe1Art = newMvnUniverse("universe1")
                .createProducer("producer1", "fp1")
                .install();

        alpha1Fpid = mvnFPID(ALPHA1_FPL, universe1Art);

        creator
        .newFeaturePack()
            .setFPID(alpha1Fpid)
            .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "p1 alpha1")
                .getFeaturePack();
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .setDefaultUniverse(MavenUniverseFactory.ID, universe1Art.getCoordsAsString())
                .addFeaturePackDep(FeaturePackLocation.fromString("producer1:1/zeta"))
                .build();
    }

    @Override
    protected String[] pmErrors() throws ProvisioningException {
        return new String[] {
                BaseErrors.frequencyNotSupported(Arrays.asList(MvnUniverse.frequencies),
                        FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + "(" + universe1Art.getCoordsAsString() + "):1/zeta"))
                };
    }
}