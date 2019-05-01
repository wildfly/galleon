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
package org.jboss.galleon.installation.universe.names;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.PmInstallFeaturePackTestBase;
import org.jboss.galleon.test.util.fs.state.DirState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;

/**
 *
 * @author Alexey Loubyansky
 */
public class InstallFpUsingUniverseNameTestCase extends PmInstallFeaturePackTestBase {

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator.newFeaturePack(FeaturePackLocation.fromString("galleon.test:fp1@galleon1:1#1.0.0.Final").getFPID())
            .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "fp1 p1")
            .getFeaturePack()
        .getCreator()
        .install();
    }

    @Override
    protected ProvisioningManager getPm() throws ProvisioningException {
        ProvisioningManager pm = super.getPm();
        pm.addUniverse("custom", LegacyGalleon1Universe.getUniverseSpec());
        return pm;
    }

    @Override
    protected FeaturePackConfig featurePackConfig() throws ProvisioningDescriptionException {
        return FeaturePackConfig.forLocation(FeaturePackLocation.fromString("galleon.test:fp1@custom:1#1.0.0.Final"));
    }

    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder()
                .addUniverse("custom", "galleon1", null)
                .addFeaturePackDep(FeaturePackConfig.forLocation(FeaturePackLocation.fromString("galleon.test:fp1@galleon1:1#1.0.0.Final")))
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(FeaturePackLocation.fromString("galleon.test:fp1@galleon1:1#1.0.0.Final").getFPID())
                        .addPackage("p1")
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1/p1.txt", "fp1 p1")
                .build();
    }
}
