/*
 * Copyright ${license.git.copyrightYears} Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.installation.fpversions;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ArtifactCoords.Gav;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.repomanager.FeaturePackRepositoryManager;
import org.jboss.galleon.spec.PackageDependencySpec;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedPackage;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.PmProvisionConfigTestBase;
import org.jboss.galleon.test.util.fs.state.DirState;

/**
 *
 * @author Alexey Loubyansky
 */
public class FpDepVersionConflictInFpSpecResolvedTestCase extends PmProvisionConfigTestBase {

    private static final Gav FP9_100_GAV = ArtifactCoords.newGav("org.jboss.pm.test", "fp9", "1.0.0.Final");
    private static final Gav FP1_100_GAV = ArtifactCoords.newGav("org.jboss.pm.test", "fp1", "1.0.0.Final");
    private static final Gav FP1_101_GAV = ArtifactCoords.newGav("org.jboss.pm.test", "fp1", "1.0.1.Final");
    private static final Gav FP2_200_GAV = ArtifactCoords.newGav("org.jboss.pm.test", "fp2", "2.0.0.Final");
    private static final Gav FP3_100_GAV = ArtifactCoords.newGav("org.jboss.pm.test", "fp3", "1.0.0.Final");

    @Override
    protected void setupRepo(FeaturePackRepositoryManager repoManager) throws ProvisioningDescriptionException {
        repoManager.installer()
            .newFeaturePack(FP1_100_GAV)
                .newPackage("p1", true)
                    .addDependency(PackageDependencySpec.forPackage("p2", true))
                    .writeContent("fp1/p1.txt", "fp1 1.0.0.Final p1")
                    .getFeaturePack()
                .newPackage("p2")
                    .writeContent("fp1/p2.txt", "fp1 1.0.0.Final p2")
                    .getFeaturePack()
                .newPackage("p3")
                    .writeContent("fp1/p3.txt", "fp1 1.0.0.Final p3")
                    .getFeaturePack()
                .getInstaller()
            .newFeaturePack(FP1_101_GAV)
                .newPackage("p1", true)
                    .addDependency(PackageDependencySpec.forPackage("p2", true))
                    .writeContent("fp1/p1.txt", "fp1 1.0.1.Final p1")
                    .getFeaturePack()
                .newPackage("p2")
                    .writeContent("fp1/p2.txt", "fp1 1.0.1.Final p2")
                    .getFeaturePack()
                .newPackage("p3")
                    .writeContent("fp1/p3.txt", "fp1 1.0.1.Final p3")
                    .getFeaturePack()
                .getInstaller()
            .newFeaturePack(FP2_200_GAV)
                .addDependency(FP1_100_GAV)
                .newPackage("p1", true)
                    .writeContent("fp2/p1.txt", "fp2 p1")
                    .getFeaturePack()
                .getInstaller()
            .newFeaturePack(FP3_100_GAV)
                .addDependency(FP1_101_GAV)
                .newPackage("p1", true)
                    .writeContent("fp3/p1.txt", "fp3 p1")
                    .getFeaturePack()
                .getInstaller()
            .newFeaturePack(FP9_100_GAV)
                .addDependency(FeaturePackConfig.forGav(FP1_101_GAV))
                .addDependency(FeaturePackConfig.forGav(FP2_200_GAV))
                .addDependency(FeaturePackConfig.forGav(FP3_100_GAV))
                .newPackage("p1", true)
                    .writeContent("fp9/p1.txt", "fp9 p1")
                    .getFeaturePack()
                .getInstaller()
            .install();
    }

    @Override
    protected ProvisioningConfig provisioningConfig()
            throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.forGav(FP9_100_GAV))
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningDescriptionException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(FP1_101_GAV)
                        .addPackage(ProvisionedPackage.newInstance("p1"))
                        .addPackage(ProvisionedPackage.newInstance("p2"))
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(FP2_200_GAV)
                        .addPackage(ProvisionedPackage.newInstance("p1"))
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(FP3_100_GAV)
                        .addPackage(ProvisionedPackage.newInstance("p1"))
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(FP9_100_GAV)
                        .addPackage(ProvisionedPackage.newInstance("p1"))
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1/p1.txt", "fp1 1.0.1.Final p1")
                .addFile("fp1/p2.txt", "fp1 1.0.1.Final p2")
                .addFile("fp2/p1.txt", "fp2 p1")
                .addFile("fp3/p1.txt", "fp3 p1")
                .addFile("fp9/p1.txt", "fp9 p1")
                .build();
    }
}
