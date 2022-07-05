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
package org.jboss.galleon.userchanges.test.diff;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.diff.FsDiff;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.util.fs.state.DirState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.userchanges.test.UserChangesTestBase;

public class UserChangesFullDiffTestCaseTestCase extends UserChangesTestBase {

    private FeaturePackLocation prod100;
    private FeaturePackLocation prod101;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1");
        universe.createProducer("prod2");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        prod100 = newFpl("prod1", "1", "1.0.0.Final");
        creator.newFeaturePack(prod100.getFPID())
            .newPackage("p1", true)
                .addDependency("p2")
                .addDependency("p3")
                .writeContent("prod1/p1.txt", "prod100 p1")
                .getFeaturePack()
            .newPackage("p2")
                .writeContent("prod1/p2.txt", "prod100 p2")
                .getFeaturePack()
            .newPackage("p3")
                .writeContent("prod1/p3.txt", "prod100 p3")
                .getFeaturePack()
            .newPackage("common", true)
                .writeContent("common.txt", "prod100");

        prod101 = newFpl("prod1", "1", "1.0.1.Final");
        creator.newFeaturePack(prod101.getFPID())
            .newPackage("p1", true)
                .addDependency("p2")
                .addDependency("p3")
                .writeContent("prod1/p101.txt", "prod101 p1")
                .getFeaturePack()
            .newPackage("p2")
                .writeContent("prod1/p2.txt", "prod100 p2")
                .getFeaturePack()
            .newPackage("p3")
                .writeContent("prod1/p3.txt", "prod101 p3")
                .getFeaturePack()
            .newPackage("p4", true)
                .writeContent("prod1/p4.txt", "prod101 p4")
                .getFeaturePack()
            .newPackage("common", true)
                .writeContent("common.txt", "prod101");

    }

    @Override
    protected void testPm(ProvisioningManager pm) throws ProvisioningException {
        final HashMap<String, String> options = getPMOptions();
        pm.install(prod100, options);
        writeContent("new.txt", "user");
        writeContent("prod1/p1.txt", "user");
        writeContent("prod1/p2.txt", "user");
        writeContent("prod1/p3.txt", "user");
        writeContent("prod1/p4.txt", "user");
        pm.install(prod101, options);
    }


    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningDescriptionException {
        final ProvisioningConfig.Builder builder = ProvisioningConfig.builder()
           .addFeaturePackDep(FeaturePackConfig.builder(prod101).build());

        final HashMap<String, String> options = getPMOptions();
        for (Map.Entry<String, String> option : options.entrySet()) {
            builder.addOption(option.getKey(), option.getValue());
        }

        return builder.build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(prod101.getFPID())
                        .addPackage("p1")
                        .addPackage("p2")
                        .addPackage("p3")
                        .addPackage("p4")
                        .addPackage("common")
                        .build())
                .build();
    }

    protected HashMap<String, String> getPMOptions() {
        final HashMap<String, String> options = new HashMap<>();
        return options;
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("prod1/p1.txt", "user")
                .addFile("prod1/p2.txt", "user")
                .addFile("prod1/p3.txt", "user")
                .addFile("prod1/p3.txt.glnew", "prod101 p3")
                .addFile("prod1/p4.txt", "user")
                .addFile("prod1/p4.txt.glnew", "prod101 p4")
                .addFile("prod1/p101.txt", "prod101 p1")
                .addFile("common.txt", "prod101")
                .addFile("new.txt", "user")
                .build();
    }

    @Override
    protected List<String> expectedDiff() {
        return Arrays.asList(
           FsDiff.formatMessage(FsDiff.ADDED, "new.txt", null),
           FsDiff.formatMessage(FsDiff.MODIFIED, "prod1/p4.txt", FsDiff.CONFLICTS_WITH_THE_UPDATED_VERSION),
           FsDiff.formatMessage(FsDiff.ADDED, "prod1/p1.txt", FsDiff.HAS_BEEN_REMOVED_FROM_THE_UPDATED_VERSION),
           FsDiff.formatMessage(FsDiff.MODIFIED, "prod1/p2.txt", null),
           FsDiff.formatMessage(FsDiff.MODIFIED, "prod1/p3.txt", FsDiff.HAS_CHANGED_IN_THE_UPDATED_VERSION)
        );
    }
}
