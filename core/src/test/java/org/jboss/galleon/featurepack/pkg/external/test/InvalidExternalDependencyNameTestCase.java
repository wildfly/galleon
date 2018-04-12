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
package org.jboss.galleon.featurepack.pkg.external.test;


import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.test.FeaturePackRepoTestBase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Alexey Loubyansky
 */
public class InvalidExternalDependencyNameTestCase extends FeaturePackRepoTestBase {

    @Test
    public void setupRepo() throws Exception {
        try {
            getRepoManager()
                    .installer()
                    .newFeaturePack(ArtifactCoords.newGav("org.pm.test", "fp1", "1.0.0.Final"))
                            .addDependency("fp2-dep",
                                    FeaturePackConfig.builder(ArtifactCoords.newGav("org.pm.test", "fp2", "1.0.0.Final")).build())
                            .newPackage("p1", true)
                                    .addDependency("fp2-depp", "p2")
                                    .writeContent("fp1/p1.txt", "p1")
                                    .getFeaturePack()
                            .getInstaller()
                    .newFeaturePack(ArtifactCoords.newGav("org.pm.test", "fp2", "1.0.0.Final"))
                            .newPackage("p1", true)
                                    .writeContent("fp2/p1.txt", "p1")
                                    .getFeaturePack()
                            .getInstaller()
                    .install();
        } catch (ProvisioningDescriptionException e) {
            Assert.assertEquals(Errors.unknownFeaturePackDependencyName(ArtifactCoords.newGav("org.pm.test", "fp1", "1.0.0.Final"), "p1", "fp2-depp"), e.getLocalizedMessage());
        }
    }
}
