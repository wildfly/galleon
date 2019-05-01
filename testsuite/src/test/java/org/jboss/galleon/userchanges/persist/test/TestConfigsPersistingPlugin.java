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
package org.jboss.galleon.userchanges.persist.test;

import java.nio.file.Path;

import org.jboss.galleon.Constants;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.diff.FsDiff;
import org.jboss.galleon.plugin.InstallPlugin;
import org.jboss.galleon.runtime.ProvisioningRuntime;
import org.jboss.galleon.state.ProvisionedConfig;
import org.jboss.galleon.xml.ProvisionedConfigXmlWriter;

/**
 *
 * @author Alexey Loubyansky
 */
public class TestConfigsPersistingPlugin implements InstallPlugin {

    @Override
    public void preInstall(ProvisioningRuntime runtime) throws ProvisioningException {
        final FsDiff fsDiff = runtime.getFsDiff();
        if(fsDiff == null) {
            return;
        }
        if(fsDiff.getEntry("tmp/running-marker") != null) {
            throw new ProvisioningException("The installation is up and running");
        }
    }

    @Override
    public void postInstall(ProvisioningRuntime runtime) throws ProvisioningException {
        final Path configsDir = runtime.getStagedDir().resolve(Constants.CONFIGS);
        for(ProvisionedConfig config : runtime.getConfigs()) {
            Path configPath = configsDir;
            if(config.getModel() != null) {
                configPath = configPath.resolve(config.getModel());
            }
            configPath = configPath.resolve(config.getName());
            try {
                ProvisionedConfigXmlWriter.getInstance().write(config, configPath);
            } catch (Exception e) {
                throw new ProvisioningException("Failed to persist config " + new ConfigId(config.getModel(), config.getName()), e);
            }
        }
    }
}
