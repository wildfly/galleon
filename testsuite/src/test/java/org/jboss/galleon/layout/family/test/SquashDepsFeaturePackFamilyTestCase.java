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

public class SquashDepsFeaturePackFamilyTestCase extends LayoutOrderingTestBase {

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
        builder1.setFamily(Family.fromString("jboss-eap:jakarta-ee+jakarta-min-ee-10+jakarta-ee10"));

        full = FeaturePackLocation.fromString("org.jboss.galleon.test:full:1.0.0.Final");
        FeaturePackBuilder builder2 = creator.newFeaturePack(full.getFPID());
        builder2.setFamily(Family.fromString("jboss-eap:microprofile"));

        grpc = FeaturePackLocation.fromString("org.jboss.galleon.test:grpc:1.0.0.Final");
        FeaturePackBuilder builder3 = creator.newFeaturePack(grpc.getFPID());
        builder3.addDependency("orig1", FeaturePackConfig.builder(ee, false, "jboss-eap:jakarta-ee+jakarta-min-ee-10").build());
        builder3.addDependency("orig2", FeaturePackConfig.builder(full, false, "jboss-eap:microprofile").build());

        preview = FeaturePackLocation.fromString("org.jboss.galleon.test:preview:1.0.0.Final");
        FeaturePackBuilder builder4 = creator.newFeaturePack(preview.getFPID());
        builder4.setFamily(Family.fromString("jboss-eap:jakarta-ee+jakarta-min-ee-10+jakarta-ee10+microprofile"));
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(preview)
                .addFeaturePackDep(grpc).build();
    }

    @Override
    protected FPID[] expectedOrder() {
        return new FPID[]{preview.getFPID(), grpc.getFPID()};
    }

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
    }
}
