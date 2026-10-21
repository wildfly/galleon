/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
public class SinglePluginTestCase extends PmProvisionConfigTestBase {

    public static class Plugin1 implements InstallPlugin {
        @Override
        public void postInstall(ProvisioningRuntime ctx) throws ProvisioningException {
            try {
                IoUtils.writeFile(ctx.getStagedDir().resolve("plugin1.txt"), "plugin1");
            } catch (IOException e) {
                throw new ProvisioningException("Failed to write a file");
            }
        }
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
            .newFeaturePack(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final"))
                .newPackage("p1", true)
                    .writeContent("fp1/p1.txt", "p1")
                    .getFeaturePack()
                .addPlugin(Plugin1.class);
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
                .build();
    }
}
