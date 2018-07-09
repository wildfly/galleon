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

package org.jboss.galleon.patching.pkg.test;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.util.fs.state.DirState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.UninstallFeaturePackTestBase;

/**
 *
 * @author Alexey Loubyansky
 */
public class UninstallPackagePatchTestCase extends UninstallFeaturePackTestBase {

    private FeaturePackLocation fp1;
    private FeaturePackLocation fp1Patch1;
    private FeaturePackLocation fp1Patch2;
    private FeaturePackLocation fp2;
    private FeaturePackLocation fp2Patch1;
    private FeaturePackLocation fp2Patch2;
    private FeaturePackLocation fp3;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1");
        universe.createProducer("prod2");
        universe.createProducer("prod3");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        fp1 = newFpl("prod1", "1", "1.0.0.Final");
        creator.newFeaturePack(fp1.getFPID())
            .newPackage("p1", true)
                .addDependency("p2")
                .addDependency("p3", true)
                .writeContent("fp1/p1.txt", "fp1 p1")
                .getFeaturePack()
            .newPackage("p2")
                .writeContent("fp1/p2.txt", "fp1 p2")
                .getFeaturePack()
            .newPackage("p3")
                .writeContent("fp1/p3.txt", "fp1 p3")
                .getFeaturePack()
            .newPackage("p4")
                .writeContent("fp1/p4.txt", "fp1 p4")
                .getFeaturePack()
            .newPackage("p5")
                .writeContent("fp1/p5.txt", "fp1 p5");

        fp1Patch1 = newFpl("prod1", "1", "1.0.0.Patch1.Final");
        creator.newFeaturePack(fp1Patch1.getFPID())
            .setPatchFor(fp1.getFPID())
            .newPackage("p1", true)
                .addDependency("p2")
                .addDependency("p3")
                .addDependency("p4", true)
                .getFeaturePack()
            .newPackage("p2")
                .writeContent("fp1/p2.txt", "fp1 p2 patch1")
                .getFeaturePack()
            .newPackage("p3")
                .writeContent("fp1/p3.txt", "fp1 p3 patch1");

        fp1Patch2 = newFpl("prod1", "1", "1.0.0.Patch2.Final");
        creator.newFeaturePack(fp1Patch2.getFPID())
            .setPatchFor(fp1.getFPID())
            .newPackage("p3")
                .writeContent("fp1/p3.txt", "fp1 p3 patch2");

        fp2 = newFpl("prod2", "1", "1.0.0.Final");
        creator.newFeaturePack(fp2.getFPID())
            .addDependency(fp1)
            .newPackage("p1")
                .writeContent("fp2/p1.txt", "fp2 p1")
                .getFeaturePack()
            .newPackage("p2")
                .writeContent("fp2/p2.txt", "fp2 p2");

        fp2Patch1 = newFpl("prod2", "1", "1.0.0.Patch1.Final");
        creator.newFeaturePack(fp2Patch1.getFPID())
            .setPatchFor(fp2.getFPID())
            .newPackage("p2")
                .writeContent("fp2/p2.txt", "fp2 p2 patch1");

        fp2Patch2 = newFpl("prod2", "1", "1.0.0.Patch2.Final");
        creator.newFeaturePack(fp2Patch2.getFPID())
            .setPatchFor(fp2.getFPID())
            .addDependency(fp2Patch1)
            .newPackage("p3")
                .addDependency("p1")
                .addDependency("p2");

        fp3 = newFpl("prod3", "1", "1.0.0.Final");
        creator.newFeaturePack(fp3.getFPID())
            .newPackage("p1", true)
                .writeContent("fp3/p1.txt", "fp3 p1");

        creator.install();
    }

    @Override
    protected ProvisioningConfig initialState() throws ProvisioningException {
        final ProvisioningConfig config = ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(fp2)
                        .addPatch(fp2Patch2.getFPID())
                        .includePackage("p3")
                        .build())
                .addFeaturePackDep(fp3)
                .addFeaturePackDep(FeaturePackConfig.transitiveBuilder(fp1)
                        .addPatch(fp1Patch1.getFPID())
                        .addPatch(fp1Patch2.getFPID())
                        .build())
                .build();
        return config;
    }

    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningException {
        final ProvisioningConfig config = ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(fp2)
                        .includePackage("p3")
                        .build())
                .addFeaturePackDep(fp3)
                .addFeaturePackDep(FeaturePackConfig.transitiveBuilder(fp1)
                        .addPatch(fp1Patch1.getFPID())
                        .addPatch(fp1Patch2.getFPID())
                        .build())
                .build();
        return config;
    }

    @Override
    protected FPID uninstallFpid() {
        return fp2Patch2.getFPID();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(fp1.getFPID())
                        .addPackage("p2")
                        .addPackage("p3")
                        .addPackage("p4")
                        .addPackage("p1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(fp2.getFPID())
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(fp3.getFPID())
                        .addPackage("p1")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1/p1.txt", "fp1 p1")
                .addFile("fp1/p2.txt", "fp1 p2 patch1")
                .addFile("fp1/p3.txt", "fp1 p3 patch2")
                .addFile("fp1/p4.txt", "fp1 p4")
                .addFile("fp3/p1.txt", "fp3 p1")
                .build();
    }
}
