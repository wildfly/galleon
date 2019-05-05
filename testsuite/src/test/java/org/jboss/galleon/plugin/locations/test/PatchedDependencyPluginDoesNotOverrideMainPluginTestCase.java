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
package org.jboss.galleon.plugin.locations.test;

import java.io.IOException;
import org.jboss.galleon.ProvisioningDescriptionException;
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
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.maven.MavenArtifact;
import org.jboss.galleon.util.IoUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class PatchedDependencyPluginDoesNotOverrideMainPluginTestCase extends PluginLocationsTestBase {

    public static class Fp1Plugin1 implements InstallPlugin {
        @Override
        public void postInstall(ProvisioningRuntime runtime) throws ProvisioningException {
            try {
                IoUtils.writeFile(runtime.getStagedDir().resolve("fp1-plugin.txt"), "fp1");
            } catch (IOException e) {
                throw new ProvisioningException(getClass().getName() + " has failed", e);
            }
        }
    }

    public static class TestPlugin1 implements InstallPlugin {
        @Override
        public void postInstall(ProvisioningRuntime runtime) throws ProvisioningException {
            try {
                IoUtils.writeFile(runtime.getStagedDir().resolve("plugin1.txt"), "plugin1");
            } catch (IOException e) {
                throw new ProvisioningException(getClass().getName() + " has failed", e);
            }
        }
    }

    public static class TestPlugin2 implements InstallPlugin {
        @Override
        public void postInstall(ProvisioningRuntime runtime) throws ProvisioningException {
            try {
                IoUtils.writeFile(runtime.getStagedDir().resolve("plugin2.txt"), "plugin2");
            } catch (IOException e) {
                throw new ProvisioningException(getClass().getName() + " has failed", e);
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

        final MavenArtifact fp1Plugin =  installPluginArtifact(
                new MavenArtifact()
                .setGroupId("org.jboss.galleon.plugin.test")
                .setArtifactId("fp1-plugin")
                .setVersion("1"),
                Fp1Plugin1.class);

        final MavenArtifact plugin1 =  installPluginArtifact(
                new MavenArtifact()
                .setGroupId("org.jboss.galleon.plugin.test")
                .setArtifactId("test-plugin")
                .setVersion("1"),
                TestPlugin1.class);

        final MavenArtifact plugin2 = installPluginArtifact(
                new MavenArtifact()
                .setGroupId("org.jboss.galleon.plugin.test")
                .setArtifactId("test-plugin")
                .setVersion("2"),
                TestPlugin2.class);

        fp1 = newFpl("prod1", "1", "1.0.0.Final");
        creator.newFeaturePack(fp1.getFPID())
            .addPlugin("test-plugin", plugin1.getCoordsAsString())
            .addPlugin("fp1-plugin", fp1Plugin.getCoordsAsString());

        fp1Patch1 = newFpl("prod1", "1", "1.0.0.Patch1.Final");
        creator.newFeaturePack(fp1Patch1.getFPID())
            .setPatchFor(fp1.getFPID())
            .addPlugin("test-plugin", plugin2.getCoordsAsString());

        fp2 = newFpl("prod2", "1", "1.0.0.Final");
        creator.newFeaturePack(fp2.getFPID())
            .addDependency(fp1)
            .addPlugin("test-plugin", plugin1.getCoordsAsString());

        creator.install();
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.transitiveBuilder(fp1)
                        .addPatch(fp1Patch1.getFPID())
                        .build())
                .addFeaturePackDep(fp2)
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(fp1.getFPID())
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(fp2.getFPID())
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1-plugin.txt", "fp1")
                .addFile("plugin1.txt", "plugin1")
                .build();
    }
}
