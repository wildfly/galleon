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

import org.jboss.galleon.Errors;
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
 * @author Alexey Loubyansky
 */
public class BetaAsDefaultFrequencyWithAlphaReleasesTestCase extends ProvisionConfigMvnTestBase {

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
                .createProducer("producer1", "fp1", "beta")
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
                    .getFeaturePack()
        .getCreator()
        .install();
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .setDefaultUniverse(MavenUniverseFactory.ID, universe1Art.getCoordsAsString())
                .addFeaturePackDep(FeaturePackLocation.fromString("producer1:1"))
                .build();
    }

    @Override
    protected String[] pmErrors() {
        return new String[] {
                Errors.noVersionAvailable(FeaturePackLocation.fromString("producer1@maven(org.jboss.galleon.universe.test:universe1:jar:1.0.0.Final):1/beta"))
                };
    }
}