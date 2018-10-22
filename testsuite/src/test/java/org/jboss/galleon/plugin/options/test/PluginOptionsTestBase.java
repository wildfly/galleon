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
