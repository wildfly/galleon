/*
 * Copyright 2016-2022 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.userchanges.systemfiles.test;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.diff.FsDiff;
import org.jboss.galleon.layout.ProvisioningPlan;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.util.fs.state.DirState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.userchanges.test.UserChangesTestBase;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Alexey Loubyansky
 */
public class UserChangesAfterUpdatePlanTestCase extends UserChangesTestBase {

    private FeaturePackLocation prod100;
    private FeaturePackLocation prod101;
    private FeaturePackLocation prod200;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1");
        universe.createProducer("prod2");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        prod100 = newFpl("prod1", "1", "1.0.0.Final");
        creator.newFeaturePack(prod100.getFPID())
            .addSystemPaths("prod1/protected")
            .newPackage("p1", true)
                .writeContent("prod1/protected/p1.txt", "prod1 p1")
                .writeContent("prod1/protected/p3.txt", "prod1 p3")
                .writeContent("prod1/protected/p4.txt", "prod1 p4")
                .writeContent("prod1/protected/p5.txt", "prod1 p5")
                .getFeaturePack();

        prod101 = newFpl("prod1", "1", "1.0.1.Final");
        creator.newFeaturePack(prod101.getFPID())
            .addSystemPaths("prod1/protected")
            .newPackage("p1", true)
                .writeContent("prod1/protected/p1.txt", "prod101 p1")
                .writeContent("prod1/protected/p2.txt", "prod101 p2")
                .writeContent("prod1/protected/p3.txt", "prod101 p3")
                .writeContent("prod1/protected/p5.txt", "prod1 p5") // content not changed
                .getFeaturePack();

        prod200 = newFpl("prod2", "1", "1.0.0.Final");
        creator.newFeaturePack(prod200.getFPID())
            .addSystemPaths("prod2/protected")
            .newPackage("p1", true)
                .writeContent("prod2/p1.txt", "prod2 p1");

    }

    @Override
    protected void testPm(ProvisioningManager pm) throws ProvisioningException {
        pm.install(prod100);
        writeContent("new.txt", "user");
        writeContent("prod1/protected/new.txt", "user");
        writeContent("prod1/protected/p2.txt", "user");
        writeContent("prod1/protected/p1.txt", "user");
        writeContent("prod1/protected/p5.txt", "user");
        recursiveDelete("prod1/protected/p3.txt");
        recursiveDelete("prod1/protected/p4.txt");

        pm.apply(ProvisioningPlan.builder()
                .install(prod101)
                .install(prod200));
    }


    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(prod101)
                        .build())
                .addFeaturePackDep(FeaturePackConfig.builder(prod200)
                        .build())
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(prod101.getFPID())
                        .addPackage("p1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(prod200.getFPID())
                        .addPackage("p1")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("prod1/protected/p1.txt", "prod101 p1")
                .addFile("prod1/protected/p1.txt.glold", "user")
                .addFile("prod1/protected/p2.txt", "prod101 p2")
                .addFile("prod1/protected/p2.txt.glold", "user")
                .addFile("prod1/protected/p3.txt", "prod101 p3")
                .addFile("prod1/protected/p5.txt", "user")
                .addFile("prod1/protected/new.txt", "user")
                .addFile("prod2/p1.txt", "prod2 p1")
                .addFile("new.txt", "user")
                .build();
    }

    @Override
    protected List<String> expectedDiff() {
        return Arrays.asList(
                FsDiff.formatMessage(FsDiff.FORCED, "prod1/protected/p3.txt", FsDiff.HAS_CHANGED_IN_THE_UPDATED_VERSION),
                FsDiff.formatMessage(FsDiff.REMOVED, "prod1/protected/p4.txt", FsDiff.HAS_BEEN_REMOVED_FROM_THE_UPDATED_VERSION),
                FsDiff.formatMessage(FsDiff.ADDED, "prod1/protected/new.txt", null),
                FsDiff.formatMessage(FsDiff.ADDED, "new.txt", null),
                FsDiff.formatMessage(FsDiff.FORCED, "prod1/protected/p2.txt", FsDiff.CONFLICTS_WITH_THE_UPDATED_VERSION),
                FsDiff.formatMessage(FsDiff.FORCED, "prod1/protected/p1.txt", FsDiff.HAS_CHANGED_IN_THE_UPDATED_VERSION),
                FsDiff.formatMessage(FsDiff.MODIFIED, "prod1/protected/p5.txt", null)
        );
    }
}
