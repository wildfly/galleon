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

package org.jboss.galleon.universe;

import java.nio.file.Path;

import org.jboss.galleon.model.GaecRange;
import org.jboss.galleon.model.Gaecvp;
import org.jboss.galleon.repo.RepositoryArtifactResolver;
import org.jboss.galleon.test.PmProvisionConfigTestBase;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.maven.MavenUniverseFactory;
import org.jboss.galleon.universe.maven.repo.MavenRepoManager;
import org.jboss.galleon.universe.maven.repo.SimplisticMavenRepoManager;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class ProvisionConfigMvnTestBase extends PmProvisionConfigTestBase {

    @Override
    protected RepositoryArtifactResolver initRepoManager(Path repoHome) {
        return SimplisticMavenRepoManager.getInstance(repoHome);
    }

    protected MvnUniverse newMvnUniverse(String name) {
        return MvnUniverse.getInstance(name, (MavenRepoManager) repo);
    }

    protected FPID mvnFPID(FeaturePackLocation fpl, Gaecvp universeArtifact) {
        return new FeaturePackLocation(
                universeArtifact == null ? null
                        : new UniverseSpec(MavenUniverseFactory.ID, universeArtifact.getGaecv().toGaecRange()),
                fpl.getProducerName(), fpl.getChannelName(), fpl.getFrequency(), fpl.getBuild()).getFPID();
    }

    protected FPID mvnFPID(FeaturePackLocation fpl, GaecRange universeArtifact) {
        return new FeaturePackLocation(
                universeArtifact == null ? null
                        : new UniverseSpec(MavenUniverseFactory.ID, universeArtifact),
                fpl.getProducerName(), fpl.getChannelName(), fpl.getFrequency(), fpl.getBuild()).getFPID();
    }
}
