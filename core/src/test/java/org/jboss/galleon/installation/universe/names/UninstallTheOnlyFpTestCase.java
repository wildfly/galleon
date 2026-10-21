/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.installation.universe.names;

import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.PmUninstallFeaturePackTestBase;
import org.jboss.galleon.test.util.fs.state.DirState;

/**
 *
 * @author Alexey Loubyansky
 */
public class UninstallTheOnlyFpTestCase extends PmUninstallFeaturePackTestBase {

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator.newFeaturePack(FeaturePackLocation.fromString("galleon.test:fp1@galleon1:1#1.0.0.Final").getFPID())
            .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "fp1 p1")
            .getFeaturePack();
    }

    @Override
    protected ProvisioningConfig initialState() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addUniverse("custom", "galleon1", null)
                .addFeaturePackDep(FeaturePackConfig.forLocation(FeaturePackLocation.fromString("galleon.test:fp1@custom:1#1.0.0.Final")))
                .build();
    }

    @Override
    protected FPID uninstallGav() throws ProvisioningDescriptionException {
        return FeaturePackLocation.fromString("galleon.test:fp1@custom:1#1.0.0.Final").getFPID();
    }

    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder()
                .addUniverse("custom", "galleon1", null)
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() {
        return ProvisionedState.builder().build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return this.newDirBuilder().build();
    }
}
