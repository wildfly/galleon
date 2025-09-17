/*
 * Copyright 2016-2025 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.layout.family.test;

import java.nio.file.Path;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackBuilder;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.layout.LayoutOrderingTestBase;
import org.jboss.galleon.repo.RepositoryArtifactResolver;
import org.jboss.galleon.spec.FeaturePackSpec.Family;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.maven.repo.SimplisticMavenRepoManager;

public class AdvancedFeaturePackFamilyTestCase extends LayoutOrderingTestBase {

    private FeaturePackLocation fpl1;
    private FeaturePackLocation fpl2;
    private FeaturePackLocation fpl3;
    private FeaturePackLocation fpl4;
    private FeaturePackLocation fpl5;
    private FeaturePackLocation fpl6;
    private FeaturePackLocation fpl7;

    @Override
    protected RepositoryArtifactResolver initRepoManager(Path repoHome) {
        return SimplisticMavenRepoManager.getInstance(repoHome);
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningDescriptionException {
        fpl1 = FeaturePackLocation.fromString("org.jboss.galleon.test:dep-family1-fp1:1.0.0.Final");
        FeaturePackBuilder builder1 = creator.newFeaturePack(fpl1.getFPID());
        builder1.setFamily(Family.fromString("family1:specific1"));

        fpl2 = FeaturePackLocation.fromString("org.jboss.galleon.test:dep-family1-fp2:1.0.0.Final");
        FeaturePackBuilder builder2 = creator.newFeaturePack(fpl2.getFPID());
        builder2.setFamily(Family.fromString("family1:specific1"));

        fpl3 = FeaturePackLocation.fromString("org.jboss.galleon.test:dep-family1-fp11:1.0.0.Final");
        FeaturePackBuilder builder3 = creator.newFeaturePack(fpl3.getFPID());
        builder3.setFamily(Family.fromString("family1:specific2"));
        builder3.addDependency(FeaturePackConfig.builder(fpl1, false, "family1").build());

        fpl4 = FeaturePackLocation.fromString("org.jboss.galleon.test:dep-family1-fp22:1.0.0.Final");
        FeaturePackBuilder builder4 = creator.newFeaturePack(fpl4.getFPID());
        builder4.setFamily(Family.fromString("family1:specific2"));
        builder4.addDependency(FeaturePackConfig.builder(fpl2, false, "family1:specific1").build());

        fpl5 = FeaturePackLocation.fromString("org.jboss.galleon.test:fp1:1.0.0.Final");
        FeaturePackBuilder builder5 = creator.newFeaturePack(fpl5.getFPID());
        builder5.addDependency(FeaturePackConfig.builder(fpl3, false, "family1:specific2").build());
        builder5.setFamily(Family.fromString("family1:specific3"));

        fpl6 = FeaturePackLocation.fromString("org.jboss.galleon.test:fp1-2:1.0.0.Final");
        FeaturePackBuilder builder6 = creator.newFeaturePack(fpl6.getFPID());
        builder6.setFamily(Family.fromString("family1:specific3"));
        builder6.addDependency(FeaturePackConfig.builder(fpl4, false, "family1:specific2").build());

        fpl7 = FeaturePackLocation.fromString("org.jboss.galleon.test:fp2:1.0.0.Final");
        FeaturePackBuilder builder7 = creator.newFeaturePack(fpl7.getFPID());
        builder7.addDependency(FeaturePackConfig.builder(fpl5, false, "family1:specific3").build())
                .addTransitiveDependency(fpl3, "family1:specific2")
                .addTransitiveDependency(fpl1, "family1:specific1");
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(fpl6)
                .addFeaturePackDep(fpl7).build();
    }

    @Override
    protected FPID[] expectedOrder() {
        return new FPID[]{fpl2.getFPID(), fpl4.getFPID(), fpl6.getFPID(), fpl7.getFPID()};
    }

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
    }
}
