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
package org.jboss.galleon.featurepack.stability;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.test.PmProvisionConfigTestBase;

/**
 *
 * @author jfdenise
 */
public abstract class AbstractFpStatibilityTestCase extends PmProvisionConfigTestBase {

    static final FPID FP1_100_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");
    static final FPID FP2_100_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "1", "1.0.0.Final");

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
            .newFeaturePack(FP1_100_GAV).setConfigStability("default").setPackageStability("default")
                .newPackage("fp1_1default", true).setStability("default")
                    .getFeaturePack()
                .newPackage("fp1_2noStability", true)
                    .getFeaturePack()
                .newPackage("fp1_3experimental", true).setStability("experimental")
                    .getFeaturePack().getCreator()
            .newFeaturePack(FP2_100_GAV).setConfigStability("experimental").setPackageStability("experimental")
                .addDependency(FP1_100_GAV.getLocation())
                .newPackage("fp2_1default", true).setStability("default")
                    .getFeaturePack()
                .newPackage("fp2_2noStability", true)
                    .getFeaturePack()
                .newPackage("fp2_3community", true).setStability("community")
                    .getFeaturePack()
                .newPackage("fp2_4preview", true).setStability("preview")
                    .getFeaturePack()
                .newPackage("fp2_5experimental", true).setStability("experimental")
                    .getFeaturePack();
    }
}
