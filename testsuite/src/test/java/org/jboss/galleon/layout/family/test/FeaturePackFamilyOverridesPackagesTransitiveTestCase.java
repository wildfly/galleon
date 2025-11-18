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
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackBuilder;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.repo.RepositoryArtifactResolver;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.spec.FeaturePackSpec.Family;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.spec.PackageDependencySpec;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.ProvisionFromUniverseTestBase;
import org.jboss.galleon.universe.maven.repo.SimplisticMavenRepoManager;
import org.jboss.galleon.xml.ProvisionedConfigBuilder;
import org.jboss.galleon.xml.ProvisionedFeatureBuilder;

public class FeaturePackFamilyOverridesPackagesTransitiveTestCase extends ProvisionFromUniverseTestBase {

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
                .addFeatureSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("id"))
                    .addPackageDep(PackageDependencySpec.required("p2"))
                    .addPackageDep(PackageDependencySpec.optional("p3")).build())
                .newPackage("p1", true).getFeaturePack()
                .newPackage("p2").getFeaturePack()
                .newPackage("p3")
                .addDependency("p4")
                .addDependency("p5")
                .getFeaturePack()
                .newPackage("p4").getFeaturePack()
                .newPackage("p5").getFeaturePack();
        builder1.setFamily(Family.fromString("family1:specific1"));

        fpl2 = FeaturePackLocation.fromString("org.jboss.galleon.test:dep-family-fp2:1.0.0.Final");
        FeaturePackBuilder builder2 = creator.newFeaturePack(fpl2.getFPID())
                .addFeatureSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("id"))
                    .addPackageDep(PackageDependencySpec.required("p2"))
                    .addPackageDep(PackageDependencySpec.optional("p3")).build())
                .newPackage("p1_2", true).getFeaturePack()
                .newPackage("p2").getFeaturePack()
                .newPackage("p3")
                .addDependency("p4_2")
                .addDependency("p5_2")
                .getFeaturePack()
                .newPackage("p4_2").getFeaturePack()
                .newPackage("p5_2").getFeaturePack();
        builder2.setFamily(Family.fromString("family1:specific1"));

        fpl3 = FeaturePackLocation.fromString("org.jboss.galleon.test:fp1:1.0.0.Final");
        FeaturePackBuilder builder3 = creator.newFeaturePack(fpl3.getFPID());
        builder3.addDependency("foo-origin", FeaturePackConfig.transitiveBuilder(fpl1).setAllowedFamily("family1:specific1").build());
        builder3.newPackage("mypackage", true).addDependency("foo-origin", "p2");
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(fpl2).setInheritPackages(true).build())
                .addFeaturePackDep(fpl3)
                .addConfig(ConfigModel.builder("model1", "name1")
                        .addFeature(new FeatureConfig("specA").setParam("id", "1"))
                        .build()).build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(fpl2.getFPID())
                        .addPackage("p1_2")
                        .addPackage("p2")
                        .addPackage("p3")
                        .addPackage("p4_2")
                        .addPackage("p5_2")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(fpl3.getFPID())
                        .addPackage("mypackage")
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setModel("model1")
                        .setName("name1")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(fpl2.getProducer(), "specA", "id", "1")))
                        .build())
                .build();
    }

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
    }
}
