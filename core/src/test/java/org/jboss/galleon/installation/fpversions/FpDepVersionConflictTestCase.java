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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ArtifactCoords.Gav;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.repomanager.FeaturePackRepositoryManager;
import org.jboss.galleon.spec.PackageDependencySpec;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.PmProvisionConfigTestBase;

/**
 *
 * @author Alexey Loubyansky
 */
public class FpDepVersionConflictTestCase extends PmProvisionConfigTestBase {

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
            .install();
    }

    @Override
    protected ProvisioningConfig provisioningConfig()
            throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.forGav(FP2_200_GAV))
                .addFeaturePackDep(FeaturePackConfig.forGav(FP3_100_GAV))
                .build();
    }

    @Override
    protected String[] pmErrors() throws ProvisioningException {
        final Set<ArtifactCoords.Gav> set = new LinkedHashSet<>(2);
        set.add(FP1_100_GAV);
        set.add(FP1_101_GAV);
        return new String[] {
                Errors.fpVersionCheckFailed(Collections.emptyList(), Collections.singleton(set))
        };
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningDescriptionException {
        return null;
    }
}
