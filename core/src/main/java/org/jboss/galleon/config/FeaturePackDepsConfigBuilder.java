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
package org.jboss.galleon.config;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.model.GaecRange;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;
import org.jboss.galleon.universe.UniverseSpec;
import org.jboss.galleon.util.CollectionUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class FeaturePackDepsConfigBuilder<B extends FeaturePackDepsConfigBuilder<B>> extends ConfigCustomizationsBuilder<B> {

    protected UniverseSpec defaultUniverse;
    Map<String, UniverseSpec> universeSpecs = Collections.emptyMap();
    Map<ProducerSpec, FeaturePackConfig> fpDeps = Collections.emptyMap();
    Map<String, FeaturePackConfig> fpDepsByOrigin = Collections.emptyMap();
    Map<ProducerSpec, String> producerOrigins = Collections.emptyMap();
    Map<ProducerSpec, FeaturePackConfig> transitiveDeps = Collections.emptyMap();

    protected UniverseSpec getConfiguredUniverse(FeaturePackLocation source) {
        return source.hasUniverse() ? universeSpecs.get(source.getUniverse().toString()) : defaultUniverse;
    }

    public FeaturePackLocation resolveUniverseSpec(FeaturePackLocation fpl) {
        final UniverseSpec resolved = getConfiguredUniverse(fpl);
        return resolved == null ? fpl : fpl.replaceUniverse(resolved);
    }

    @SuppressWarnings("unchecked")
    public B clearFeaturePackDeps() {
        fpDeps = Collections.emptyMap();
        fpDepsByOrigin = Collections.emptyMap();
        producerOrigins = Collections.emptyMap();
        transitiveDeps = Collections.emptyMap();
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B initUniverses(FeaturePackDepsConfig original) throws ProvisioningDescriptionException {
        if (original.defaultUniverse != null) {
            setDefaultUniverse(original.defaultUniverse);
        }
        for (Map.Entry<String, UniverseSpec> universe : original.universeSpecs.entrySet()) {
            addUniverse(universe.getKey(), universe.getValue());
        }
        return (B) this;
    }

    public B addFeaturePackDep(FeaturePackLocation fpl) throws ProvisioningDescriptionException {
        return addFeaturePackDepResolved(null, FeaturePackConfig.forLocation(resolveUniverseSpec(fpl)), false);
    }

    public B updateFeaturePackDep(FeaturePackLocation fpl) throws ProvisioningDescriptionException {
        return addFeaturePackDepResolved(null, FeaturePackConfig.forLocation(resolveUniverseSpec(fpl)), true);
    }

    public B addTransitiveDep(FeaturePackLocation fpl) throws ProvisioningDescriptionException {
        return addFeaturePackDepResolved(null, FeaturePackConfig.forTransitiveDep(resolveUniverseSpec(fpl)), false);
    }

    public B updateTransitiveDep(FeaturePackLocation fpl) throws ProvisioningDescriptionException {
        return addFeaturePackDepResolved(null, FeaturePackConfig.forTransitiveDep(resolveUniverseSpec(fpl)), true);
    }

    public B addFeaturePackDep(FeaturePackConfig dependency) throws ProvisioningDescriptionException {
        return addFeaturePackDep(null, dependency);
    }

    public B updateFeaturePackDep(FeaturePackConfig dependency) throws ProvisioningDescriptionException {
        return updateFeaturePackDep(null, dependency);
    }

    public B addFeaturePackDep(String origin, FeaturePackConfig dependency) throws ProvisioningDescriptionException {
        final UniverseSpec configuredUniverse = getConfiguredUniverse(dependency.getLocation());
        return addFeaturePackDepResolved(origin, configuredUniverse == null ? dependency : FeaturePackConfig.builder(dependency.getLocation().replaceUniverse(configuredUniverse)).init(dependency).build(), false);
    }

    public B updateFeaturePackDep(String origin, FeaturePackConfig dependency) throws ProvisioningDescriptionException {
        final UniverseSpec configuredUniverse = getConfiguredUniverse(dependency.getLocation());
        return addFeaturePackDepResolved(origin, configuredUniverse == null ? dependency : FeaturePackConfig.builder(dependency.getLocation().replaceUniverse(configuredUniverse)).init(dependency).build(), true);
    }

    @SuppressWarnings("unchecked")
    private B addFeaturePackDepResolved(String origin, FeaturePackConfig dependency, boolean replaceExistingVersion) throws ProvisioningDescriptionException {
        String existingOrigin = null;
        final ProducerSpec producer = dependency.getLocation().getProducer();
        if(dependency.isTransitive()) {
            if(fpDeps.containsKey(producer)) {
                throw new ProvisioningDescriptionException(producer + " has been already added as a direct dependency");
            }
            if(transitiveDeps.containsKey(producer)) {
                if(!replaceExistingVersion) {
                    throw new ProvisioningDescriptionException(Errors.featurePackAlreadyConfigured(producer));
                }
                existingOrigin = producerOrigins.get(producer);
            }
            transitiveDeps = CollectionUtils.putLinked(transitiveDeps, producer, dependency);
        } else {
            if(transitiveDeps.containsKey(producer)) {
                throw new ProvisioningDescriptionException(producer + " has been already added as a transitive dependency");
            }
            if(fpDeps.containsKey(producer)) {
                if(!replaceExistingVersion) {
                    throw new ProvisioningDescriptionException(Errors.featurePackAlreadyConfigured(producer));
                }
                existingOrigin = producerOrigins.get(producer);
            }
            fpDeps = CollectionUtils.putLinked(fpDeps, producer, dependency);
        }
        if(origin != null) {
            if(existingOrigin != null) {
                if (!existingOrigin.equals(origin)) {
                    fpDepsByOrigin = CollectionUtils.remove(fpDepsByOrigin, existingOrigin);
                    producerOrigins = CollectionUtils.put(producerOrigins, producer, origin);
                }
            } else if(fpDepsByOrigin.containsKey(origin)) {
                throw new ProvisioningDescriptionException(Errors.duplicateDependencyName(origin));
            } else {
                producerOrigins = CollectionUtils.put(producerOrigins, producer, origin);
            }
            fpDepsByOrigin = CollectionUtils.put(fpDepsByOrigin, origin, dependency);
        }
        return (B) this;
    }

    public boolean hasFeaturePackDep(ProducerSpec producer) {
        return fpDeps.containsKey(producer);
    }

    public boolean hasFeaturePackDeps() {
        return !fpDeps.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public B removeFeaturePackDep(FeaturePackLocation fpl) throws ProvisioningException {
        fpl = resolveUniverseSpec(fpl);
        final ProducerSpec producer = fpl.getProducer();
        final FeaturePackConfig fpDep = fpDeps.get(producer);
        if(fpDep == null) {
            throw new ProvisioningException(Errors.unknownFeaturePack(fpl.getFPID()));
        }
        if(!fpDep.getLocation().getFPID().equals(fpl.getFPID())) {
            throw new ProvisioningException(Errors.unknownFeaturePack(fpl.getFPID()));
        }
        if(fpDeps.size() == 1) {
            fpDeps = Collections.emptyMap();
        } else {
            fpDeps = CollectionUtils.remove(fpDeps, producer);
        }
        updateOriginMappings(producer);
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B removeTransitiveDep(FPID fpid) throws ProvisioningException {
        final FeaturePackLocation fpl = resolveUniverseSpec(fpid.getLocation());
        final ProducerSpec producer = fpl.getProducer();
        final FeaturePackConfig fpDep = transitiveDeps.get(producer);
        if(fpDep == null) {
            throw new ProvisioningException(Errors.unknownFeaturePack(fpid));
        }
        if(!fpDep.getLocation().equals(fpl)) {
            throw new ProvisioningException(Errors.unknownFeaturePack(fpid));
        }
        if(transitiveDeps.size() == 1) {
            transitiveDeps = Collections.emptyMap();
            return (B) this;
        } else {
            transitiveDeps = CollectionUtils.remove(transitiveDeps, producer);
        }
        updateOriginMappings(producer);
        return (B) this;
    }

    private void updateOriginMappings(final ProducerSpec producer) {
        if (producerOrigins.isEmpty()) {
            return;
        }
        final String origin = producerOrigins.get(producer);
        if (origin == null) {
            return;
        }
        if (fpDepsByOrigin.size() == 1) {
            fpDepsByOrigin = Collections.emptyMap();
            producerOrigins = Collections.emptyMap();
            return;
        }
        fpDepsByOrigin.remove(origin);
        producerOrigins.remove(producer);
    }

    public int getFeaturePackDepIndex(FeaturePackLocation fpl) throws ProvisioningException {
        fpl = resolveUniverseSpec(fpl);
        final ProducerSpec producer = fpl.getProducer();
        final FeaturePackConfig fpDep = fpDeps.get(producer);
        if (fpDep == null) {
            throw new ProvisioningException(Errors.unknownFeaturePack(fpl.getFPID()));
        }
        if (!fpDep.getLocation().equals(fpl)) {
            throw new ProvisioningException(Errors.unknownFeaturePack(fpl.getFPID()));
        }
        int i = 0;
        for (ProducerSpec depProducer : fpDeps.keySet()) {
            if (depProducer.equals(producer)) {
                break;
            }
            i += 1;
        }
        return i;
    }

    @SuppressWarnings("unchecked")
    public B addFeaturePackDep(int index, FeaturePackConfig dependency) throws ProvisioningDescriptionException {
        if (index >= fpDeps.size()) {
            addFeaturePackDep(dependency);
            return (B) this;
        }
        FeaturePackLocation fpl = dependency.getLocation();
        final UniverseSpec resolvedUniverse = getConfiguredUniverse(fpl);
        if(resolvedUniverse != null) {
            fpl = fpl.replaceUniverse(resolvedUniverse);
            dependency = FeaturePackConfig.builder(fpl).init(dependency).build();
        }
        if (fpDeps.containsKey(fpl.getProducer())) {
            throw new ProvisioningDescriptionException(Errors.featurePackAlreadyConfigured(fpl.getProducer()));
        }
        // reconstruct the linkedMap.
        Map<ProducerSpec, FeaturePackConfig> tmp = Collections.emptyMap();
        int i = 0;
        for (Entry<ProducerSpec, FeaturePackConfig> entry : fpDeps.entrySet()) {
            if (i == index) {
                tmp = CollectionUtils.putLinked(tmp, fpl.getProducer(), dependency);
            }
            tmp = CollectionUtils.putLinked(tmp, entry.getKey(), entry.getValue());
            i += 1;
        }
        fpDeps = tmp;
        return (B) this;
    }

    public String originOf(ProducerSpec producer) {
        return producerOrigins.get(producer);
    }

    public B setDefaultUniverse(String factory, GaecRange location) throws ProvisioningDescriptionException {
        return setDefaultUniverse(new UniverseSpec(factory, location));
    }

    public B setDefaultUniverse(UniverseSpec universeSpec) throws ProvisioningDescriptionException {
        return addUniverse(null, universeSpec);
    }

    public B addUniverse(String name, String factory, GaecRange location) throws ProvisioningDescriptionException {
        return addUniverse(name, new UniverseSpec(factory, location));
    }

    @SuppressWarnings("unchecked")
    public B addUniverse(String name, UniverseSpec universe) throws ProvisioningDescriptionException {
        if(name == null) {
            defaultUniverse = universe;
            return (B) this;
        }
        universeSpecs = CollectionUtils.put(universeSpecs, name, universe);
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B removeUniverse(String name) throws ProvisioningDescriptionException {
        if(name == null) {
            defaultUniverse = null;
            return (B) this;
        }
        universeSpecs = CollectionUtils.remove(universeSpecs, name);
        return (B) this;
    }

    public boolean hasUniverse(String name) {
        if(name == null) {
            return hasDefaultUniverse();
        }
        return universeSpecs.containsKey(name);
    }

    public UniverseSpec getUniverseSpec(String name) {
        return universeSpecs.get(name);
    }

    public boolean hasDefaultUniverse() {
        return defaultUniverse != null;
    }

    public UniverseSpec getDefaultUniverse() {
        return defaultUniverse;
    }
}
