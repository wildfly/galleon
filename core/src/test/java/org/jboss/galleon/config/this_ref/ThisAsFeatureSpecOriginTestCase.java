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
package org.jboss.galleon.config.this_ref;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ArtifactCoords.Gav;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeatureGroup;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.repomanager.FeaturePackRepositoryManager;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureReferenceSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.PmProvisionConfigTestBase;
import org.jboss.galleon.xml.ProvisionedConfigBuilder;
import org.jboss.galleon.xml.ProvisionedFeatureBuilder;

/**
 *
 * @author Alexey Loubyansky
 */
public class ThisAsFeatureSpecOriginTestCase extends PmProvisionConfigTestBase {

    private static final Gav FP1_GAV = ArtifactCoords.newGav("org.jboss.pm.test", "fp1", "1.0.0.Final");
    private static final Gav FP2_GAV = ArtifactCoords.newGav("org.jboss.pm.test", "fp2", "1.0.0.Final");

    @Override
    protected void setupRepo(FeaturePackRepositoryManager repoManager) throws ProvisioningDescriptionException {
        repoManager.installer()
        .newFeaturePack(FP1_GAV)
            .addDependency("fp2", FP2_GAV)
            .addSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("a"))
                    .addParam(FeatureParameterSpec.create("p1", true))
                    .build())
            .addSpec(FeatureSpec.builder("specB")
                    .addFeatureRef(FeatureReferenceSpec.builder("specD").setOrigin("fp2").build())
                    .addParam(FeatureParameterSpec.createId("b"))
                    .addParam(FeatureParameterSpec.createId("d"))
                    .addParam(FeatureParameterSpec.create("p1", true))
                    .build())
            .addFeatureGroup(FeatureGroup.builder("fg1")
                    .addFeature(new FeatureConfig("specD")
                            .setOrigin("fp2")
                            .setParam("d", "dOne")
                            .addFeature(new FeatureConfig("specB")
                                    .setOrigin("this")
                                    .setParam("b", "bOne")))
                    .build()).getInstaller()
        .newFeaturePack(FP2_GAV)
            .addDependency("fp1", FP1_GAV)
            .addSpec(FeatureSpec.builder("specC")
                    .addFeatureRef(FeatureReferenceSpec.builder("specA").setOrigin("fp1").build())
                    .addParam(FeatureParameterSpec.createId("a"))
                    .addParam(FeatureParameterSpec.createId("c"))
                    .addParam(FeatureParameterSpec.create("p1", true))
                    .build())
            .addSpec(FeatureSpec.builder("specD")
                    .addParam(FeatureParameterSpec.createId("d"))
                    .addParam(FeatureParameterSpec.create("p1", true))
                    .build())
            .addFeatureGroup(FeatureGroup.builder("fg2")
                    .addFeature(new FeatureConfig("specA")
                            .setOrigin("fp1")
                            .setParam("a", "aOne")
                            .addFeature(new FeatureConfig("specC")
                                    .setOrigin("this")
                                    .setParam("c", "cOne")))
                    .build())
            .addConfig(ConfigModel.builder()
                    .setProperty("prop1", "value1")
                    .setProperty("prop2", "value2")
                    .addFeatureGroup(FeatureGroup.forGroup("fp1", "fg1"))
                    .addFeatureGroup(FeatureGroup.forGroup("fg2"))
                    .build())
            .getInstaller()
        .install();
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FP2_GAV)
                .build();
    }

    /* (non-Javadoc)
     * @see org.jboss.galleon.test.PmMethodTestBase#provisionedState()
     */
    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(FP1_GAV).build())
                .addFeaturePack(ProvisionedFeaturePack.builder(FP2_GAV).build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setProperty("prop1", "value1")
                        .setProperty("prop2", "value2")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.builder(FP2_GAV, "specD").setParam("d", "dOne").build())
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.builder(FP1_GAV, "specB")
                                .setParam("d", "dOne").setParam("b", "bOne").build())
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP1_GAV, "specA", "a", "aOne"))
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.builder(FP2_GAV, "specC")
                                .setParam("a", "aOne").setParam("c", "cOne").build())
                                .build())
                        .build())
                .build();
    }
}
