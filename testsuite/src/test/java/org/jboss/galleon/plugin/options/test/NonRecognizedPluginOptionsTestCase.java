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

package org.jboss.galleon.plugin.options.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.plugin.PluginOption;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;

/**
 *
 * @author Alexey Loubyansky
 */
public class NonRecognizedPluginOptionsTestCase extends PluginOptionsTestBase {

    public static class Plugin1 extends PluginBase {
        protected Map<String, PluginOption> initOptions() {
            final Map<String, PluginOption> options = new HashMap<>();
            addOption(options, PluginOption.builder("p1o1").build());
            addOption(options, PluginOption.builder("p1o2").setPersistent(false).build());
            addOption(options, PluginOption.builder("p1o3").setDefaultValue("false").build());
            addOption(options, PluginOption.builder("p1o4").setDefaultValue("false").build());
            return options;
        }
    }

    public static class Plugin2 extends PluginBase {
        protected Map<String, PluginOption> initOptions() {
            final Map<String, PluginOption> options = new HashMap<>();
            addOption(options, PluginOption.builder("p2o1").build());
            addOption(options, PluginOption.builder("p2o2").setPersistent(false).build());
            addOption(options, PluginOption.builder("p2o3").setDefaultValue("false").build());
            addOption(options, PluginOption.builder("p2o4").setDefaultValue("false").build());
            return options;
        }
    }

    private FeaturePackLocation prod1;
    private FeaturePackLocation prod2;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1");
        universe.createProducer("prod2");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        prod1 = newFpl("prod1", "1", "1.0.0.Final");
        creator.newFeaturePack(prod1.getFPID())
            .setPluginFileName("plugin1.jar")
            .addPlugin(Plugin1.class);

        prod2 = newFpl("prod2", "1", "1.0.0.Final");
        creator.newFeaturePack(prod2.getFPID())
            .addDependency(prod1)
            .setPluginFileName("plugin2.jar")
            .addPlugin(Plugin2.class);

        creator.install();
    }

    @Override
    protected void testPm(ProvisioningManager pm) throws ProvisioningException {
        final Map<String, String> options = new HashMap<>();
        options.put("p1o1", "v1");
        options.put("p1o2", "v2");
        options.put("p1o3", "true");
        options.put("p1o4", "false");
        options.put("p2o1", "v3");
        options.put("p2o2", "v4");
        options.put("p2o3", "true");
        options.put("p2o4", "false");
        options.put("p2o5", "true");
        pm.provision(ProvisioningConfig.builder()
                .addFeaturePackDep(prod2)
                .addPluginOption("p1o5", null)
                .build(), options);
    }

    @Override
    protected String[] pmErrors() {
        return new String[] {Errors.pluginOptionsNotRecognized(Arrays.asList(new String[] {"p1o5", "p2o5"}))};
    }
}
