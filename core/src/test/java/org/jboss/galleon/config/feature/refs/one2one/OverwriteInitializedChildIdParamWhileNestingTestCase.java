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
package org.jboss.galleon.config.feature.refs.one2one;

import java.util.HashMap;
import java.util.Map;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ArtifactCoords.Gav;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeatureGroup;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.repomanager.FeaturePackRepositoryManager;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.runtime.ResolvedSpecId;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureReferenceSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.test.PmInstallFeaturePackTestBase;
import org.junit.Assert;

/**
 * An id param may have a default value but it can be initialized to a different one.
 * It cannot be overwritten afterwards.
 *
 * @author Alexey Loubyansky
 */
public class OverwriteInitializedChildIdParamWhileNestingTestCase extends PmInstallFeaturePackTestBase {

    private static final Gav FP_GAV = ArtifactCoords.newGav("org.jboss.pm.test", "fp1", "1.0.0.Final");

    @Override
    protected void setupRepo(FeaturePackRepositoryManager repoManager) throws ProvisioningDescriptionException {
        repoManager.installer()
        .newFeaturePack(FP_GAV)
            .addSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("id"))
                    .addParam(FeatureParameterSpec.create("p1", "spec"))
                    .build())
            .addSpec(FeatureSpec.builder("specC")
                    .addFeatureRef(FeatureReferenceSpec.builder("specA").mapParam("a", "id").build())
                    .addParam(FeatureParameterSpec.createId("id"))
                    .addParam(FeatureParameterSpec.create("a", true, false, "def"))
                    .build())
            .addFeatureGroup(FeatureGroup.builder("groupC")
                    .addFeature(
                            new FeatureConfig("specC")
                            .setParam("id", "c1")
                            .setParam("a", "a2"))
                    .addFeature(
                            new FeatureConfig("specC")
                            .setParam("id", "c2"))
                    .build())
            .addConfig(ConfigModel.builder()
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("id", "a1")
                            .addFeatureGroup(FeatureGroup.forGroup("groupC")))
                    .build())
            .getInstaller()
        .install();
    }

    @Override
    protected FeaturePackConfig featurePackConfig() {
        return FeaturePackConfig.forGav(FP_GAV);
    }

    @Override
    protected void pmSuccess() {
        Assert.fail("There should be an id param conflict");
    }

    @Override
    protected void pmFailure(Throwable e) throws ProvisioningDescriptionException {
        Assert.assertEquals("Failed to resolve config", e.getMessage());
        e = (ProvisioningException) e.getCause();
        Assert.assertNotNull(e);
        Assert.assertEquals(Errors.failedToProcess(FP_GAV,
                new FeatureConfig("specA")
                            .setParam("id", "a1")
                            .addFeatureGroup(FeatureGroup.forGroup("groupC"))), e.getLocalizedMessage());
        e = (ProvisioningException) e.getCause();
        Assert.assertNotNull(e);
        Assert.assertEquals(Errors.failedToProcess(FP_GAV, "groupC"), e.getLocalizedMessage());
        e = (ProvisioningException) e.getCause();
        Assert.assertNotNull(e);
        Assert.assertEquals(Errors.failedToProcess(FP_GAV,
                new FeatureConfig("specC")
                .setParam("id", "c1")
                .setParam("a", "a2")), e.getLocalizedMessage());
        e = (ProvisioningException) e.getCause();
        Assert.assertNotNull(e);
        final Map<String, String> params = new HashMap<>();
        params.put("id", "c1");
        params.put("a", "a2");
        Assert.assertEquals(Errors.failedToInitializeForeignKeyParams(new ResolvedSpecId(FP_GAV, "specC"), ResolvedFeatureId.create(new ResolvedSpecId(FP_GAV, "specA"), "id", "a1"), params), e.getLocalizedMessage());
        e = (ProvisioningException) e.getCause();
        Assert.assertNotNull(e);
        Assert.assertEquals(Errors.idParamForeignKeyInitConflict(new ResolvedSpecId(FP_GAV, "specC"), "a", "a2", "a1"), e.getLocalizedMessage());
    }
}
