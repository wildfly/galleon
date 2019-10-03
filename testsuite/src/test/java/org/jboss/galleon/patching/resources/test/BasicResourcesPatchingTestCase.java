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
package org.jboss.galleon.patching.resources.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.plugin.InstallPlugin;
import org.jboss.galleon.runtime.FeaturePackRuntime;
import org.jboss.galleon.runtime.ProvisioningRuntime;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.util.fs.state.DirState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.ProvisionFromUniverseTestBase;
import org.jboss.galleon.util.IoUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class BasicResourcesPatchingTestCase extends ProvisionFromUniverseTestBase {

    public static class Plugin1 implements InstallPlugin {
        @Override
        public void postInstall(ProvisioningRuntime ctx) throws ProvisioningException {
            for(FeaturePackRuntime fp : ctx.getFeaturePacks()) {
                try {
                    copyIfExists(ctx, fp, "fp1");
                    copyIfExists(ctx, fp, "common");
                } catch (IOException e) {
                    throw new ProvisioningException("Failed to write a file", e);
                }
            }

            try {
                Path resource = ctx.getResource("fp1");
                if(Files.exists(resource)) {
                    IoUtils.copy(resource, ctx.getStagedDir().resolve("global").resolve("fp1"));
                }
                resource = ctx.getResource("common");
                if(Files.exists(resource)) {
                    IoUtils.copy(ctx.getResource("common"), ctx.getStagedDir().resolve("global").resolve("common"));
                }
            } catch (IOException e) {
                throw new ProvisioningException("Failed to write a file", e);
            }
        }

        private void copyIfExists(ProvisioningRuntime ctx, FeaturePackRuntime fp, String name)
                throws ProvisioningDescriptionException, IOException {
            final Path resource = fp.getResource(name);
            if(Files.exists(resource)) {
                IoUtils.copy(resource, ctx.getStagedDir().resolve(name));
            }
        }
    }

    private FeaturePackLocation fp1;
    private FeaturePackLocation fp1Patch1;
    private FeaturePackLocation fp2;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1");
        universe.createProducer("prod2");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        fp1 = newFpl("prod1", "1", "1.0.0.Final");
        creator.newFeaturePack(fp1.getFPID())
            .addPlugin(Plugin1.class)
            .writeResources("fp1/resources/file1.txt", "fp1")
            .writeResources("fp1/resources/file2.txt", "fp1")
            .writeResources("common/resources/file1.txt", "fp1")
            .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "fp1 p1");

        fp1Patch1 = newFpl("prod1", "1", "1.0.0.Patch1.Final");
        creator.newFeaturePack(fp1Patch1.getFPID())
            .setPatchFor(fp1.getFPID())
            .writeResources("fp1/resources/file2.txt", "patched")
            .writeResources("common/resources/file1.txt", "patched");

        fp2 = newFpl("prod2", "1", "1.0.0.Final");
        creator.newFeaturePack(fp2.getFPID())
            .writeResources("common/resources/file1.txt", "fp2");

    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        final ProvisioningConfig config = ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(fp1)
                        .addPatch(fp1Patch1.getFPID())
                        .build())
                .addFeaturePackDep(fp2)
                .build();
        return config;
    }


    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(fp1.getFPID())
                        .addPackage("p1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(fp2.getFPID()).build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1/p1.txt", "fp1 p1")
                .addFile("fp1/resources/file1.txt", "fp1")
                .addFile("fp1/resources/file2.txt", "patched")
                .addFile("common/resources/file1.txt", "fp2")
                .addFile("global/fp1/resources/file1.txt", "fp1")
                .addFile("global/fp1/resources/file2.txt", "patched")
                .addFile("global/common/resources/file1.txt", "fp2")
                .build();
    }
}
