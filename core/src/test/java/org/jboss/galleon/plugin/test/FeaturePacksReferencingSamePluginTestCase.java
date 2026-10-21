/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.plugin.test;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.PmProvisionConfigTestBase;
import org.jboss.galleon.test.util.fs.state.DirState;
/**
 *
 * @author Alexey Loubyansky
 */
public class FeaturePacksReferencingSamePluginTestCase extends PmProvisionConfigTestBase {

    public static class Plugin1 extends BasicFileWritingPlugin {

        @Override
        protected String getBasePath() {
            return "plugin";
        }

        @Override
        protected String getContent() {
            return "plugin1";
        }
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
                .newFeaturePack(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final"))
                    .addDependency(FeaturePackConfig.forLocation(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "2", "2.0.0.Final").getLocation()))
                    .newPackage("p1", true)
                        .writeContent("fp1/p1.txt", "p1")
                        .getFeaturePack()
                    .addPlugin(Plugin1.class)
                    .getCreator()
                .newFeaturePack(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "2", "2.0.0.Final"))
                    .newPackage("p1", true)
                        .writeContent("fp2/p1.txt", "p1")
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
                .addFeaturePack(ProvisionedFeaturePack.builder(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "2", "2.0.0.Final"))
                        .addPackage("p1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final"))
                        .addPackage("p1")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1/p1.txt", "p1")
                .addFile("fp2/p1.txt", "p1")
                .addFile("plugin", "plugin1")
                .build();
    }
}
