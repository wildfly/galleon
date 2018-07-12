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

package org.jboss.galleon.layout.update;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.layout.ProvisioningLayout;
import org.jboss.galleon.layout.ProvisioningLayout.FeaturePackLayout;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;
import org.jboss.galleon.util.CollectionUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class ProvisioningUpdatePlan {

    public static Map<ProducerSpec, FeaturePackUpdatePlan> getFullUpdatePlan(Iterable<ProducerSpec> producers, ProvisioningLayout<?> layout) throws ProvisioningException {
        final Map<ProducerSpec, FeaturePackUpdatePlan> plans = new LinkedHashMap<>(layout.getOrderedFeaturePacks().size());
        List<ProducerSpec> transitiveNotInConfig = Collections.emptyList();
        final ProvisioningConfig installedConfig = layout.getConfig();
        boolean skipBuildingLayout = true;
        for(ProducerSpec producer : producers) {
            final FeaturePackUpdatePlan plan = layout.getUpdatePlan(producer);
            if(plan.isEmpty()) {
                continue;
            }
            if(!installedConfig.hasFeaturePackDep(producer) && installedConfig.hasTransitiveDep(producer)) {
                // work in progress
            }
            plans.put(plan.getNewLocation().getProducer(), plan);
            final FeaturePackLayout fp = layout.getFeaturePack(producer);
            if(skipBuildingLayout && plan.hasNewLocation() && fp.getSpec().hasFeaturePackDeps()) {
                skipBuildingLayout = false;
            }
            if (fp.isTransitiveDep() && !installedConfig.hasTransitiveDep(producer)) {
                transitiveNotInConfig = CollectionUtils.add(transitiveNotInConfig, producer);
            }
        }
        if(skipBuildingLayout) {
            return plans;
        }

        final ProvisioningConfig.Builder configBuilder = ProvisioningConfig.builder(installedConfig);
        if(installedConfig.hasTransitiveDeps()) {
            for(FeaturePackConfig fpConfig : installedConfig.getTransitiveDeps()) {
                final FeaturePackUpdatePlan plan = plans.get(fpConfig.getLocation().getProducer());
                if(plan == null) {
                    continue;
                }
                final FeaturePackConfig.Builder fpBuilder = FeaturePackConfig.builder(plan.getNewLocation()).init(fpConfig);
                if(plan.hasNewPatches()) {
                    for(FPID patchId : plan.getNewPatches()) {
                        fpBuilder.addPatch(patchId);
                    }
                }
                configBuilder.updateFeaturePackDep(fpBuilder.build());
            }
        }
        if(!transitiveNotInConfig.isEmpty()) {
            for(ProducerSpec p : transitiveNotInConfig) {
                final FeaturePackUpdatePlan plan = plans.get(p);
                configBuilder.addTransitiveDep(plan.getNewLocation());
            }
        }
        for(FeaturePackConfig fpConfig : installedConfig.getFeaturePackDeps()) {
            final FeaturePackUpdatePlan plan = plans.get(fpConfig.getLocation().getProducer());
            if(plan == null) {
                continue;
            }
            final FeaturePackConfig.Builder fpBuilder = FeaturePackConfig.builder(plan.getNewLocation()).init(fpConfig);
            if(plan.hasNewPatches()) {
                for(FPID patchId : plan.getNewPatches()) {
                    fpBuilder.addPatch(patchId);
                }
            }
            configBuilder.updateFeaturePackDep(fpBuilder.build());
        }

        final ProvisioningLayout<?> updatedLayout = layout.getFactory().newConfigLayout(configBuilder.build(), layout.getFeaturePackFactory(), true);

        if(installedConfig.hasTransitiveDeps()) {
            for(FeaturePackConfig fpConfig : installedConfig.getTransitiveDeps()) {
                if(!updatedLayout.hasFeaturePack(fpConfig.getLocation().getProducer())) {
                    plans.remove(fpConfig.getLocation().getProducer());
                }
            }
        }
        return plans;
    }
}
