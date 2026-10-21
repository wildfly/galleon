/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.installation.home.test;

import org.jboss.galleon.BaseErrors;
import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.test.PmProvisionConfigTestBase;
import org.jboss.galleon.util.IoUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class NotUsableHomeDirContainingFileTestCase extends PmProvisionConfigTestBase {

    private static final FPID FP1_100_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");

    @Override
    protected void doBefore() throws Exception {
        IoUtils.writeFile(installHome.resolve("some.file"), "some content");
        super.doBefore();
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
            .newFeaturePack(FP1_100_GAV)
                .newPackage("p1", true)
                    .writeContent("fp1/p1.txt", "fp1 1.0.0.Final p1")
                    .getFeaturePack();
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder().addFeaturePackDep(FP1_100_GAV.getLocation()).build();
    }

    @Override
    protected String[] pmErrors() {
        return new String[] {
                BaseErrors.homeDirNotUsable(installHome)
        };
    }
}
