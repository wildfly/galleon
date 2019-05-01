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
package org.jboss.galleon.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.spec.CapabilitySpec;
import org.jboss.galleon.util.CollectionUtils;

/**
 * @author Alexey Loubyansky
 *
 */
public class SpecOnlyConfigArranger {

    private Map<ResolvedSpecId, SpecFeatures> specFeatures;
    private Map<ResolvedFeatureId, ResolvedFeature> features;

    private CapabilityResolver capResolver = new CapabilityResolver();
    private Map<String, CapabilityProviders> capProviders = Collections.emptyMap();

    // features in the order they should be processed by the provisioning handlers
    private List<ResolvedFeature> orderedFeatures = Collections.emptyList();
    private boolean orderReferencedSpec = true;
    private boolean inBatch;

    public List<ResolvedFeature> orderFeatures(ConfigModelStack stack) throws ProvisioningException {
        this.specFeatures = stack.specFeatures;
        this.features = stack.features;
        try {
            doOrder(stack.rt);
        } catch (ProvisioningException e) {
            throw new ProvisioningException(Errors.failedToBuildConfigSpec(stack.id.getModel(), stack.id.getName()), e);
        }
        return orderedFeatures;
    }

    private void doOrder(ProvisioningRuntimeBuilder rt) throws ProvisioningException {
        for (SpecFeatures features : specFeatures.values()) {
            // resolve references
            features.spec.resolveRefMappings(rt);
            // resolve and register capability providers
            if(features.spec.xmlSpec.providesCapabilities()) {
                for(CapabilitySpec cap : features.spec.xmlSpec.getProvidedCapabilities()) {
                    if(cap.isStatic()) {
                        getProviders(cap.toString(), true).add(features);
                    } else {
                        for(ResolvedFeature feature : features.getFeatures()) {
                            final List<String> resolvedCaps = capResolver.resolve(cap, feature);
                            if(resolvedCaps.isEmpty()) {
                                continue;
                            }
                            for(String resolvedCap : resolvedCaps) {
                                getProviders(resolvedCap, true).add(feature);
                            }
                        }
                    }
                }
            }
        }
        orderedFeatures = new ArrayList<>(features.size());
        for(SpecFeatures features : specFeatures.values()) {
            orderFeaturesInSpec(features, false);
        }
    }

    private CapabilityProviders getProviders(String cap, boolean add) throws ProvisioningException {
        CapabilityProviders providers = capProviders.get(cap);
        if(providers != null) {
            return providers;
        }
        if(!add) {
            throw new ProvisioningException(Errors.noCapabilityProvider(cap));
        }
        providers = new CapabilityProviders();
        capProviders = CollectionUtils.put(capProviders, cap, providers);
        return providers;
    }

    /**
     * Attempts to order the features of the spec.
     * Terminates immediately when a feature reference loop is detected.
     *
     * @param specFeatures  spec features
     * @return  returns the feature id on which the feature reference loop was detected,
     *   returns null if no loop was detected (despite whether any feature was processed or not)
     * @throws ProvisioningException
     */
    private List<CircularRefInfo> orderFeaturesInSpec(SpecFeatures specFeatures, boolean force) throws ProvisioningException {
        if(!force) {
            if (!specFeatures.isFree()) {
                return null;
            }
            specFeatures.schedule();
        }

        List<CircularRefInfo> allCircularRefs = null;
        int i = 0;
        final List<ResolvedFeature> features = specFeatures.getFeatures();
        while(i < features.size() && allCircularRefs == null) {
            allCircularRefs = orderFeature(features.get(i++));
/*            if(circularRefs != null) {
                if(allCircularRefs == null) {
                    allCircularRefs = circularRefs;
                } else {
                    if(allCircularRefs.size() == 1) {
                        final CircularRefInfo first = allCircularRefs.get(0);
                        allCircularRefs = new ArrayList<>(1 + circularRefs.size());
                        allCircularRefs.add(first);
                    }
                    allCircularRefs.addAll(circularRefs);
                }
            }
*/        }
        if(!force) {
            specFeatures.free();
        }
        return allCircularRefs;
    }

