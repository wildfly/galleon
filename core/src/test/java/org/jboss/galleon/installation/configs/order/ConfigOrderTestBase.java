/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.installation.configs.order;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.plugin.InstallPlugin;
import org.jboss.galleon.plugin.ProvisionedConfigHandler;
import org.jboss.galleon.runtime.ProvisioningRuntime;
import org.jboss.galleon.state.ProvisionedConfig;
import org.jboss.galleon.test.PmProvisionConfigTestBase;
import org.jboss.galleon.test.util.fs.state.DirState;

/**
 *
 * @author Alexey Loubyansky
 */
abstract class ConfigOrderTestBase extends PmProvisionConfigTestBase {

    protected static final String CONFIG_LIST_NAME = "configs.list";

    public static class ConfigListPlugin implements InstallPlugin, ProvisionedConfigHandler {

        private int configCount;
        private BufferedWriter configWriter;
        final StringBuilder buf = new StringBuilder();

        @Override
        public void postInstall(ProvisioningRuntime runtime) throws ProvisioningException {
            final Path configReport = runtime.getStagedDir().resolve(CONFIG_LIST_NAME);
            try (BufferedWriter configWriter = Files.newBufferedWriter(configReport)){
                this.configWriter = configWriter;
                for(ProvisionedConfig config : runtime.getConfigs()) {
                    config.handle(this);
                }
            } catch (IOException e) {
                throw new ProvisioningException("Failed to write the config list.", e);
            }
        }

        @Override
        public void prepare(ProvisionedConfig config) throws ProvisioningException {
            buf.setLength(0);
            if(config.getModel() != null) {
                buf.append(config.getModel()).append(' ');
            }
            if(config.getName() != null) {
                buf.append(config.getName());
            } else if(config.getModel() == null) {
                buf.append("anonymous " + config.getProperty("id"));
            }

            try {
                if (configCount == 0) {
                    configWriter.write(buf.toString());
                } else {
                    configWriter.write(", ");
                    configWriter.write(buf.toString());
                }
            } catch(IOException e) {
                throw new ProvisioningException("Failed to write config info", e);
            }
            ++configCount;
        }
    }

    protected abstract String[] configList();

    @Override
    protected DirState provisionedHomeDir() {
        final StringBuilder buf = new StringBuilder();

        final String[] provisionedConfigs = configList();
        if(provisionedConfigs == null) {
            return DirState.rootBuilder().build();
        }
        if(provisionedConfigs.length > 0) {
            buf.append(provisionedConfigs[0]);
            for(int i = 1; i < provisionedConfigs.length; ++i){
                buf.append(", ").append(provisionedConfigs[i]);
            }
        }

        return newDirBuilder()
                .addFile(CONFIG_LIST_NAME, buf.toString())
                .build();
    }
}
