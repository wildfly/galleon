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

package org.jboss.galleon.defchannel.test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.ProvisionFromUniverseTestBase;

/**
 *
 * @author Alexey Loubyansky
 */
public class TransitiveDepWithDefaultChannelVersionConflictTestCase extends ProvisionFromUniverseTestBase {

    private FeaturePackLocation prod1_1;
    private FeaturePackLocation prod1_2;
    private FeaturePackLocation prod1_3;

    private FeaturePackLocation prod2_1;
    private FeaturePackLocation prod2_2;
    private FeaturePackLocation prod2_3;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1", 2, 3);
        universe.createProducer("prod2", 2, 3);
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {

        prod1_1 = newFpl("prod1", "1", "1.0.0.Final");
        prod1_2 = newFpl("prod1", "2", "2.0.0.Final");
        prod1_3 = newFpl("prod1", "3", "3.0.0.Final");

        prod2_1 = newFpl("prod2", "1", "1.0.0.Final");
        prod2_2 = newFpl("prod2", "2", "2.0.0.Final");
        prod2_3 = newFpl("prod2", "3", "3.0.0.Final");

        creator.newFeaturePack()
            .setFPID(prod1_1.getFPID());

        creator.newFeaturePack()
            .setFPID(prod1_2.getFPID())
            .addDependency(prod2_1);

        creator.newFeaturePack()
            .setFPID(prod1_3.getFPID());

        creator.newFeaturePack()
            .setFPID(prod2_1.getFPID());

        creator.newFeaturePack()
            .setFPID(prod2_2.getFPID());

        creator.newFeaturePack()
            .setFPID(prod2_3.getFPID());

        creator.install();
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addTransitiveDep(newFpl("prod2", "2"))
                .addFeaturePackDep(FeaturePackConfig.forLocation(newProducerFpl("prod1")))
                .build();
    }

    @Override
    protected String[] pmErrors() throws ProvisioningException {
        final Set<FPID> set = new HashSet<>();
        set.add(newFpl("prod2", "2").getFPID());
        set.add(prod2_1.getFPID());
        final List<Set<FPID>> conflicts = Collections.singletonList(set);
        return new String[] {
                Errors.fpVersionCheckFailed(conflicts)
        };
    }
}