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
package org.jboss.galleon.provision.config.layers.test;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.spec.ConfigLayerSpec;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.spec.PackageDependencySpec;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.ProvisionFromUniverseTestBase;
import org.jboss.galleon.xml.ProvisionedConfigBuilder;
import org.jboss.galleon.xml.ProvisionedFeatureBuilder;

/**
 *
 * @author Alexey Loubyansky
 */
public class ExcludeOptionalPackageFromConfigLayerTestCase extends ProvisionFromUniverseTestBase {

    private FeaturePackLocation prod1;
    private FeaturePackLocation prod2;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1");
        universe.createProducer("prod2");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {

        prod1 = newFpl("prod1", "1", "1.0.0.Final");
        prod2 = newFpl("prod2", "1", "1.0.0.Final");

        creator.newFeaturePack()
            .setFPID(prod1.getFPID())
            .addFeatureSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("id"))
                    .addPackageDep("prod1.specA")
                    .addPackageDep(PackageDependencySpec.optional("prod1.specA.optional"))
                    .build())
            .addConfigLayer(ConfigLayerSpec.builder()
                    .setModel("model1").setName("base")
                    .addFeature(new FeatureConfig("specA").setParam("id", "1"))
                    .addPackageDep("prod1.layer.base")
                    .addPackageDep(PackageDependencySpec.optional("prod1.layer.base.optional"))
                    .build())
            .newPackage("prod1.layer.base")
                .getFeaturePack()
            .newPackage("prod1.layer.base.optional")
                .getFeaturePack()
            .newPackage("prod1", true)
                .addDependency(PackageDependencySpec.optional("prod1.optional"))
                .getFeaturePack()
            .newPackage("prod1.optional")
                .getFeaturePack()
            .newPackage("prod1.specA")
                .getFeaturePack()
            .newPackage("prod1.specA.optional", true);

        creator.newFeaturePack()
            .setFPID(prod2.getFPID())
            .addDependency(prod1)
            .addFeatureSpec(FeatureSpec.builder("specB")
                .addParam(FeatureParameterSpec.createId("id"))
                .addPackageDep("prod2.specB")
                .addPackageDep(PackageDependencySpec.optional("prod2.specB.optional"))
                .build())
            .addConfigLayer(ConfigLayerSpec.builder()
                .setModel("model1").setName("base")
                .addFeature(new FeatureConfig("specB").setParam("id", "1"))
                .addPackageDep("prod2.layer.base")
                .addPackageDep(PackageDependencySpec.optional("prod2.layer.base.optional"))
                .build())
            .newPackage("prod2.layer.base")
                .getFeaturePack()
            .newPackage("prod2.layer.base.optional")
                .getFeaturePack()
            .newPackage("prod2", true)
                .addDependency(PackageDependencySpec.optional("prod2.optional"))
                .getFeaturePack()
            .newPackage("prod2.optional")
                .getFeaturePack()
            .newPackage("prod2.specB")
                .getFeaturePack()
            .newPackage("prod2.specB.optional", true);

    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.transitiveBuilder(prod1)
                        .excludePackage("prod1.optional")
                        .excludePackage("prod1.layer.base.optional")
                        .excludePackage("prod1.specA.optional")
                        .build())
                .addFeaturePackDep(FeaturePackConfig.builder(prod2)
                        .excludePackage("prod2.optional")
                        .excludePackage("prod2.layer.base.optional")
                        .excludePackage("prod2.specB.optional")
                        .build())
                .addConfig(ConfigModel.builder("model1", "name1")
                        .includeLayer("base")
                        .build())
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(prod1.getFPID())
                        .addPackage("prod1.layer.base")
                        .addPackage("prod1.specA")
                        .addPackage("prod1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(prod2.getFPID())
                        .addPackage("prod2.layer.base")
                        .addPackage("prod2.specB")
                        .addPackage("prod2")
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setModel("model1")
                        .setName("name1")
                        .addLayer("model1", "base")
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.create(prod1.getProducer(), "specA", "id", "1"))
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.create(prod2.getProducer(), "specB", "id", "1"))
                                .build())
                        .build())
                .build();
    }
}