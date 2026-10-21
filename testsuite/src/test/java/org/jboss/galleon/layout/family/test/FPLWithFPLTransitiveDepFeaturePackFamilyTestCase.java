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

public class FPLWithFPLTransitiveDepFeaturePackFamilyTestCase extends FamilyLayoutOrderingTestBase {

    private FeaturePackLocation grpc;
    private FeaturePackLocation ee;
    private FeaturePackLocation full;
    private FeaturePackLocation ee10;

    @Override
    protected RepositoryArtifactResolver initRepoManager(Path repoHome) {
        return SimplisticMavenRepoManager.getInstance(repoHome);
    }
    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("ee-gal", 2);
        universe.createProducer("full-gal", 2);
        universe.createProducer("grpc-gal", 2);
        universe.createProducer("ee-10-gal", 2);
    }
    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningDescriptionException {
        ee = newFpl("ee-gal", "1", "1.0.0.Final");
        FeaturePackBuilder builder1 = creator.newFeaturePack(ee.getFPID());
        builder1.setFamily(Family.fromString("wildfly:jakarta-ee,jakarta-min-ee-10,jakarta-ee10"));

        full = newFpl("full-gal", "1", "1.0.0.Final");
        FeaturePackBuilder builder2 = creator.newFeaturePack(full.getFPID());
        builder2.addDependency("orig1", FeaturePackConfig.builder(ee, true, "wildfly:jakarta-ee,jakarta-min-ee-10").build());
        builder2.setFamily(Family.fromString("wildfly:microprofile"));

        grpc = newFpl("grpc-gal", "1", "1.0.0.Final");
        FeaturePackBuilder builder3 = creator.newFeaturePack(grpc.getFPID());
        builder3.addDependency("orig1", FeaturePackConfig.builder(ee, true, "wildfly:jakarta-ee,jakarta-min-ee-10").build());
        builder3.addDependency("orig2", FeaturePackConfig.builder(full, false, "wildfly:microprofile").build());

        ee10 = newFpl("ee-10-gal", "1", "1.0.0.Final");
        FeaturePackBuilder builder4 = creator.newFeaturePack(ee10.getFPID());
        builder4.setFamily(Family.fromString("wildfly:jakarta-ee,jakarta-min-ee-10,jakarta-ee10"));
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(ee10)
                .addFeaturePackDep(full)
                .addFeaturePackDep(grpc).build();
    }

    @Override
    protected FPID[] expectedOrder() {
        return new FPID[]{ee10.getFPID(), full.getFPID(),grpc.getFPID()};
    }

}
