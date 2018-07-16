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

package org.jboss.galleon.install.test;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.util.fs.state.DirState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.InstallFromUniverseTestBase;
import org.jboss.galleon.universe.MvnUniverse;

/**
 *
 * @author Alexey Loubyansky
 */
public class InstallTransitiveAsDirectTestCase extends InstallFromUniverseTestBase {

    private FeaturePackLocation fp1;
    private FeaturePackLocation fp2;
    private FeaturePackLocation fp3;
    private FeaturePackLocation fp4;
    private FeaturePackLocation fp5;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1");
        universe.createProducer("prod2");
        universe.createProducer("prod3");
        universe.createProducer("prod4");
        universe.createProducer("prod5");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        fp1 = newFpl("prod1", "1", "1.0.0.Final");
        creator.newFeaturePack(fp1.getFPID())
            .newPackage("p1", true)
                .writeContent("common.txt", "fp1")
                .writeContent("fp1/p1.txt", "fp1 p1");

        fp2 = newFpl("prod2", "1", "1.0.0.Final");
        creator.newFeaturePack(fp2.getFPID())
            .addDependency(fp1)
            .newPackage("p1", true)
            .writeContent("common.txt", "fp2")
            .writeContent("fp2/p1.txt", "fp2 p1");

        fp3 = newFpl("prod3", "1", "1.0.0.Final");
        creator.newFeaturePack(fp3.getFPID())
            .addDependency(fp1)
            .newPackage("p1", true)
            .writeContent("common.txt", "fp3")
            .writeContent("fp3/p1.txt", "fp3 p1");

        fp4 = newFpl("prod4", "1", "1.0.0.Final");
        creator.newFeaturePack(fp4.getFPID())
            .addDependency(fp3)
            .newPackage("p1", true)
            .writeContent("common.txt", "fp4")
            .writeContent("fp4/p1.txt", "fp4 p1");

        fp5 = newFpl("prod5", "1", "1.0.0.Final");
        creator.newFeaturePack(fp5.getFPID())
            .newPackage("p1", true)
            .writeContent("common.txt", "fp5")
            .writeContent("fp5/p1.txt", "fp5 p1");

        creator.install();
    }

    @Override
    protected ProvisioningConfig initialState() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(fp5)
                .addFeaturePackDep(fp4)
                .addFeaturePackDep(fp2)
                .build();
    }

    @Override
    protected FeaturePackConfig featurePackConfig() throws ProvisioningDescriptionException {
        return FeaturePackConfig.forLocation(fp1);
    }

    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(fp5)
                        .build())
                .addFeaturePackDep(FeaturePackConfig.builder(fp1)
                        .build())
                .addFeaturePackDep(FeaturePackConfig.builder(fp4)
                        .build())
                .addFeaturePackDep(FeaturePackConfig.builder(fp2)
                        .build())
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(fp5.getFPID())
                        .addPackage("p1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(fp1.getFPID())
                        .addPackage("p1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(fp3.getFPID())
                        .addPackage("p1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(fp4.getFPID())
                        .addPackage("p1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(fp2.getFPID())
                        .addPackage("p1")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1/p1.txt", "fp1 p1")
                .addFile("fp2/p1.txt", "fp2 p1")
                .addFile("fp3/p1.txt", "fp3 p1")
                .addFile("fp4/p1.txt", "fp4 p1")
                .addFile("fp5/p1.txt", "fp5 p1")
                .addFile("common.txt", "fp2")
                .build();
    }
}
