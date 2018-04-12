/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.featurepack.dependency.override.test;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.repomanager.FeaturePackRepositoryManager;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.PmProvisionConfigTestBase;
import org.jboss.galleon.test.util.fs.state.DirState;

/**
 *
 * @author Alexey Loubyansky
 */
public class ExplicitExcludeFromDependenciesTestCase extends PmProvisionConfigTestBase {

    @Override
    protected void setupRepo(FeaturePackRepositoryManager repoManager) throws ProvisioningDescriptionException {
        repoManager.installer()
            .newFeaturePack(ArtifactCoords.newGav("org.jboss.pm.test", "fp1", "1.0.0.Alpha-SNAPSHOT"))
                .addDependency(FeaturePackConfig
                        .builder(ArtifactCoords.newGav("org.jboss.pm.test", "fp2", "2.0.0.Final"))
                        .excludePackage("b")
                        .build())
                .addDependency(FeaturePackConfig
                        .builder(ArtifactCoords.newGav("org.jboss.pm.test", "fp3", "2.0.0.Final"))
                        .excludePackage("c")
                        .build())
                .newPackage("d", true)
                    .addDependency("e")
                    .writeContent("fp1/d.txt", "d")
                    .getFeaturePack()
                .newPackage("e")
                    .writeContent("fp1/e.txt", "e")
                    .getFeaturePack()
                .getInstaller()
            .newFeaturePack(ArtifactCoords.newGav("org.jboss.pm.test", "fp2", "2.0.0.Final"))
                .addDependency(FeaturePackConfig
                        .builder(ArtifactCoords.newGav("org.jboss.pm.test", "fp3", "2.0.0.Final"))
                        .excludePackage("a")
                        .build())
                .newPackage("a", true)
                    .addDependency("b", true)
                    .addDependency("c")
                    .writeContent("fp2/a.txt", "a")
                    .getFeaturePack()
                .newPackage("b")
                    .addDependency("b1")
                    .writeContent("fp2/b.txt", "b")
                    .getFeaturePack()
                .newPackage("b1")
                    .writeContent("fp2/b1.txt", "b1")
                    .getFeaturePack()
                .newPackage("c")
                    .addDependency("c1")
                    .writeContent("fp2/c.txt", "c")
                    .getFeaturePack()
                .newPackage("c1")
                    .writeContent("fp2/c1.txt", "c1")
                    .getFeaturePack()
                .getInstaller()
            .newFeaturePack(ArtifactCoords.newGav("org.jboss.pm.test", "fp3", "2.0.0.Final"))
                .newPackage("a", true)
                    .writeContent("fp3/a.txt", "a")
                    .getFeaturePack()
                .newPackage("b", true)
                    .addDependency("b1")
                    .writeContent("fp3/b.txt", "b")
                    .getFeaturePack()
                .newPackage("b1")
                    .writeContent("fp3/b1.txt", "b1")
                    .getFeaturePack()
                .newPackage("c", true)
                    .addDependency("c1")
                    .writeContent("fp3/c.txt", "c")
                    .getFeaturePack()
                .newPackage("c1")
                    .writeContent("fp3/c1.txt", "c1")
                    .getFeaturePack()
                .getInstaller()
            .install();
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(
                        FeaturePackConfig.forGav(
                                ArtifactCoords.newGav("org.jboss.pm.test", "fp1", "1.0.0.Alpha-SNAPSHOT")))
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(ArtifactCoords.newGav("org.jboss.pm.test", "fp1", "1.0.0.Alpha-SNAPSHOT"))
                        .addPackage("d")
                        .addPackage("e")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(ArtifactCoords.newGav("org.jboss.pm.test", "fp2", "2.0.0.Final"))
                        .addPackage("a")
                        .addPackage("c")
                        .addPackage("c1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(ArtifactCoords.newGav("org.jboss.pm.test", "fp3", "2.0.0.Final"))
                        .addPackage("a")
                        .addPackage("b")
                        .addPackage("b1")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1/d.txt", "d")
                .addFile("fp1/e.txt", "e")
                .addFile("fp2/a.txt", "a")
                .addFile("fp2/c.txt", "c")
                .addFile("fp2/c1.txt", "c1")
                .addFile("fp3/a.txt", "a")
                .addFile("fp3/b.txt", "b")
                .addFile("fp3/b1.txt", "b1")
                .build();
    }
}
