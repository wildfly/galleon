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
import java.util.HashSet;
import java.util.Set;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackBuilder;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.layout.LayoutOrderingTestBase;
import org.jboss.galleon.repo.RepositoryArtifactResolver;
import org.jboss.galleon.spec.FeaturePackSpec.Family;
import org.jboss.galleon.spec.FeaturePackSpec.Family.Criteria;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.maven.repo.SimplisticMavenRepoManager;

public class AdvancedFeaturePackMultipleFamilyOverridesTestCase extends LayoutOrderingTestBase {

    private FeaturePackLocation fpl0;
    private FeaturePackLocation fpl0_0;
    private FeaturePackLocation fpl1;
    private FeaturePackLocation fpl2;
    private FeaturePackLocation fpl3;
    private FeaturePackLocation fpl4;

    @Override
    protected RepositoryArtifactResolver initRepoManager(Path repoHome) {
        return SimplisticMavenRepoManager.getInstance(repoHome);
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningDescriptionException {
        fpl0_0 = FeaturePackLocation.fromString("org.jboss.galleon.test:dep-family-fp0_0:1.0.0.Final");
        FeaturePackBuilder builder = creator.newFeaturePack(fpl0_0.getFPID());
        builder.setFamily(Family.fromString("family1:specificity1"));

        fpl0 = FeaturePackLocation.fromString("org.jboss.galleon.test:dep-family-fp0:1.0.0.Final");
        FeaturePackBuilder builder0 = creator.newFeaturePack(fpl0.getFPID());
        Set<Criteria> criteria = new HashSet<>();
        criteria.add(new Criteria("specificity2", false));
        criteria.add(new Criteria("specificity3", false));
        criteria.add(new Criteria("specificity4", false));
        criteria.add(new Criteria("specificity1", true));
        Family f = new Family("family1", criteria);
        builder0.setFamily(f);
        builder0.addDependency(FeaturePackConfig.builder(fpl0_0, false, "family1:specificity1").build());


        fpl1 = FeaturePackLocation.fromString("org.jboss.galleon.test:dep-family-fp1:1.0.0.Final");
        FeaturePackBuilder builder1 = creator.newFeaturePack(fpl1.getFPID());
        builder1.setFamily(Family.fromString("family1:specificity1,specificity2"));

        fpl2 = FeaturePackLocation.fromString("org.jboss.galleon.test:dep-family-fp2:1.0.0.Final");
        FeaturePackBuilder builder2 = creator.newFeaturePack(fpl2.getFPID());
        builder2.setFamily(Family.fromString("family1:specificity1,specificity2,specificity3"));

        fpl3 = FeaturePackLocation.fromString("org.jboss.galleon.test:fp1:1.0.0.Final");
        FeaturePackBuilder builder3 = creator.newFeaturePack(fpl3.getFPID());
        builder3.addDependency(FeaturePackConfig.builder(fpl1, false, "family1:specificity4").build());

        fpl4 = FeaturePackLocation.fromString("org.jboss.galleon.test:fp2:1.0.0.Final");
        FeaturePackBuilder builder4 = creator.newFeaturePack(fpl4.getFPID());
        builder4.addDependency(FeaturePackConfig.builder(fpl2, false, "family1:specificity2,specificity1,specificity3").build());
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(fpl0)
                .addFeaturePackDep(fpl3)
                .addFeaturePackDep(fpl4).build();
    }

    @Override
    protected FPID[] expectedOrder() {
        return new FPID[]{fpl0_0.getFPID(), fpl0.getFPID(), fpl3.getFPID(), fpl4.getFPID()};
    }

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
    }
}
