/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.patching.plugin.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.plugin.InstallPlugin;
import org.jboss.galleon.runtime.ProvisioningRuntime;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.util.fs.state.DirState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.UninstallFeaturePackTestBase;
import org.jboss.galleon.util.IoUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class PatchedDependencyPluginEffectiveAfterUninstallOfMainPluginTestCase extends UninstallFeaturePackTestBase {

    public static class Plugin1 implements InstallPlugin {
        @Override
        public void postInstall(ProvisioningRuntime ctx) throws ProvisioningException {
            try {
                final Path baseDir = ctx.getStagedDir().resolve("plugin");
                Files.createDirectories(baseDir);
                IoUtils.writeFile(baseDir.resolve("file1.txt"), "plugin1");
                IoUtils.writeFile(baseDir.resolve("file2.txt"), "plugin1");
            } catch (IOException e) {
                throw new ProvisioningException("Failed to write a file");
            }
        }
    }

    public static class Plugin2 implements InstallPlugin {
        @Override
        public void postInstall(ProvisioningRuntime ctx) throws ProvisioningException {
            try {
                final Path baseDir = ctx.getStagedDir().resolve("plugin");
                Files.createDirectories(baseDir);
                IoUtils.writeFile(baseDir.resolve("file1.txt"), "plugin1");
                IoUtils.writeFile(baseDir.resolve("file2.txt"), "patched");
            } catch (IOException e) {
                throw new ProvisioningException("Failed to write a file");
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
            .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "fp1 p1");

        fp1Patch1 = newFpl("prod1", "1", "1.0.0.Patch1.Final");
        creator.newFeaturePack(fp1Patch1.getFPID())
            .setPatchFor(fp1.getFPID())
            .addPlugin(Plugin2.class);

        fp2 = newFpl("prod2", "1", "1.0.0.Final");
        creator.newFeaturePack(fp2.getFPID())
            .addDependency(fp1)
            .addPlugin(Plugin1.class);

    }

    @Override
    protected ProvisioningConfig initialState() throws ProvisioningException {
        final ProvisioningConfig config = ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(fp1)
                        .addPatch(fp1Patch1.getFPID())
                        .build())
                .addFeaturePackDep(FeaturePackConfig.builder(fp2)
                        .build())
                .build();
        return config;
    }

    @Override
    protected FPID uninstallFpid() {
        return fp2.getFPID();
    }

    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningException {
        final ProvisioningConfig config = ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(fp1)
                        .addPatch(fp1Patch1.getFPID())
                        .build())
                .build();
        return config;
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(fp1.getFPID())
                        .addPackage("p1")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1/p1.txt", "fp1 p1")
                .addFile("plugin/file1.txt", "plugin1")
                .addFile("plugin/file2.txt", "patched")
                .build();
    }
}
