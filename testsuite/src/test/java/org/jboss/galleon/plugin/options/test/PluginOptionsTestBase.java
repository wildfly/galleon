/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.plugin.options.test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningOption;
import org.jboss.galleon.plugin.InstallPlugin;
import org.jboss.galleon.runtime.ProvisioningRuntime;
import org.jboss.galleon.universe.SingleUniverseTestBase;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class PluginOptionsTestBase extends SingleUniverseTestBase {

    public abstract static class PluginBase implements InstallPlugin {

        protected static void addOption(Map<String, ProvisioningOption> options, ProvisioningOption option) {
            options.put(option.getName(), option);
        }

        protected final Map<String, ProvisioningOption> options;
        {
            options = Collections.unmodifiableMap(initOptions());
        }

        protected abstract Map<String, ProvisioningOption> initOptions();

        @Override
        public Map<String, ProvisioningOption> getOptions() {
            return options;
        }

        @Override
        public void postInstall(ProvisioningRuntime ctx) throws ProvisioningException {
            handleOptions(ctx, options.values());
        }
    }

    protected static void handleOptions(ProvisioningRuntime rt, Collection<ProvisioningOption> options) throws ProvisioningException {
        for(ProvisioningOption option : options) {
            if(!rt.isOptionSet(option)) {
                continue;
            }
            writeContent(rt.getStagedDir().resolve(option.getName()), rt.getOptionValue(option));
        }
    }

    protected static void writeContent(Path p, String content) {
        try {
            Files.createDirectories(p.getParent());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create parent directories for " + p, e);
        }
        try(BufferedWriter writer = Files.newBufferedWriter(p)) {
            writer.write(content);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write to " + p, e);
        }
    }
}
