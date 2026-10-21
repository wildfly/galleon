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

public class FeaturePackFamilyFailingDifferentFamiliesTestCase extends FamilyLayoutOrderingTestBase {

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
        builder1.setFamily(Family.fromString("family2:specific1"));

        fpl2 = FeaturePackLocation.fromString("org.jboss.galleon.test:dep-family-fp2:1.0.0.Final");
        FeaturePackBuilder builder2 = creator.newFeaturePack(fpl2.getFPID());
        builder2.setFamily(Family.fromString("family1:specific1,specific2"));

        fpl3 = FeaturePackLocation.fromString("org.jboss.galleon.test:fp1:1.0.0.Final");
        FeaturePackBuilder builder3 = creator.newFeaturePack(fpl3.getFPID());
        builder3.addDependency(FeaturePackConfig.builder(fpl2, false, "family1:specific2").build());
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(fpl1)
                .addFeaturePackDep(fpl3).build();
    }

    @Override
    protected String[] pmErrors() throws ProvisioningException {
        String[] errors = {"Some errors have been encountered processing feature-pack families.\n"+
                           "Different families are mixed in the provisioning configuration: [family1, family2]\n"+
                           "Check the config and remove the conflicting feature-packs."};
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
