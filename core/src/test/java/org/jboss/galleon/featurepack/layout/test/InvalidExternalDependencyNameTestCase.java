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
package org.jboss.galleon.featurepack.layout.test;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ArtifactCoords.Gav;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.layout.FeaturePackLayout;
import org.jboss.galleon.spec.FeaturePackSpec;
import org.jboss.galleon.spec.PackageSpec;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Alexey Loubyansky
 */
public class InvalidExternalDependencyNameTestCase {

    private static final Gav fp1Gav = ArtifactCoords.newGav("g", "a1", "v");
    private static final Gav fp2Gav = ArtifactCoords.newGav("g", "a2", "v");

    @Test
    public void testRequiredDependency() throws Exception {

        final FeaturePackLayout.Builder builder = FeaturePackLayout.builder(FeaturePackSpec.builder(fp1Gav)
                        .addFeaturePackDep(FeaturePackConfig.forGav(fp2Gav))
                        .addDefaultPackage("p1"))
                        .addPackage(PackageSpec.builder("p1")
                                .addPackageDep("fp2dep", "p2")
                                .build());

        try {
            builder.build();
            Assert.fail("Cannot build feature-pack description with inconsistent package dependencies.");
        } catch (ProvisioningDescriptionException e) {
            Assert.assertEquals(Errors.unknownFeaturePackDependencyName(fp1Gav, "p1", "fp2dep"), e.getMessage());
        }
    }

    @Test
    public void testOptionalDependency() throws Exception {

        final FeaturePackLayout.Builder builder = FeaturePackLayout.builder(FeaturePackSpec.builder(fp1Gav)
                .addFeaturePackDep(FeaturePackConfig.forGav(fp2Gav))
                .addDefaultPackage("p1"))
                .addPackage(PackageSpec.builder("p1")
                        .addPackageDep("fp2dep", "p2", true)
                        .build());

        try {
            builder.build();
            Assert.fail("Cannot build feature-pack description with inconsistent package dependencies.");
        } catch (ProvisioningDescriptionException e) {
            Assert.assertEquals(Errors.unknownFeaturePackDependencyName(fp1Gav, "p1", "fp2dep"), e.getMessage());
        }
    }
}
