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
public class ChannelExcludeMatchTestCase extends ProvisionConfigMvnTestBase {

    private static final String EXT_REGEX = ".*-ext-[0-9][0-9][0-9][0-9][0-9]";
    private static final FeaturePackLocation STD_FPL = FeaturePackLocation.fromString("producer1:1#1.1.0.Final");
    private static final FeaturePackLocation EXT_FPL = FeaturePackLocation.fromString("producer1:1#1.1.1.Final-ext-00001");

    private MavenArtifact universe1Art;
    private FPID stdFpid;
    private FPID extFpid;

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {

        universe1Art = newMvnUniverse("universe1")
                .createProducer("producer1", "fp1", "final", null, EXT_REGEX)
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
        .addFeaturePackDep(FeaturePackLocation.fromString("producer1:1#1.1.0.Final"))
        .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(stdFpid)
                        .addPackage("p1")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1/p1.txt", "p1 std")
                .build();
    }
}