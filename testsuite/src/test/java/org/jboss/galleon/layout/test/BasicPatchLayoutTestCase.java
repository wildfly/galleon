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
package org.jboss.galleon.layout.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.layout.ProvisioningLayout;
import org.jboss.galleon.layout.FeaturePackLayout;
import org.jboss.galleon.layout.LayoutTestBase;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;

/**
 *
 * @author Alexey Loubyansky
 */
public class BasicPatchLayoutTestCase extends LayoutTestBase {

    private FeaturePackLocation fp1;
    private FeaturePackLocation fp1Patch1;
    private FeaturePackLocation fp1Patch2;
    private FeaturePackLocation fp2;
    private FeaturePackLocation fp2Patch1;
    private FeaturePackLocation fp2Patch2;
    private FeaturePackLocation fp3;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("prod1");
        universe.createProducer("prod2");
        universe.createProducer("prod3");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningDescriptionException {
        fp1 = newFpl("prod1", "1", "1.0.0.Final");
        creator.newFeaturePack(fp1.getFPID());

        fp1Patch1 = newFpl("prod1", "1", "1.0.0.Patch1.Final");
        creator.newFeaturePack(fp1Patch1.getFPID()).setPatchFor(fp1.getFPID());

        fp1Patch2 = newFpl("prod1", "1", "1.0.0.Patch2.Final");
        creator.newFeaturePack(fp1Patch2.getFPID()).setPatchFor(fp1.getFPID());

        fp2 = newFpl("prod2", "1", "1.0.0.Final");
        creator.newFeaturePack(fp2.getFPID()).addDependency(fp1);

        fp2Patch1 = newFpl("prod2", "1", "1.0.0.Patch1.Final");
        creator.newFeaturePack(fp2Patch1.getFPID()).setPatchFor(fp2.getFPID());

        fp2Patch2 = newFpl("prod2", "1", "1.0.0.Patch2.Final");
        creator.newFeaturePack(fp2Patch2.getFPID()).setPatchFor(fp2.getFPID()).addDependency(fp2Patch1);

        fp3 = newFpl("prod3", "1", "1.0.0.Final");
        creator.newFeaturePack(fp3.getFPID());
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        final ProvisioningConfig config = ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(fp2)
                        .addPatch(fp2Patch2.getFPID())
                        .build())
                .addFeaturePackDep(fp3)
                .addFeaturePackDep(FeaturePackConfig.transitiveBuilder(fp1)
                        .addPatch(fp1Patch1.getFPID())
                        .addPatch(fp1Patch2.getFPID())
                        .build())
                .build();
        return config;
    }

    @Override
    protected void assertLayout(ProvisioningLayout<FeaturePackLayout> layout) throws Exception {
        final List<FeaturePackLayout> featurePacks = layout.getOrderedFeaturePacks();
        assertEquals(3, featurePacks.size());
        FeaturePackLayout fp = featurePacks.get(0);
        assertEquals(fp1.getFPID(), fp.getFPID());
        assertTrue(fp.isTransitiveDep());

        fp = featurePacks.get(1);
        assertEquals(fp2.getFPID(), fp.getFPID());
        assertTrue(fp.isDirectDep());

        fp = featurePacks.get(2);
        assertEquals(fp3.getFPID(), fp.getFPID());
        assertTrue(fp.isDirectDep());

        List<FeaturePackLayout> patches = layout.getPatches(fp1.getFPID());
        assertEquals(2, patches.size());
        fp = patches.get(0);
        assertEquals(fp1Patch1.getFPID(), fp.getFPID());
        assertTrue(fp.isPatch());

        fp = patches.get(1);
        assertEquals(fp1Patch2.getFPID(), fp.getFPID());
        assertTrue(fp.isPatch());

        patches = layout.getPatches(fp2.getFPID());
        assertEquals(2, patches.size());
        assertEquals(fp2Patch1.getFPID(), patches.get(0).getFPID());
        assertEquals(fp2Patch2.getFPID(), patches.get(1).getFPID());

        patches = layout.getPatches(fp3.getFPID());
        assertEquals(0, patches.size());
    }
}
