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

import java.util.Collection;
import java.util.Map;

import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.UniverseSpec;
import org.jboss.galleon.util.CollectionUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeaturePackDepsConfig extends ConfigCustomizations {

    protected final UniverseSpec defaultUniverse;
    protected final Map<String, UniverseSpec> universeConfigs;
    protected final Map<FeaturePackLocation.ChannelSpec, FeaturePackConfig> fpDeps;
    protected final Map<String, FeaturePackConfig> fpDepsByOrigin;
    private final Map<FeaturePackLocation.ChannelSpec, String> channelToOrigin;

    protected FeaturePackDepsConfig(FeaturePackDepsConfigBuilder<?> builder) throws ProvisioningDescriptionException {
        super(builder);
        this.fpDeps = CollectionUtils.unmodifiable(builder.fpDeps);
        this.fpDepsByOrigin = CollectionUtils.unmodifiable(builder.fpDepsByOrigin);
        this.channelToOrigin = builder.channelToOrigin;
        this.defaultUniverse = builder.defaultUniverse;
        this.universeConfigs = CollectionUtils.unmodifiable(builder.universeSpecs);
    }

    public boolean hasDefaultUniverse() {
        return defaultUniverse != null;
    }

    public UniverseSpec getDefaultUniverse() {
        return defaultUniverse;
    }

    public UniverseSpec getUniverseConfig(String name) throws ProvisioningDescriptionException {
        final UniverseSpec universe = name == null ? defaultUniverse : universeConfigs.get(name);
        if(universe == null) {
            throw new ProvisioningDescriptionException((name == null ? "The default" : name) + " universe was not configured");
        }
        return universe;
    }

    public boolean hasUniverseNamedConfigs() {
        return !universeConfigs.isEmpty();
    }

    public Map<String, UniverseSpec> getNamedUniverses() {
        return universeConfigs;
    }

    public FeaturePackLocation getUserConfiguredSource(FeaturePackLocation fpSource) {
        final UniverseSpec universeSource = fpSource.getUniverse();
        if(defaultUniverse != null && defaultUniverse.equals(universeSource)) {
            return new FeaturePackLocation(null, fpSource.getProducer(), fpSource.getChannelName(), fpSource.getFrequency(),
                    fpSource.getBuild());
        }
        for (Map.Entry<String, UniverseSpec> entry : universeConfigs.entrySet()) {
            if (entry.getValue().equals(universeSource)) {
                return new FeaturePackLocation(new UniverseSpec(entry.getKey(), null), fpSource.getProducer(),
                        fpSource.getChannelName(), fpSource.getFrequency(), fpSource.getBuild());
            }
        }
        return fpSource;
    }

    public boolean hasFeaturePackDeps() {
        return !fpDeps.isEmpty();
    }

    public boolean hasFeaturePackDep(FeaturePackLocation.ChannelSpec channel) {
        return fpDeps.containsKey(channel);
    }

    public FeaturePackConfig getFeaturePackDep(FeaturePackLocation.ChannelSpec channel) {
        return fpDeps.get(channel);
    }

    public Collection<FeaturePackConfig> getFeaturePackDeps() {
        return fpDeps.values();
    }

    public FeaturePackConfig getFeaturePackDep(String origin) throws ProvisioningDescriptionException {
        final FeaturePackConfig fpDep = fpDepsByOrigin.get(origin);
        if(fpDep == null) {
            throw new ProvisioningDescriptionException(Errors.unknownFeaturePackDependencyName(origin));
        }
        return fpDep;
    }

    public String originOf(FeaturePackLocation.ChannelSpec channel) {
        return channelToOrigin.get(channel);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((fpDeps == null) ? 0 : fpDeps.hashCode());
        result = prime * result + ((fpDepsByOrigin == null) ? 0 : fpDepsByOrigin.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        FeaturePackDepsConfig other = (FeaturePackDepsConfig) obj;
        if (fpDeps == null) {
            if (other.fpDeps != null)
                return false;
        } else if (!fpDeps.equals(other.fpDeps))
            return false;
        if (fpDepsByOrigin == null) {
            if (other.fpDepsByOrigin != null)
                return false;
        } else if (!fpDepsByOrigin.equals(other.fpDepsByOrigin))
            return false;
        return true;
    }
}
