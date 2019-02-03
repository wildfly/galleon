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
package org.jboss.galleon.cli.model.state;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;

import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.junit.Assert;
import org.junit.Test;

public class FeaturePackProvisioningTestCase {

    // location format is: producer[@factory[(location)]]:channel[/frequency]#build
    private static FeaturePackLocation FEATURE_PACK_LOCATION = FeaturePackLocation.fromString("producer@factory:channel#build");
    private static ConfigId CONFIG_ID = new ConfigId("model", "name");

    private FeaturePackProvisioning provisioning;
    private FeaturePackConfig.Builder featurePackConfigBuilder;
    private ProvisioningConfig.Builder provisioningConfigBuilder;
    private Deque<State.Action> actions = new ArrayDeque<>();

    public FeaturePackProvisioningTestCase() throws Exception {
        provisioning = new FeaturePackProvisioning();
        featurePackConfigBuilder = FeaturePackConfig.builder(FEATURE_PACK_LOCATION);
        provisioningConfigBuilder = ProvisioningConfig.builder()
                .addFeaturePackDep(featurePackConfigBuilder.build());
    }

    @Test
    public void testIncludeExcludeConfig() throws Exception {
        FeaturePackConfig builtConfig;

        // step 0 - no inclusions/exclusions initially
        Assert.assertTrue(getFeaturePackConfig().getIncludedConfigs().isEmpty());
        Assert.assertTrue(getFeaturePackConfig().getExcludedConfigs().isEmpty());

        // step 1 - include config
        builtConfig = performAction(provisioning.includeConfiguration(Collections.singletonMap(getFeaturePackConfig(), CONFIG_ID)));
        Assert.assertTrue(builtConfig.getIncludedConfigs().contains(CONFIG_ID));
        Assert.assertTrue(builtConfig.getExcludedConfigs().isEmpty());

        // step 2 - exclude the same config -> previous include should be removed
        builtConfig = performAction(provisioning.excludeConfiguration(Collections.singletonMap(getFeaturePackConfig(), CONFIG_ID)));
        Assert.assertTrue(builtConfig.getIncludedConfigs().isEmpty());
        Assert.assertTrue(builtConfig.getExcludedConfigs().contains(CONFIG_ID));

        // step 3 - include the same config again -> previous exclude should be removed
        builtConfig = performAction(provisioning.includeConfiguration(Collections.singletonMap(getFeaturePackConfig(), CONFIG_ID)));
        Assert.assertTrue(builtConfig.getIncludedConfigs().contains(CONFIG_ID));
        Assert.assertTrue(builtConfig.getExcludedConfigs().isEmpty());


        // undo step 3
        builtConfig = undo();
        Assert.assertTrue(builtConfig.getIncludedConfigs().isEmpty());
        Assert.assertTrue(builtConfig.getExcludedConfigs().contains(CONFIG_ID));

        // undo step 2
        builtConfig = undo();
        Assert.assertTrue(builtConfig.getIncludedConfigs().contains(CONFIG_ID));
        Assert.assertTrue(builtConfig.getExcludedConfigs().isEmpty());

        // undo step 1
        builtConfig = undo();
        Assert.assertTrue(builtConfig.getIncludedConfigs().isEmpty());
        Assert.assertTrue(builtConfig.getExcludedConfigs().isEmpty());
    }

    @Test
    public void testIncludeExcludePackage() throws Exception {
        FeaturePackConfig builtConfig;

        // no inclusions/exclusions initially
        Assert.assertTrue(getFeaturePackConfig().getIncludedPackages().isEmpty());
        Assert.assertTrue(getFeaturePackConfig().getExcludedPackages().isEmpty());

        // include a package
        builtConfig = performAction(provisioning.includePackage("package", getFeaturePackConfig()));
        Assert.assertTrue(builtConfig.getIncludedPackages().contains("package"));
        Assert.assertTrue(builtConfig.getExcludedPackages().isEmpty());

        // exclude the same package
        builtConfig = performAction(provisioning.excludePackage("package", getFeaturePackConfig()));
        Assert.assertTrue(builtConfig.getIncludedPackages().isEmpty());
        Assert.assertTrue(builtConfig.getExcludedPackages().contains("package"));

        // include the same package again
        builtConfig = performAction(provisioning.includePackage("package", getFeaturePackConfig()));
        Assert.assertTrue(builtConfig.getIncludedPackages().contains("package"));
        Assert.assertTrue(builtConfig.getExcludedPackages().isEmpty());

        // undo
        builtConfig = undo();
        Assert.assertTrue(builtConfig.getIncludedPackages().isEmpty());
        Assert.assertTrue(builtConfig.getExcludedPackages().contains("package"));

        builtConfig = undo();
        Assert.assertTrue(builtConfig.getIncludedPackages().contains("package"));
        Assert.assertTrue(builtConfig.getExcludedPackages().isEmpty());

        builtConfig = undo();
        Assert.assertTrue(builtConfig.getIncludedPackages().isEmpty());
        Assert.assertTrue(builtConfig.getExcludedPackages().isEmpty());
    }

    private FeaturePackConfig performAction(State.Action action) throws Exception {
        action.doAction(provisioningConfigBuilder.build(), provisioningConfigBuilder);
        actions.push(action);
        return featurePackConfigBuilder.build();
    }

    private FeaturePackConfig undo() throws Exception {
        State.Action action = actions.pop();
        action.undoAction(provisioningConfigBuilder);
        return featurePackConfigBuilder.build();
    }

    private FeaturePackConfig getFeaturePackConfig() {
        return featurePackConfigBuilder.build();
    }
}
