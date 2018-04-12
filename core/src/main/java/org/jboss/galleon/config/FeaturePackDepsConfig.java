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

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.util.CollectionUtils;

/**
 * @author Alexey Loubyansky
 *
 */
public class FeaturePackDepsConfig extends ConfigCustomizations {

    protected final Map<ArtifactCoords.Ga, FeaturePackConfig> fpDeps;
    protected final Map<String, FeaturePackConfig> fpDepsByOrigin;
    private final Map<ArtifactCoords.Ga, String> fpGaToOrigin;

    protected FeaturePackDepsConfig(FeaturePackDepsConfigBuilder<?> builder) {
        super(builder);
        this.fpDeps = CollectionUtils.unmodifiable(builder.fpDeps);
        this.fpDepsByOrigin = CollectionUtils.unmodifiable(builder.fpDepsByOrigin);
        this.fpGaToOrigin = builder.fpGaToOrigin;
    }

    public boolean hasFeaturePackDeps() {
        return !fpDeps.isEmpty();
    }

    public boolean hasFeaturePackDep(ArtifactCoords.Ga gaPart) {
        return fpDeps.containsKey(gaPart);
    }

    public FeaturePackConfig getFeaturePackDep(ArtifactCoords.Ga gaPart) {
        return fpDeps.get(gaPart);
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

    public String originOf(ArtifactCoords.Ga fpGa) {
        return fpGaToOrigin.get(fpGa);
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
