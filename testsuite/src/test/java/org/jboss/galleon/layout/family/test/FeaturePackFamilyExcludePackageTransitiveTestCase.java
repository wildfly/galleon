/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.layout.family.test;

import java.nio.file.Path;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
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
import org.jboss.galleon.xml.ProvisionedConfigBuilder;

public class FeaturePackFamilyExcludePackageTransitiveTestCase extends ProvisionFromUniverseTestBase {

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
        FeaturePackBuilder builder1 = creator.newFeaturePack(fpl1.getFPID())
                .newPackage("p3", true)
                .addDependency("p4", true)
                .getFeaturePack().
                newPackage("p4").getFeaturePack();
        builder1.setFamily(Family.fromString("family1:specific1"));

        fpl2 = FeaturePackLocation.fromString("org.jboss.galleon.test:dep-family-fp2:1.0.0.Final");
        FeaturePackBuilder builder2 = creator.newFeaturePack(fpl2.getFPID())
                .newPackage("p3", true)
                .addDependency("p4", true)
                .getFeaturePack().
                newPackage("p4").getFeaturePack().
                newPackage("p5").getFeaturePack();
        builder2.setFamily(Family.fromString("family1:specific1"));

        fpl3 = FeaturePackLocation.fromString("org.jboss.galleon.test:fp1:1.0.0.Final");
        FeaturePackBuilder builder3 = creator.newFeaturePack(fpl3.getFPID());
        builder3.addDependency("foo-origin", FeaturePackConfig.builder(toMavenCoordsFpl(fpl1)).setAllowedFamily("family1:specific1").
                setInheritPackages(false).excludePackage("p4").includePackage("p5").build());
        builder3.newPackage("local", true)
                .addDependency("p4")
                .getFeaturePack().
                newPackage("p4").getFeaturePack();
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.transitiveBuilder(fpl2).build())
                .addFeaturePackDep(FeaturePackConfig.builder(fpl3).build())
                .addConfig(ConfigModel.builder("model1", "name1").build()).build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(fpl2.getFPID())
                        .addPackage("p5")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(fpl3.getFPID())
                        .addPackage("local")
                        .addPackage("p4")
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setModel("model1")
                        .setName("name1")
                        .build())
                .build();
    }

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
    }
}
