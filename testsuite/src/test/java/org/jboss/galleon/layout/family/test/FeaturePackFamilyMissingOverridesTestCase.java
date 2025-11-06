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

public class FeaturePackFamilyMissingOverridesTestCase extends LayoutOrderingTestBase {

    private FeaturePackLocation fpl1;
    private FeaturePackLocation fpl2;
    private FeaturePackLocation fpl3;

    @Override
    protected RepositoryArtifactResolver initRepoManager(Path repoHome) {
        return SimplisticMavenRepoManager.getInstance(repoHome);
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningDescriptionException {
        fpl1 = FeaturePackLocation.fromString("org.jboss.galleon.test:dep-family-fp1:1.0.0.Final");
        FeaturePackBuilder builder1 = creator.newFeaturePack(fpl1.getFPID());
        builder1.setFamily(Family.fromString("family1:specific1"));

        fpl2 = FeaturePackLocation.fromString("org.jboss.galleon.test:dep-family-fp2:1.0.0.Final");
        FeaturePackBuilder builder2 = creator.newFeaturePack(fpl2.getFPID());
        builder2.setFamily(Family.fromString("family1:specific1"));

        fpl3 = FeaturePackLocation.fromString("org.jboss.galleon.test:fp1:1.0.0.Final");
        FeaturePackBuilder builder3 = creator.newFeaturePack(fpl3.getFPID());
        builder3.addDependency(FeaturePackConfig.builder(fpl1).build());
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(fpl2)
                .addFeaturePackDep(fpl3).build();
    }

    @Override
    protected String[] pmErrors() throws ProvisioningException {
        String[] errors = {"The feature-pack org.jboss.galleon.test:fp1:1.0.0.Final expects the dependency on org.jboss.galleon.test:dep-family-fp1:1.0.0.Final but this dependency is in the family family1:specific1 for which a different member org.jboss.galleon.test:dep-family-fp2:1.0.0.Final is in use."};
        return errors;
    }

    @Override
    protected FPID[] expectedOrder() {
        return null;
    }

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
    }
}
