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

public class NotMavenFPLSimpleFamilyTest extends FamilyLayoutOrderingTestBase {

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
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("ee10-gal", 2);
        universe.createProducer("ee11-gal", 2);
        universe.createProducer("cloud-gal", 2);
    }
    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningDescriptionException {
        ee10 = newFpl("ee10-gal", "1", "1.0.0.Final");
        FeaturePackBuilder builder1 = creator.newFeaturePack(ee10.getFPID());
        builder1.setFamily(Family.fromString("wildfly:jakarta-ee,jakarta-min-ee-10,jakarta-ee10"));

        ee11 = newFpl("ee11-gal", "1", "1.0.0.Final");
        FeaturePackBuilder builder2 = creator.newFeaturePack(ee11.getFPID());
        builder2.setFamily(Family.fromString("wildfly:jakarta-ee,jakarta-min-ee-10,jakarta-ee11"));

        cloud = newFpl("cloud-gal", "1", "1.0.0.Final");
        FeaturePackBuilder builder5 = creator.newFeaturePack(cloud.getFPID());
        builder5.addDependency(FeaturePackConfig.builder(toMavenCoordsFpl(ee10), false, "wildfly:jakarta-ee,jakarta-min-ee-10").build());

    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(ee11)
                .addFeaturePackDep(cloud).build();
    }

    @Override
    protected FPID[] expectedOrder() {
        return new FPID[]{ee11.getFPID(), cloud.getFPID()};
    }

}
