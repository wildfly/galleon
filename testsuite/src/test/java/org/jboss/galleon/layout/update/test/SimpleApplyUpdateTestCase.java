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

package org.jboss.galleon.layout.update.test;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.layout.ProvisioningLayout;
import org.jboss.galleon.layout.ProvisioningPlan;
import org.jboss.galleon.layout.FeaturePackLayout;
import org.jboss.galleon.layout.FeaturePackUpdatePlan;
import org.jboss.galleon.layout.LayoutPlanTestBase;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.MvnUniverse;

/**
 *
 * @author Alexey Loubyansky
 */
public class SimpleApplyUpdateTestCase extends LayoutPlanTestBase {

    private FeaturePackLocation a100;
    private FeaturePackLocation a100Patch1;
    private FeaturePackLocation a101;
    private FeaturePackLocation a101Patch1;

    private FeaturePackLocation b200;
    private FeaturePackLocation b200Patch1;

    private FeaturePackLocation c300;
    private FeaturePackLocation c301;
    private FeaturePackLocation c301Patch1;
    private FeaturePackLocation c301Patch2;

    private FeaturePackLocation d400;
    private FeaturePackLocation d400Patch1;
    private FeaturePackLocation d400Patch2;

    private FeaturePackLocation e500;
    private FeaturePackLocation e501;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("a");
        universe.createProducer("b");
        universe.createProducer("c");
        universe.createProducer("d");
        universe.createProducer("e");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningDescriptionException {
        a100 = newFpl("a", "1", "1.0.0.Final");
        creator.newFeaturePack(a100.getFPID());
        a100Patch1 = newFpl("a", "1", "1.0.0.Patch1");
        creator.newFeaturePack(a100Patch1.getFPID()).setPatchFor(a100.getFPID());
        a101 = newFpl("a", "1", "1.0.1.Final");
        creator.newFeaturePack(a101.getFPID());
        a101Patch1 = newFpl("a", "1", "1.0.1.Patch1");
        creator.newFeaturePack(a101Patch1.getFPID()).setPatchFor(a101.getFPID());

        b200 = newFpl("b", "1", "1.0.0.Final");
        creator.newFeaturePack(b200.getFPID());
        b200Patch1 = newFpl("b", "1", "1.0.0.Patch1");
        creator.newFeaturePack(b200Patch1.getFPID()).setPatchFor(b200.getFPID());

        c300 = newFpl("c", "1", "1.0.0.Final");
        creator.newFeaturePack(c300.getFPID()).addDependency(b200);
        c301 = newFpl("c", "1", "1.0.1.Final");
        creator.newFeaturePack(c301.getFPID()).addDependency(a100);
        c301Patch1 = newFpl("c", "1", "1.0.1.Patch1");
        creator.newFeaturePack(c301Patch1.getFPID()).setPatchFor(c301.getFPID());
        c301Patch2 = newFpl("c", "1", "1.0.1.Patch2");
        creator.newFeaturePack(c301Patch2.getFPID()).setPatchFor(c301.getFPID()).addDependency(c301Patch1);

        d400 = newFpl("d", "1", "1.0.0.Final");
        creator.newFeaturePack(d400.getFPID());
        d400Patch1 = newFpl("d", "1", "1.0.0.Patch1");
        creator.newFeaturePack(d400Patch1.getFPID()).setPatchFor(d400.getFPID());
        d400Patch2 = newFpl("d", "1", "1.0.0.Patch2");
        creator.newFeaturePack(d400Patch2.getFPID()).setPatchFor(d400.getFPID());

        e500 = newFpl("e", "1", "1.0.0.Final");
        creator.newFeaturePack(e500.getFPID())
        .addDependency(c300)
        .addDependency(d400);
        e501 = newFpl("e", "1", "1.0.1.Final");
        creator.newFeaturePack(e501.getFPID())
        .addDependency(c301)
        .addDependency(d400);

    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(e500)
                .addFeaturePackDep(FeaturePackConfig.transitiveBuilder(FeaturePackLocation.fromString(d400.getProducer().toString()))
                        .addPatch(d400Patch1.getFPID())
                        .build())
                .build();
    }

    @Override
    protected FPID[] expectedInitialOrder() {
        return new FPID[] {b200.getFPID(), c300.getFPID(), d400.getFPID(), e500.getFPID()};
    }

    @Override
    protected ProvisioningPlan getPlan() throws ProvisioningDescriptionException {
        return ProvisioningPlan.builder()
                .update(FeaturePackUpdatePlan.request(e500).setNewLocation(e501).buildPlan())
                .update(FeaturePackUpdatePlan.request(d400).addNewPatch(d400Patch2.getFPID()).buildPlan())
                .update(FeaturePackUpdatePlan.request(c300).setNewLocation(c301).addNewPatch(c301Patch2.getFPID()).buildPlan())
                .update(FeaturePackUpdatePlan.request(b200).addNewPatch(b200Patch1.getFPID()).buildPlan());
    }

    @Override
    protected FPID[] expectedOrder() {
        return new FPID[] {a100.getFPID(), c301.getFPID(), d400.getFPID(), e501.getFPID()};
    }

    @Override
    protected ProvisioningConfig expectedLayoutConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(e501)
                .addFeaturePackDep(FeaturePackConfig.transitiveBuilder(d400)
                        .addPatch(d400Patch1.getFPID())
                        .addPatch(d400Patch2.getFPID())
                        .build())
                .addFeaturePackDep(FeaturePackConfig.transitiveBuilder(c301)
                        .addPatch(c301Patch2.getFPID())
                        .build())
                .build();
    }

    @Override
    protected void assertFeaturePacks(ProvisioningLayout<FeaturePackLayout> layout) throws Exception {
        List<FeaturePackLayout> patches = layout.getPatches(c301.getFPID());
        assertEquals(2, patches.size());
        assertEquals(c301Patch1.getFPID(), patches.get(0).getFPID());
        assertEquals(c301Patch2.getFPID(), patches.get(1).getFPID());
    }
}