    /**
     * Attempts to order the feature. If the feature has already been scheduled
     * for ordering but haven't been ordered yet, it means there is a circular feature
     * reference loop, in which case the feature is not ordered and false is returned.
     *
     * @param feature  the feature to put in the ordered list
     * @return  whether the feature was added to the ordered list or not
     * @throws ProvisioningException
     */
    private List<CircularRefInfo> orderFeature(ResolvedFeature feature) throws ProvisioningException {
        if(feature.isOrdered()) {
            return null;
        }
        if(!feature.isFree()) {
            return Collections.singletonList(new CircularRefInfo(feature));
        }
        feature.schedule();

        List<CircularRefInfo> circularRefs = null;
        if(feature.spec.xmlSpec.requiresCapabilities()) {
            circularRefs = orderCapabilityProviders(feature, circularRefs);
        }
        if(!feature.deps.isEmpty()) {
            circularRefs = orderReferencedFeatures(feature, feature.deps.keySet(), false, circularRefs);
        }
        List<ResolvedFeatureId> refIds = feature.resolveRefs();
        if(!refIds.isEmpty()) {
            circularRefs = orderReferencedFeatures(feature, refIds, true, circularRefs);
        }

        List<CircularRefInfo> initiatedCircularRefs = Collections.emptyList();
        if(circularRefs != null) {
            // there is a one or more circular feature reference loop(s)

            // check whether there is a loop that this feature didn't initiate
            // if there is such a loop then propagate the loops this feature didn't start to their origins
            if(circularRefs.size() == 1) {
                final CircularRefInfo next = circularRefs.get(0);
                if (next.loopedOn.id.equals(feature.id)) { // this feature initiated the loop
                    circularRefs = Collections.emptyList();
                    initiatedCircularRefs = Collections.singletonList(next);
                } else {
                    next.setNext(feature);
                    feature.free();
                }
            } else {
                final Iterator<CircularRefInfo> i = circularRefs.iterator();
                while (i.hasNext()) {
                    final CircularRefInfo next = i.next();
                    if (next.loopedOn.id.equals(feature.id)) {
                        // this feature initiated the loop
                        i.remove();
                        initiatedCircularRefs = CollectionUtils.add(initiatedCircularRefs, next);
                    } else {
                        // the feature is in the middle of the loop
                        next.setNext(feature);
                        feature.free();
                    }
                }
            }
            if(!circularRefs.isEmpty()) {
                return circularRefs;
            }
            // all the loops were initiated by this feature
        }

        if (!initiatedCircularRefs.isEmpty()) {
            final boolean prevOrderRefSpec = orderReferencedSpec;
            orderReferencedSpec = false;
            // sort according to the appearance in the config
            initiatedCircularRefs.sort(CircularRefInfo.getFirstInConfigComparator());
            if(initiatedCircularRefs.get(0).firstInConfig.includeNo < feature.includeNo) {
                feature.free();
                for(CircularRefInfo ref : initiatedCircularRefs) {
                    if(orderFeature(ref.firstInConfig) != null) {
                        throw new IllegalStateException();
                    }
                }
            } else {
                final boolean endBatch;
                if(inBatch) {
                    endBatch = false;
                } else {
                    inBatch = true;
                    feature.startBatch();
                    endBatch = true;
                }
                feature.ordered();
                orderedFeatures.add(feature);
                initiatedCircularRefs.sort(CircularRefInfo.getNextOnPathComparator());
                for(CircularRefInfo ref : initiatedCircularRefs) {
                    if(orderFeature(ref.nextOnPath) != null) {
                        throw new IllegalStateException();
                    }
                }
                if(endBatch) {
                    inBatch = false;
                    orderedFeatures.get(orderedFeatures.size() - 1).endBatch();
                }
            }
            orderReferencedSpec = prevOrderRefSpec;
        } else {
            feature.ordered();
            orderedFeatures.add(feature);
        }
        return null;
    }

    private List<CircularRefInfo> orderCapabilityProviders(ResolvedFeature feature, List<CircularRefInfo> circularRefs)
            throws ProvisioningException {
        for (CapabilitySpec capSpec : feature.spec.xmlSpec.getRequiredCapabilities()) {
            final List<String> resolvedCaps = capResolver.resolve(capSpec, feature);
            if (resolvedCaps.isEmpty()) {
                continue;
            }
            for (String resolvedCap : resolvedCaps) {
                final CapabilityProviders providers;
                try {
                    providers = getProviders(resolvedCap, false);
                } catch (ProvisioningException e) {
                    throw new ProvisioningException(Errors.noCapabilityProvider(feature, capSpec, resolvedCap));
                }
                final List<CircularRefInfo> circles = orderProviders(providers);
                if (circularRefs == null) {
                    circularRefs = circles;
                } else {
                    if (circularRefs.size() == 1) {
                        final CircularRefInfo first = circularRefs.get(0);
                        circularRefs = new ArrayList<>(1 + circles.size());
                        circularRefs.add(first);
                    }
                    circularRefs.addAll(circles);
                }
            }
        }
        return circularRefs;
    }

