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

public class Advanced3FeaturePackFamilyTestCase extends LayoutOrderingTestBase {

    private FeaturePackLocation ee10;
    private FeaturePackLocation ee11;
    private FeaturePackLocation cloud;
    private FeaturePackLocation cloudxp;
    private FeaturePackLocation xpee10;
    private FeaturePackLocation xpee11;

    @Override
    protected RepositoryArtifactResolver initRepoManager(Path repoHome) {
        return SimplisticMavenRepoManager.getInstance(repoHome);
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningDescriptionException {
        ee10 = FeaturePackLocation.fromString("org.jboss.galleon.test:ee10:1.0.0.Final");
        FeaturePackBuilder builder1 = creator.newFeaturePack(ee10.getFPID());
        builder1.setFamily(Family.fromString("jboss-eap:jakarta-ee+jakarta-min-ee-10+jakarta-ee10"));

        ee11 = FeaturePackLocation.fromString("org.jboss.galleon.test:ee11:1.0.0.Final");
        FeaturePackBuilder builder2 = creator.newFeaturePack(ee11.getFPID());
        builder2.setFamily(Family.fromString("jboss-eap:jakarta-ee+jakarta-min-ee-10+jakarta-ee11"));

        xpee10 = FeaturePackLocation.fromString("org.jboss.galleon.test:xp-ee10:1.0.0.Final");
        FeaturePackBuilder builder3 = creator.newFeaturePack(xpee10.getFPID());
        builder3.setFamily(Family.fromString("jboss-eap:microprofile+microprofile-7x"));
        builder3.addDependency(FeaturePackConfig.builder(ee10, false).build());

        xpee11 = FeaturePackLocation.fromString("org.jboss.galleon.test:xp-ee11:1.0.0.Final");
        FeaturePackBuilder builder4 = creator.newFeaturePack(xpee11.getFPID());
        builder4.setFamily(Family.fromString("jboss-eap:microprofile+microprofile-8x"));
        builder4.addDependency(FeaturePackConfig.builder(ee11, false).build());

        cloud = FeaturePackLocation.fromString("org.jboss.galleon.test:cloud:1.0.0.Final");
        FeaturePackBuilder builder5 = creator.newFeaturePack(cloud.getFPID());
        builder5.addDependency(FeaturePackConfig.builder(ee10, false, "jboss-eap:jakarta-ee+jakarta-min-ee-10").build());

        cloudxp = FeaturePackLocation.fromString("org.jboss.galleon.test:xpcloud:1.0.0.Final");
        FeaturePackBuilder builder6 = creator.newFeaturePack(cloudxp.getFPID());
        builder6.addDependency(FeaturePackConfig.builder(cloud, false).build());
        builder6.addDependency(FeaturePackConfig.builder(xpee10, false, "jboss-eap:microprofile").build());

    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(cloudxp).build();
    }

    @Override
    protected FPID[] expectedOrder() {
        return new FPID[]{ee10.getFPID(), cloud.getFPID(), xpee10.getFPID(), cloudxp.getFPID()};
    }

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
    }
}
