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
package org.jboss.galleon.preinstall.test;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.plugin.StateDiffPlugin;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.util.fs.state.DirState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.userchanges.persist.test.BasicStateDiffPlugin;
import org.jboss.galleon.userchanges.persist.test.TestConfigsPersistingPlugin;
import org.jboss.galleon.userchanges.test.UserChangesTestBase;

/**
 *
 * @author Alexey Loubyansky
 */
public class PreInstallFailureTestCase extends UserChangesTestBase {

    private FeaturePackLocation prod1;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        prod1 = newFpl("prod1", "1", "1.0.0.Final");
        creator.newFeaturePack(prod1.getFPID())
            .addFeatureSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("id"))
                    .addParam(FeatureParameterSpec.create("a", "spec"))
                    .addParam(FeatureParameterSpec.create("b", "spec"))
                    .addParam(FeatureParameterSpec.create("c", "spec"))
                    .build())
            .addConfig(ConfigModel.builder("model1", "name1")
                    .addFeature(new FeatureConfig("specA")
                            .setParam("id", "1")
                            .setParam("a", "config")
                            .setParam("b", "config"))
                    .build(), false)
            .addService(StateDiffPlugin.class, BasicStateDiffPlugin.class)
            .addPlugin(TestConfigsPersistingPlugin.class);

        creator.install();
    }

    @Override
    protected ProvisioningConfig initialState() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder().addFeaturePackDep(prod1).build();
    }

    @Override
    protected void testPm(ProvisioningManager pm) throws ProvisioningException {
        writeContent("tmp/running-marker", "running");
        pm.install(prod1);
    }

    @Override
    protected String[] pmErrors() {
        return new String[] {"The installation is up and running"};
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningDescriptionException {
        return ProvisionedState.builder().build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("tmp/running-marker", "running")
                .build();
    }
}
