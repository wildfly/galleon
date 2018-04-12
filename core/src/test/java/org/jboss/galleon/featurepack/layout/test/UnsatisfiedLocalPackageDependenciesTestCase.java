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

import java.util.Collections;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ArtifactCoords.Gav;
import org.jboss.galleon.layout.FeaturePackLayout;
import org.jboss.galleon.spec.FeaturePackSpec;
import org.jboss.galleon.spec.PackageSpec;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Alexey Loubyansky
 */
public class UnsatisfiedLocalPackageDependenciesTestCase {

    private static final Gav fpGav = ArtifactCoords.newGav("g", "a", "v");

    @Test
    public void testRequiredDependency() throws Exception {

        final FeaturePackLayout.Builder builder = FeaturePackLayout
                .builder(FeaturePackSpec.builder(fpGav)
                        .addDefaultPackage("p1"))
                        .addPackage(PackageSpec.builder("p1")
                                .addPackageDep("p2", true)
                                .build())
                        .addPackage(PackageSpec.builder("p2")
                                .addPackageDep("p3", true)
                                .build())
                        .addPackage(PackageSpec.builder("p3")
                                .addPackageDep("p4")
                                .build());

        try {
            builder.build();
            Assert.fail("Cannot build feature-pack description with inconsistent package dependencies.");
        } catch (ProvisioningDescriptionException e) {
            Assert.assertEquals(Errors.unsatisfiedPackageDependencies(fpGav, "p3", Collections.singleton("p4")), e.getMessage());
        }
    }

    @Test
    public void testOptionalDependency() throws Exception {

        final FeaturePackLayout.Builder builder = FeaturePackLayout
                .builder(FeaturePackSpec.builder(fpGav)
                        .addDefaultPackage("p1"))
                        .addPackage(PackageSpec.builder("p1")
                                .addPackageDep("p2", true)
                                .build())
                        .addPackage(PackageSpec.builder("p2")
                                .addPackageDep("p3", true)
                                .build())
                        .addPackage(PackageSpec.builder("p3")
                                .addPackageDep("p4", true)
                                .build());

        try {
            builder.build();
            Assert.fail("Cannot build feature-pack description with inconsistent package dependencies.");
        } catch (ProvisioningDescriptionException e) {
            Assert.assertEquals(Errors.unsatisfiedPackageDependencies(fpGav, "p3", Collections.singleton("p4")), e.getMessage());
        }
    }
}
