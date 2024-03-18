/*
 * Copyright 2016-2024 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.config.feature.stability;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.test.PmProvisionConfigTestBase;

/**
 *
 * @author jfdenise
 */
public abstract class AbstractFeatureStabilityTestCase extends PmProvisionConfigTestBase {

    static final FPID FP1_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");
    static final FPID FP2_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "1", "1.0.0.Final");

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
            .newFeaturePack(FP1_GAV).setConfigStability("experimental").setPackageStability("experimental")
                .addFeatureSpec(FeatureSpec.builder("specNoStability")
                        .addParam(FeatureParameterSpec.createId("id"))
                        .build())
                .addFeatureSpec(FeatureSpec.builder("specExperimental")
                        .addParam(FeatureParameterSpec.createId("id"))
                        .addParam(FeatureParameterSpec.builder("idDefault").setStability("default").build())
                        .addParam(FeatureParameterSpec.builder("idCommunity").setStability("community").build())
                        .addParam(FeatureParameterSpec.builder("idPreview").setStability("preview").build())
                        .addParam(FeatureParameterSpec.builder("idExperimental").setStability("experimental").build())
                        .setStability("experimental")
                        .build())
                .addFeatureSpec(FeatureSpec.builder("specPreview")
                        .addParam(FeatureParameterSpec.createId("id"))
                        .addParam(FeatureParameterSpec.builder("idDefault").setStability("default").build())
                        .addParam(FeatureParameterSpec.builder("idCommunity").setStability("community").build())
                        .addParam(FeatureParameterSpec.builder("idPreview").setStability("preview").build())
                        .addParam(FeatureParameterSpec.builder("idExperimental").setNillable().setStability("experimental").build())
                        .setStability("preview")
                        .build())
                .addFeatureSpec(FeatureSpec.builder("specCommunity")
                        .addParam(FeatureParameterSpec.createId("id"))
                        .addParam(FeatureParameterSpec.builder("idDefault").setStability("default").build())
                        .addParam(FeatureParameterSpec.builder("idCommunity").setStability("community").build())
                        .addParam(FeatureParameterSpec.builder("idPreview").setNillable().setStability("preview").build())
                        .setStability("community")
                        .build())
                .addFeatureSpec(FeatureSpec.builder("specDefault")
                        .addParam(FeatureParameterSpec.createId("id"))
                        .addParam(FeatureParameterSpec.builder("idDefault").setStability("default").build())
                        .addParam(FeatureParameterSpec.builder("idCommunity").setNillable().setStability("community").build())
                        .setStability("default")
                        .build())
                .addConfig(ConfigModel.builder(null, "configA")
                        .addFeature(new FeatureConfig("specNoStability").setParam("id", "1"))
                        .addFeature(new FeatureConfig("specExperimental").setParam("id", "1").setParam("idDefault", "1").setParam("idCommunity", "1").setParam("idPreview", "1").setParam("idExperimental", "1"))
                        .addFeature(new FeatureConfig("specPreview").setParam("id", "1").setParam("idDefault", "1").setParam("idCommunity", "1").setParam("idPreview", "1").setParam("idExperimental", "1"))
                        .addFeature(new FeatureConfig("specCommunity").setParam("id", "1").setParam("idDefault", "1").setParam("idCommunity", "1").setParam("idPreview", "1"))
                        .addFeature(new FeatureConfig("specDefault").setParam("id", "1").setParam("idCommunity", "1").setParam("idDefault", "1"))
                        .build())
                .newPackage("p", true)
                .getFeaturePack()
                .newPackage("pDefault", true).setStability("default")
                .getFeaturePack()
                .newPackage("pCommunity", true).setStability("community")
                .getFeaturePack()
                .newPackage("pPreview", true).setStability("preview")
                .getFeaturePack()
                .newPackage("pExperimental", true).setStability("experimental")
                .getFeaturePack().getCreator()
                .newFeaturePack(FP2_GAV).setConfigStability("default").setPackageStability("default")
                .addDependency(FP1_GAV.getLocation())
                .addFeatureSpec(FeatureSpec.builder("specNoStability")
                        .addParam(FeatureParameterSpec.createId("id"))
                        .build())
                .addFeatureSpec(FeatureSpec.builder("specExperimental")
                        .addParam(FeatureParameterSpec.createId("id"))
                        .addParam(FeatureParameterSpec.builder("idDefault").setStability("default").build())
                        .addParam(FeatureParameterSpec.builder("idCommunity").setStability("community").build())
                        .addParam(FeatureParameterSpec.builder("idPreview").setStability("preview").build())
                        .addParam(FeatureParameterSpec.builder("idExperimental").setStability("experimental").build())
                        .setStability("experimental")
                        .build())
                .addFeatureSpec(FeatureSpec.builder("specPreview")
                        .addParam(FeatureParameterSpec.createId("id"))
                        .addParam(FeatureParameterSpec.builder("idDefault").setStability("default").build())
                        .addParam(FeatureParameterSpec.builder("idCommunity").setStability("community").build())
                        .addParam(FeatureParameterSpec.builder("idPreview").setStability("preview").build())
                        .addParam(FeatureParameterSpec.builder("idExperimental").setNillable().setStability("experimental").build())
                        .setStability("preview")
                        .build())
                .addFeatureSpec(FeatureSpec.builder("specCommunity")
                        .addParam(FeatureParameterSpec.createId("id"))
                        .addParam(FeatureParameterSpec.builder("idDefault").setStability("default").build())
                        .addParam(FeatureParameterSpec.builder("idCommunity").setStability("community").build())
                        .addParam(FeatureParameterSpec.builder("idPreview").setNillable().setStability("preview").build())
                        .setStability("community")
                        .build())
                .addFeatureSpec(FeatureSpec.builder("specDefault")
                        .addParam(FeatureParameterSpec.createId("id"))
                        .addParam(FeatureParameterSpec.builder("idDefault").setStability("default").build())
                        .addParam(FeatureParameterSpec.builder("idCommunity").setNillable().setStability("community").build())
                        .setStability("default")
                        .build())
                .addConfig(ConfigModel.builder(null, "configB")
                        .addFeature(new FeatureConfig("specNoStability").setParam("id", "1"))
                        .addFeature(new FeatureConfig("specExperimental").setParam("id", "1").setParam("idDefault", "1").setParam("idCommunity", "1").setParam("idPreview", "1").setParam("idExperimental", "1"))
                        .addFeature(new FeatureConfig("specPreview").setParam("id", "1").setParam("idDefault", "1").setParam("idCommunity", "1").setParam("idPreview", "1").setParam("idExperimental", "1"))
                        .addFeature(new FeatureConfig("specCommunity").setParam("id", "1").setParam("idDefault", "1").setParam("idCommunity", "1").setParam("idPreview", "1"))
                        .addFeature(new FeatureConfig("specDefault").setParam("id", "1").setParam("idCommunity", "1").setParam("idDefault", "1"))
                        .build())
                .newPackage("p", true)
                .getFeaturePack()
                .newPackage("pDefault", true).setStability("default")
                .getFeaturePack()
                .newPackage("pCommunity", true).setStability("community")
                .getFeaturePack()
                .newPackage("pPreview", true).setStability("preview")
                .getFeaturePack()
                .newPackage("pExperimental", true).setStability("experimental")
                .getFeaturePack();
    }
}
