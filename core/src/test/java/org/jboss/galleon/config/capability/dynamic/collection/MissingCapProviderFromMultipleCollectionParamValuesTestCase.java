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
package org.jboss.galleon.config.capability.dynamic.collection;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ArtifactCoords.Gav;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.repomanager.FeaturePackRepositoryManager;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.PmInstallFeaturePackTestBase;

/**
 *
 * @author Alexey Loubyansky
 */
public class MissingCapProviderFromMultipleCollectionParamValuesTestCase extends PmInstallFeaturePackTestBase {

    private static final Gav FP_GAV = ArtifactCoords.newGav("org.jboss.pm.test", "fp1", "1.0.0.Final");


    @Override
    protected void setupRepo(FeaturePackRepositoryManager repoManager) throws ProvisioningDescriptionException {
        repoManager.installer()
        .newFeaturePack(FP_GAV)
            .addSpec(FeatureSpec.builder("specA")
                    .providesCapability("$a.$p")
                    .addParam(FeatureParameterSpec.createId("a"))
                    .addParam(FeatureParameterSpec.createId("p"))
                    .build())
            .addSpec(FeatureSpec.builder("specB")
                    .requiresCapability("$p1.$p2")
                    .addParam(FeatureParameterSpec.createId("b"))
                    .addParam(FeatureParameterSpec.builder("p1").setType("List<String>").build())
                    .addParam(FeatureParameterSpec.builder("p2").setType("List<String>").build())
                    .build())
            .addConfig(ConfigModel.builder()
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("b", "b1")
                            .setParam("p1", "[ 1 , 2 ]")
                            .setParam("p2", "[a,b]"))
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("a", "1")
                            .setParam("p", "a"))
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("a", "1")
                            .setParam("p", "b"))
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("a", "2")
                            .setParam("p", "a"))
                    .build())
            .getInstaller()
        .install();
    }

    @Override
    protected FeaturePackConfig featurePackConfig() {
        return FeaturePackConfig.forGav(FP_GAV);
    }

    @Override
    protected String[] pmErrors() {
        return new String[] {
                Errors.failedToBuildConfigSpec(null, null),
                "No provider found for capability 2.b required by org.jboss.pm.test:fp1:1.0.0.Final#specB:b=b1 as $p1.$p2"
        };
    }
    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return null;
    }
}
