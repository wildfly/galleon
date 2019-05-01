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
package org.jboss.galleon.featurepack.resources.test;

import java.io.IOException;
import java.nio.file.Path;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.plugin.InstallPlugin;
import org.jboss.galleon.runtime.ProvisioningRuntime;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.PmInstallFeaturePackTestBase;
import org.jboss.galleon.test.util.fs.state.DirState;
import org.jboss.galleon.util.IoUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class SimpleResourcesOverwriteTestCase extends PmInstallFeaturePackTestBase {

    public static class ResourcesCopyingPlugin implements InstallPlugin {
        @Override
        public void postInstall(ProvisioningRuntime runtime) throws ProvisioningException {
            final Path resources = runtime.getResource(".");
            try {
                IoUtils.copy(resources, runtime.getStagedDir());
            } catch (IOException e1) {
                throw new ProvisioningException("Failed to copy resources");
            }
        }
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
            .newFeaturePack(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final"))
                .addDependency(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "2", "2.0.0.Final").getLocation())
                .newPackage("main", true)
                    .getFeaturePack()
                .writeResources("res1.txt", "fp1")
                .addPlugin(ResourcesCopyingPlugin.class)
                .getCreator()
            .newFeaturePack(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "2", "2.0.0.Final"))
                .addDependency(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp3", "2", "2.0.0.Final").getLocation())
                .newPackage("main", true)
                    .getFeaturePack()
                .writeResources("res1.txt", "fp2")
                .writeResources("res2.txt", "fp2")
                .getCreator()
            .newFeaturePack(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp3", "2", "2.0.0.Final"))
                .newPackage("main", true)
                    .getFeaturePack()
                .writeResources("res1.txt", "fp3")
                .writeResources("res2.txt", "fp3")
                .writeResources("res3.txt", "fp3")
                .getCreator()
            .install();
    }

    @Override
    protected FeaturePackConfig featurePackConfig()
            throws ProvisioningDescriptionException {
        return FeaturePackConfig.forLocation(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final").getLocation());
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp3", "2", "2.0.0.Final"))
                        .addPackage("main")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "2", "2.0.0.Final"))
                        .addPackage("main")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final"))
                        .addPackage("main")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("res1.txt", "fp1")
                .addFile("res2.txt", "fp2")
                .addFile("res3.txt", "fp3")
                .build();
    }
}
