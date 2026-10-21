/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.config.feature.param.type.list;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.runtime.ResolvedSpecId;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.PmInstallFeaturePackTestBase;

/**
 *
 * @author Alexey Loubyansky
 */
public class IncompleteListFeatureParameterTestCase extends PmInstallFeaturePackTestBase {

    private static final FPID FP_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(FP_GAV)
            .addFeatureSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addParam(FeatureParameterSpec.builder("my-list").setType("List").build())
                    .build())
            .addConfig(ConfigModel.builder()
                    .setName("main")
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("name", "a1")
                            .setParam("my-list", "[c,d"))
                    .build());
    }

    @Override
    protected FeaturePackConfig featurePackConfig() {
        return FeaturePackConfig.forLocation(FP_GAV.getLocation());
    }

    @Override
    protected String[] pmErrors() throws ProvisioningException {
        return new String[] {Errors.failedToResolveConfigSpec(null, "main"),
                Errors.failedToProcess(FP_GAV, new FeatureConfig("specA").setParam("name", "a1").setParam("my-list", "[c,d")),
                Errors.failedToResolveParameter(new ResolvedSpecId(FP_GAV.getProducer(), "specA"), "my-list", "[c,d"),
                "Failed to parse List parameter value",
                "Parsing of '[c,d' failed while parsing format List<?> started on index 0",
                "Format List<?> is incomplete"};
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return null;
    }
}
