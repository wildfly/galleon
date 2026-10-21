/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.layout.family.test;

import java.nio.file.Path;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackBuilder;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.layout.FamilyLayoutOrderingTestBase;
import org.jboss.galleon.repo.RepositoryArtifactResolver;
import org.jboss.galleon.spec.FeaturePackSpec.Family;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.maven.repo.SimplisticMavenRepoManager;

public class BasicFeaturePackNoFamilyTestCase extends FamilyLayoutOrderingTestBase {

    private FeaturePackLocation fpl1;
    private FeaturePackLocation fpl2;

    @Override
    protected RepositoryArtifactResolver initRepoManager(Path repoHome) {
        return SimplisticMavenRepoManager.getInstance(repoHome);
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningDescriptionException {
        fpl1 = FeaturePackLocation.fromString("org.jboss.galleon.test:dep-family-fp1:1.0.0.Final");
        FeaturePackBuilder builder1 = creator.newFeaturePack(fpl1.getFPID());
        builder1.setFamily(Family.fromString("family1:specific1"));

        fpl2 = FeaturePackLocation.fromString("org.jboss.galleon.test:fp1:1.0.0.Final");
        FeaturePackBuilder builder3 = creator.newFeaturePack(fpl2.getFPID());
        // Depends on an exact member of the family, doesn't allow for other members.
        builder3.addDependency(FeaturePackConfig.builder(fpl1).build());
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(fpl2).build();
    }

    @Override
    protected FPID[] expectedOrder() {
        return new FPID[]{fpl1.getFPID(), fpl2.getFPID()};
    }

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
    }
}
