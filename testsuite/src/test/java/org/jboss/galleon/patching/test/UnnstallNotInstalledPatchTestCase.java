/*
 * Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.patching.test;

import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.UninstallFeaturePackTestBase;

/**
 *
 * @author Alexey Loubyansky
 */
public class UnnstallNotInstalledPatchTestCase extends UninstallFeaturePackTestBase {

    private FeaturePackLocation fp1;
    private FeaturePackLocation fp1Patch1;
    private FeaturePackLocation fp1Patch2;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1");
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
            .setPatchFor(newFpl("prod1", "1", "1.0.1.Final").getFPID())
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

        creator.install();
    }

    @Override
    protected ProvisioningConfig initialState() throws ProvisioningException {
        return ProvisioningConfig.builder().addFeaturePackDep(fp1).build();
    }

    @Override
    protected FPID uninstallFpid() {
        return fp1Patch2.getFPID();
    }

    @Override
    protected String[] pmErrors() {
        return new String[] {Errors.unknownFeaturePack(fp1Patch2.getFPID())};
    }
}
