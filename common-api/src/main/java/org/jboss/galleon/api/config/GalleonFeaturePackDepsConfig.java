/*
 * Copyright 2016-2023 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.api.config;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.jboss.galleon.BaseErrors;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;
import org.jboss.galleon.universe.UniverseSpec;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.StringUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class GalleonFeaturePackDepsConfig extends GalleonConfigCustomizations {

    protected final UniverseSpec defaultUniverse;
    protected final Map<String, UniverseSpec> universeSpecs;
    protected final Map<ProducerSpec, GalleonFeaturePackConfig> fpDeps;
    protected final Map<String, GalleonFeaturePackConfig> fpDepsByOrigin;
    private final Map<ProducerSpec, String> producerOrigins;
    protected final Map<ProducerSpec, GalleonFeaturePackConfig> transitiveDeps;

    protected GalleonFeaturePackDepsConfig(GalleonFeaturePackDepsConfigBuilder<?> builder) throws ProvisioningDescriptionException {
        super(builder);
        this.fpDeps = CollectionUtils.unmodifiable(builder.fpDeps);
        this.fpDepsByOrigin = CollectionUtils.unmodifiable(builder.fpDepsByOrigin);
        this.producerOrigins = builder.producerOrigins;
        this.transitiveDeps = CollectionUtils.unmodifiable(builder.transitiveDeps);
        this.defaultUniverse = builder.defaultUniverse;
        this.universeSpecs = CollectionUtils.unmodifiable(builder.universeSpecs);
    }

    public boolean hasDefaultUniverse() {
        return defaultUniverse != null;
    }

    public UniverseSpec getDefaultUniverse() {
        return defaultUniverse;
    }

    public boolean hasUniverse(String name) {
        return name == null ? defaultUniverse != null : universeSpecs.containsKey(name);
    }

    public UniverseSpec getUniverseSpec(String name) throws ProvisioningDescriptionException {
        final UniverseSpec universe = name == null ? defaultUniverse : universeSpecs.get(name);
        if(universe == null) {
            throw new ProvisioningDescriptionException((name == null ? "The default" : name) + " universe was not configured");
        }
        return universe;
    }

    public boolean hasUniverseNamedSpecs() {
        return !universeSpecs.isEmpty();
    }

    public Map<String, UniverseSpec> getUniverseNamedSpecs() {
        return universeSpecs;
    }

    public FeaturePackLocation getUserConfiguredLocation(FeaturePackLocation fpl) {
        final UniverseSpec universeSource = fpl.getUniverse();
        if(defaultUniverse != null && defaultUniverse.equals(universeSource)) {
            return new FeaturePackLocation(null, fpl.getProducerName(), fpl.getChannelName(), fpl.getFrequency(),
                    fpl.getBuild());
        }
        for (Map.Entry<String, UniverseSpec> entry : universeSpecs.entrySet()) {
            if (entry.getValue().equals(universeSource)) {
                return new FeaturePackLocation(new UniverseSpec(entry.getKey(), null), fpl.getProducerName(),
                        fpl.getChannelName(), fpl.getFrequency(), fpl.getBuild());
            }
        }
        return fpl;
    }

    public boolean hasFeaturePackDeps() {
        return !fpDeps.isEmpty();
    }

    public boolean hasFeaturePackDep(ProducerSpec producer) {
        return fpDeps.containsKey(producer);
    }

    public Set<ProducerSpec> getProducers() {
        return fpDeps.keySet();
    }

    public GalleonFeaturePackConfig getFeaturePackDep(ProducerSpec producer) {
        return fpDeps.get(producer);
    }

    public Collection<GalleonFeaturePackConfig> getFeaturePackDeps() {
        return fpDeps.values();
    }

    public GalleonFeaturePackConfig getFeaturePackDep(String origin) throws ProvisioningDescriptionException {
        final GalleonFeaturePackConfig fpDep = fpDepsByOrigin.get(origin);
        if(fpDep == null) {
            throw new ProvisioningDescriptionException(BaseErrors.unknownFeaturePackDependencyName(origin));
        }
        return fpDep;
    }

    public String originOf(ProducerSpec producer) {
        return producerOrigins.get(producer);
    }

    public boolean hasTransitiveDeps() {
        return !transitiveDeps.isEmpty();
    }

    public boolean hasTransitiveDep(ProducerSpec producer) {
        return transitiveDeps.containsKey(producer);
    }

    public GalleonFeaturePackConfig getTransitiveDep(ProducerSpec producer) {
        return transitiveDeps.get(producer);
    }

    public Collection<GalleonFeaturePackConfig> getTransitiveDeps() {
        return transitiveDeps.values();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((producerOrigins == null) ? 0 : producerOrigins.hashCode());
        result = prime * result + ((defaultUniverse == null) ? 0 : defaultUniverse.hashCode());
        result = prime * result + ((fpDeps == null) ? 0 : fpDeps.hashCode());
        result = prime * result + ((fpDepsByOrigin == null) ? 0 : fpDepsByOrigin.hashCode());
        result = prime * result + ((transitiveDeps == null) ? 0 : transitiveDeps.hashCode());
        result = prime * result + ((universeSpecs == null) ? 0 : universeSpecs.hashCode());
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
        GalleonFeaturePackDepsConfig other = (GalleonFeaturePackDepsConfig) obj;
        if (producerOrigins == null) {
            if (other.producerOrigins != null)
                return false;
        } else if (!producerOrigins.equals(other.producerOrigins))
            return false;
        if (defaultUniverse == null) {
            if (other.defaultUniverse != null)
                return false;
        } else if (!defaultUniverse.equals(other.defaultUniverse))
            return false;
        if (fpDeps == null) {
            if (other.fpDeps != null)
                return false;
        } else if (fpDeps.size() != other.fpDeps.size())
            return false;
        final Iterator<Map.Entry<ProducerSpec, GalleonFeaturePackConfig>> i = fpDeps.entrySet().iterator();
        final Iterator<Map.Entry<ProducerSpec, GalleonFeaturePackConfig>> otherI = other.fpDeps.entrySet().iterator();
        while(i.hasNext()) {
            if(!i.next().equals(otherI.next())) {
                return false;
            }
        }
        if (fpDepsByOrigin == null) {
            if (other.fpDepsByOrigin != null)
                return false;
        } else if (!fpDepsByOrigin.equals(other.fpDepsByOrigin))
            return false;
        if (transitiveDeps == null) {
            if (other.transitiveDeps != null)
                return false;
        } else if (!transitiveDeps.equals(other.transitiveDeps))
            return false;
        if (universeSpecs == null) {
            if (other.universeSpecs != null)
                return false;
        } else if (!universeSpecs.equals(other.universeSpecs))
            return false;
        return true;
    }

    protected void append(StringBuilder buf) {
        if(defaultUniverse != null) {
            buf.append("default-universe=").append(defaultUniverse);
        }
        if(!universeSpecs.isEmpty()) {
            if(defaultUniverse != null) {
                buf.append(' ');
            }
            buf.append("universes=[");
            StringUtils.append(buf, universeSpecs.entrySet());
            buf.append("] ");
        }
        if(!transitiveDeps.isEmpty()) {
            StringUtils.append(buf.append("transitive="), transitiveDeps.values());
            buf.append(' ');
        }
        StringUtils.append(buf, fpDeps.values());
        super.append(buf);
    }
}
