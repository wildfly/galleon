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
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.UniverseSpec;
import org.jboss.galleon.util.CollectionUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class FeaturePackDepsConfigBuilder<B extends FeaturePackDepsConfigBuilder<B>> extends ConfigCustomizationsBuilder<B> {

    protected UniverseSpec defaultUniverse;
    Map<String, UniverseSpec> universeSpecs = Collections.emptyMap();
    Map<FeaturePackLocation.ChannelSpec, FeaturePackConfig> fpDeps = Collections.emptyMap();
    Map<String, FeaturePackConfig> fpDepsByOrigin = Collections.emptyMap();
    Map<FeaturePackLocation.ChannelSpec, String> channelToOrigin = Collections.emptyMap();

    protected FeaturePackLocation getConfiguredSource(FeaturePackLocation source) throws ProvisioningDescriptionException {
        if (source.getUniverse() == null) {
            if (defaultUniverse == null) {
                throw new ProvisioningDescriptionException(
                        "Failed to resolve " + source + ": default universe was not configured");
            }
            return new FeaturePackLocation(defaultUniverse, source.getProducer(), source.getChannelName(),
                    source.getFrequency(), source.getBuild());
        }
        final UniverseSpec resolvedSpec = universeSpecs.get(source.getUniverse().toString());
        if (resolvedSpec != null) {
            return new FeaturePackLocation(resolvedSpec, source.getProducer(), source.getChannelName(),
                    source.getFrequency(), source.getBuild());
        }
        return source;
    }

    public B addFeaturePackDep(FeaturePackLocation fpl) throws ProvisioningDescriptionException {
        return addFeaturePackDep(FeaturePackConfig.forLocation(getConfiguredSource(fpl)));
    }

    public B addFeaturePackDep(FeaturePackConfig dependency) throws ProvisioningDescriptionException {
        return addFeaturePackDep(null, dependency);
    }

    @SuppressWarnings("unchecked")
    public B addFeaturePackDep(String origin, FeaturePackConfig dependency) throws ProvisioningDescriptionException {
        if(fpDeps.containsKey(dependency.getLocation().getChannel())) {
            throw new ProvisioningDescriptionException("Feature-pack already added " + dependency.getLocation().getChannel());
        }
        if(origin != null) {
            if(fpDepsByOrigin.containsKey(origin)){
                throw new ProvisioningDescriptionException(Errors.duplicateDependencyName(origin));
            }
            fpDepsByOrigin = CollectionUtils.put(fpDepsByOrigin, origin, dependency);
            channelToOrigin = CollectionUtils.put(channelToOrigin, dependency.getLocation().getChannel(), origin);
        }
        fpDeps = CollectionUtils.putLinked(fpDeps, dependency.getLocation().getChannel(), dependency);
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B removeFeaturePackDep(FeaturePackLocation fpl) throws ProvisioningException {
        final FeaturePackLocation.ChannelSpec channel = fpl.getChannel();
        final FeaturePackConfig fpDep = fpDeps.get(channel);
        if(fpDep == null) {
            throw new ProvisioningException(Errors.unknownFeaturePack(fpl.getFPID()));
        }
        if(!fpDep.getLocation().equals(fpl)) {
            throw new ProvisioningException(Errors.unknownFeaturePack(fpl.getFPID()));
        }
        if(fpDeps.size() == 1) {
            fpDeps = Collections.emptyMap();
            fpDepsByOrigin = Collections.emptyMap();
            channelToOrigin = Collections.emptyMap();
            return (B) this;
        }
        fpDeps = CollectionUtils.remove(fpDeps, channel);
        if(!channelToOrigin.isEmpty()) {
            final String origin = channelToOrigin.get(channel);
            if(origin != null) {
                if(fpDepsByOrigin.size() == 1) {
                    fpDepsByOrigin = Collections.emptyMap();
                    channelToOrigin = Collections.emptyMap();
                } else {
                    fpDepsByOrigin.remove(origin);
                    channelToOrigin.remove(channel);
                }
            }
        }
        return (B) this;
    }

    public int getFeaturePackDepIndex(FeaturePackLocation fpl) throws ProvisioningException {
        final FeaturePackLocation.ChannelSpec channel = fpl.getChannel();
        final FeaturePackConfig fpDep = fpDeps.get(channel);
        if (fpDep == null) {
            throw new ProvisioningException(Errors.unknownFeaturePack(fpl.getFPID()));
        }
        if (!fpDep.getLocation().equals(fpl)) {
            throw new ProvisioningException(Errors.unknownFeaturePack(fpl.getFPID()));
        }
        int i = 0;
        for (FeaturePackLocation.ChannelSpec depChannel : fpDeps.keySet()) {
            if (depChannel.equals(channel)) {
                break;
            }
            i += 1;
        }
        return i;
    }

    @SuppressWarnings("unchecked")
    public B addFeaturePackDep(int index, FeaturePackConfig dependency) throws ProvisioningDescriptionException {
        if (index >= fpDeps.size()) {
            FeaturePackDepsConfigBuilder.this.addFeaturePackDep(dependency);
        } else {
            if (fpDeps.containsKey(dependency.getLocation().getChannel())) {
                throw new ProvisioningDescriptionException("Feature-pack already added " + dependency.getLocation().getChannel());
            }
            // reconstruct the linkedMap.
            Map<FeaturePackLocation.ChannelSpec, FeaturePackConfig> tmp = Collections.emptyMap();
            int i = 0;
            for (Entry<FeaturePackLocation.ChannelSpec, FeaturePackConfig> entry : fpDeps.entrySet()) {
                if (i == index) {
                    tmp = CollectionUtils.putLinked(tmp, dependency.getLocation().getChannel(), dependency);
                }
                tmp = CollectionUtils.putLinked(tmp, entry.getKey(), entry.getValue());
                i += 1;
            }
            fpDeps = tmp;
        }
        return (B) this;
    }

    public B setDefaultUniverse(String factory, String location) throws ProvisioningDescriptionException {
        return setDefaultUniverse(new UniverseSpec(factory, location));
    }

    public B setDefaultUniverse(UniverseSpec universeSpec) throws ProvisioningDescriptionException {
        return addUniverse(null, universeSpec);
    }

    public B addUniverse(String name, String factory, String location) throws ProvisioningDescriptionException {
        return addUniverse(name, new UniverseSpec(factory, location));
    }

    @SuppressWarnings("unchecked")
    public B addUniverse(String name, UniverseSpec universe) throws ProvisioningDescriptionException {
        if(name == null) {
            if(defaultUniverse != null) {
                if(defaultUniverse.equals(universe)) {
                    return (B) this;
                }
                throw new ProvisioningDescriptionException("Failed to make " + universe + " the default universe, "
                        + defaultUniverse + " has already been configured as the default one");
            }
            defaultUniverse = universe;
            return (B) this;
        }
        universeSpecs = CollectionUtils.put(universeSpecs, name, universe);
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
