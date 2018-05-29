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
package org.jboss.galleon.plugin.test;

import java.io.IOException;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.plugin.InstallPlugin;
import org.jboss.galleon.runtime.ProvisioningRuntime;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.PmProvisionConfigTestBase;
import org.jboss.galleon.test.util.fs.state.DirState;
import org.jboss.galleon.util.IoUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class MultiplePluginsTestCase extends PmProvisionConfigTestBase {

    public static class Plugin1 implements InstallPlugin {
        @Override
        public void postInstall(ProvisioningRuntime ctx) throws ProvisioningException {
            try {
                writeFile(ctx);
            } catch (IOException e) {
                throw new ProvisioningException("Failed to write a file");
            }
        }

        protected void writeFile(ProvisioningRuntime ctx) throws IOException {
            writeFile(ctx, "plugin1.txt", "plugin1");
        }

        protected void writeFile(ProvisioningRuntime ctx, final String path, final String content) throws IOException {
            IoUtils.writeFile(ctx.getStagedDir().resolve(path), content);
        }
    }

    public static class Plugin2 extends Plugin1 {
        @Override
        protected void writeFile(ProvisioningRuntime ctx) throws IOException {
            writeFile(ctx, "plugin2.txt", "plugin2");
        }
    }

    public static class Plugin3 extends Plugin1 {
        @Override
        protected void writeFile(ProvisioningRuntime ctx) throws IOException {
            writeFile(ctx, "plugin3.txt", "plugin3");
        }
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
            .newFeaturePack(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final"))
                .newPackage("p1", true)
                    .writeContent("fp1/p1.txt", "p1")
                    .getFeaturePack()
                .addPlugin(Plugin1.class)
                .addPlugin(Plugin2.class)
                .addPlugin(Plugin3.class)
                .getCreator()
            .install();
    }

    @Override
    protected ProvisioningConfig provisioningConfig()
            throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(
                        FeaturePackConfig.forLocation(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final").getLocation()))
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final"))
                        .addPackage("p1")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1/p1.txt", "p1")
                .addFile("plugin1.txt", "plugin1")
                .addFile("plugin2.txt", "plugin2")
                .addFile("plugin3.txt", "plugin3")
                .build();
    }
}
