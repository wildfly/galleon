/*
 * Copyright ${license.git.copyrightYears} Red Hat, Inc. and/or its affiliates
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

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ArtifactCoords.Ga;
import org.jboss.galleon.util.CollectionUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class FeaturePackDepsConfigBuilder<B extends FeaturePackDepsConfigBuilder<B>> extends ConfigCustomizationsBuilder<B> {

    Map<ArtifactCoords.Ga, FeaturePackConfig> fpDeps = Collections.emptyMap();
    Map<String, FeaturePackConfig> fpDepsByOrigin = Collections.emptyMap();
    Map<ArtifactCoords.Ga, String> fpGaToOrigin = Collections.emptyMap();

    public B addFeaturePackDep(FeaturePackConfig dependency) throws ProvisioningDescriptionException {
        return addFeaturePackDep(null, dependency);
    }

    @SuppressWarnings("unchecked")
    public B addFeaturePackDep(String origin, FeaturePackConfig dependency) throws ProvisioningDescriptionException {
        if(fpDeps.containsKey(dependency.getGav().toGa())) {
            throw new ProvisioningDescriptionException("Feature-pack already added " + dependency.getGav().toGa());
        }
        if(origin != null) {
            if(fpDepsByOrigin.containsKey(origin)){
                throw new ProvisioningDescriptionException(Errors.duplicateDependencyName(origin));
            }
            fpDepsByOrigin = CollectionUtils.put(fpDepsByOrigin, origin, dependency);
            fpGaToOrigin = CollectionUtils.put(fpGaToOrigin, dependency.getGav().toGa(), origin);
        }
        fpDeps = CollectionUtils.putLinked(fpDeps, dependency.getGav().toGa(), dependency);
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B removeFeaturePackDep(ArtifactCoords.Gav gav) throws ProvisioningException {
        final Ga ga = gav.toGa();
        final FeaturePackConfig fpDep = fpDeps.get(ga);
        if(fpDep == null) {
            throw new ProvisioningException(Errors.unknownFeaturePack(gav));
        }
        if(!fpDep.getGav().equals(gav)) {
            throw new ProvisioningException(Errors.unknownFeaturePack(gav));
        }
        if(fpDeps.size() == 1) {
            fpDeps = Collections.emptyMap();
            fpDepsByOrigin = Collections.emptyMap();
            fpGaToOrigin = Collections.emptyMap();
            return (B) this;
        }
        fpDeps = CollectionUtils.remove(fpDeps, ga);
        if(!fpGaToOrigin.isEmpty()) {
            final String origin = fpGaToOrigin.get(ga);
            if(origin != null) {
                if(fpDepsByOrigin.size() == 1) {
                    fpDepsByOrigin = Collections.emptyMap();
                    fpGaToOrigin = Collections.emptyMap();
                } else {
                    fpDepsByOrigin.remove(origin);
                    fpGaToOrigin.remove(ga);
                }
            }
        }
        return (B) this;
    }

    public int getFeaturePackDepIndex(ArtifactCoords.Gav gav) throws ProvisioningException {
        final ArtifactCoords.Ga ga = gav.toGa();
        final FeaturePackConfig fpDep = fpDeps.get(ga);
        if (fpDep == null) {
            throw new ProvisioningException(Errors.unknownFeaturePack(gav));
        }
        if (!fpDep.getGav().equals(gav)) {
            throw new ProvisioningException(Errors.unknownFeaturePack(gav));
        }
        int i = 0;
        for (ArtifactCoords.Ga g : fpDeps.keySet()) {
            if (g.equals(ga)) {
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
            if (fpDeps.containsKey(dependency.getGav().toGa())) {
                throw new ProvisioningDescriptionException("Feature-pack already added " + dependency.getGav().toGa());
            }
            // reconstruct the linkedMap.
            Map<ArtifactCoords.Ga, FeaturePackConfig> tmp = Collections.emptyMap();
            int i = 0;
            for (Entry<ArtifactCoords.Ga, FeaturePackConfig> entry : fpDeps.entrySet()) {
                if (i == index) {
                    tmp = CollectionUtils.putLinked(tmp, dependency.getGav().toGa(), dependency);
                }
                tmp = CollectionUtils.putLinked(tmp, entry.getKey(), entry.getValue());
                i += 1;
            }
            fpDeps = tmp;
        }
        return (B) this;
    }
}
