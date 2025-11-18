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
import org.jboss.galleon.repo.RepositoryArtifactResolver;
import org.jboss.galleon.spec.FeaturePackSpec.Family;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.ProvisionFromUniverseTestBase;
import org.jboss.galleon.universe.maven.repo.SimplisticMavenRepoManager;

public class SquashTransitiveDepFeaturePackPackagesFamilyTestCase extends ProvisionFromUniverseTestBase {

    private FeaturePackLocation grpc;
    private FeaturePackLocation ee;
    private FeaturePackLocation full;
    private FeaturePackLocation preview;

    @Override
    protected RepositoryArtifactResolver initRepoManager(Path repoHome) {
        return SimplisticMavenRepoManager.getInstance(repoHome);
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningDescriptionException {
        ee = FeaturePackLocation.fromString("org.jboss.galleon.test:ee:1.0.0.Final");
        FeaturePackBuilder builder1 = creator.newFeaturePack(ee.getFPID());
        builder1.setFamily(Family.fromString("wildfly:jakarta-ee+jakarta-min-ee-10+jakarta-ee10"));
        builder1.newPackage("p2");

        full = FeaturePackLocation.fromString("org.jboss.galleon.test:full:1.0.0.Final");
        FeaturePackBuilder builder2 = creator.newFeaturePack(full.getFPID());
        builder2.setFamily(Family.fromString("wildfly:microprofile"));
        builder2.newPackage("p3");

        grpc = FeaturePackLocation.fromString("org.jboss.galleon.test:grpc:1.0.0.Final");
        FeaturePackBuilder builder3 = creator.newFeaturePack(grpc.getFPID());
        builder3.addDependency("orig1", FeaturePackConfig.transitiveBuilder(ee).setAllowedFamily("wildfly:jakarta-ee+jakarta-min-ee-10").build());
        builder3.addDependency("orig2", FeaturePackConfig.builder(full, false, "wildfly:microprofile").build());
        builder3.newPackage("mypackage", true).addDependency("orig1", "p2").addDependency("orig2", "p3");
        preview = FeaturePackLocation.fromString("org.jboss.galleon.test:preview:1.0.0.Final");
        FeaturePackBuilder builder4 = creator.newFeaturePack(preview.getFPID());
        builder4.setFamily(Family.fromString("wildfly:jakarta-ee+jakarta-min-ee-10+jakarta-ee10+microprofile"));
        builder4.newPackage("p2");
        builder4.newPackage("p3");
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(preview)
                .addFeaturePackDep(FeaturePackConfig.builder(grpc).setInheritPackages(true).build()).build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(preview.getFPID())
                        .addPackage("p2")
                        .addPackage("p3")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(grpc.getFPID())
                        .addPackage("mypackage")
                        .build())
                .build();
    }

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
    }
}