    private List<CircularRefInfo> orderProviders(CapabilityProviders providers) throws ProvisioningException {
        if(!providers.isProvided()) {
            List<CircularRefInfo> firstLoop = null;
            if(!providers.specs.isEmpty()) {
                for(SpecFeatures specFeatures : providers.specs) {
                    final List<CircularRefInfo> loop = orderFeaturesInSpec(specFeatures, !specFeatures.isFree());
                    if(providers.isProvided()) {
                        return null;
                    }
                    if(firstLoop == null) {
                        firstLoop = loop;
                    }
                }
            }
            if (!providers.features.isEmpty()) {
                for (ResolvedFeature provider : providers.features) {
                    final List<CircularRefInfo> loop = orderFeature(provider);
                    if(providers.isProvided()) {
                        return null;
                    }
                    if(firstLoop == null) {
                        firstLoop = loop;
                    }
                }
            }
            return firstLoop;
        }
        return null;
    }

    /**
     * Attempts to order the referenced features.
     *
     * @param feature  parent feature
     * @param refIds  referenced features ids
     * @param specRefs  whether these referenced features represent actual spec references or feature dependencies
     * @return  feature ids that form circular dependency loops
     * @throws ProvisioningException
     */
    private List<CircularRefInfo> orderReferencedFeatures(ResolvedFeature feature, Collection<ResolvedFeatureId> refIds, boolean specRefs, List<CircularRefInfo> circularRefs) throws ProvisioningException {
        for(ResolvedFeatureId refId : refIds) {
            final List<CircularRefInfo> loopedOnFeature = orderReferencedFeature(feature, refId, specRefs);
            if(loopedOnFeature != null) {
                if(circularRefs == null) {
                    circularRefs = loopedOnFeature;
                } else {
                    if(circularRefs.size() == 1) {
                        final CircularRefInfo first = circularRefs.get(0);
                        circularRefs = new ArrayList<>(1 + loopedOnFeature.size());
                        circularRefs.add(first);
                    }
                    circularRefs.addAll(loopedOnFeature);
                }
            }
        }
        return circularRefs;
    }

    /**
     * Attempts to order a feature reference.
     *
     * @param feature  parent feature
     * @param refId  referenced feature id
     * @param specRef  whether the referenced feature represents a spec reference or a feature dependency
     * @return  true if the referenced feature was ordered, false if the feature was not ordered because of the circular reference loop
     * @throws ProvisioningException
     */
    private List<CircularRefInfo> orderReferencedFeature(ResolvedFeature feature, ResolvedFeatureId refId, boolean specRef) throws ProvisioningException {
        if(orderReferencedSpec && specRef && !feature.spec.id.equals(refId.specId)) {
            final SpecFeatures targetSpecFeatures = specFeatures.get(refId.specId);
            if (targetSpecFeatures == null) {
                throw new ProvisioningDescriptionException(Errors.unresolvedFeatureDep(feature, refId));
            }
            final List<CircularRefInfo> specLoops = orderFeaturesInSpec(targetSpecFeatures, false);
            if (specLoops != null) {
                List<CircularRefInfo> featureLoops = null;
                for (int i = 0; i < specLoops.size(); ++i) {
                    final CircularRefInfo specLoop = specLoops.get(i);
                    if (specLoop.nextOnPath.id.equals(refId)) {
                        if (featureLoops == null) {
                            featureLoops = Collections.singletonList(specLoop);
                        } else {
                            if (featureLoops.size() == 1) {
                                final CircularRefInfo first = featureLoops.get(0);
                                featureLoops = new ArrayList<>(2);
                                featureLoops.add(first);
                            }
                            featureLoops.add(specLoop);
                        }
                    }
                }
                if (featureLoops != null) {
                    return featureLoops;
                }
            }
        }
        final ResolvedFeature dep = features.get(refId);
        if (dep == null) {
            throw new ProvisioningDescriptionException(Errors.unresolvedFeatureDep(feature, refId));
        }
        return orderFeature(dep);
    }
}
