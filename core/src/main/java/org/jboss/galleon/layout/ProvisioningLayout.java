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
package org.jboss.galleon.layout;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ArtifactCoords.Gav;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.spec.FeaturePackSpec;
import org.jboss.galleon.spec.PackageDependencySpec;
import org.jboss.galleon.spec.PackageSpec;
import org.jboss.galleon.util.CollectionUtils;

/**
 * This class describes a layout of feature-packs from which
 * the target installation is provisioned.
 *
 * @author Alexey Loubyansky
 */
public class ProvisioningLayout {

    public static class Builder {

        private Map<ArtifactCoords.Ga, FeaturePackLayout> featurePacks = Collections.emptyMap();

        private Builder() {
        }

        public Builder addFeaturePack(FeaturePackLayout fp) throws ProvisioningDescriptionException {
            assert fp != null : "fp is null";
            final ArtifactCoords.Ga fpGa = fp.getGav().toGa();
            if(featurePacks.containsKey(fpGa)) {
                final Gav existingGav = featurePacks.get(fpGa).getGav();
                if(existingGav.getVersion().equals(fp.getGav().getVersion())) {
                    return this;
                }
                throw new ProvisioningDescriptionException(Errors.featurePackVersionConflict(fp.getGav(), existingGav));
            }
            featurePacks = CollectionUtils.put(featurePacks, fpGa, fp);
            return this;
        }

        public ProvisioningLayout build() throws ProvisioningDescriptionException {
            for(FeaturePackLayout fp : featurePacks.values()) {
                if(!fp.externalPkgDeps) {
                    continue;
                }

                final FeaturePackSpec fpSpec = fp.getSpec();
                for (PackageSpec pkg : fp.getPackages()) {
                    if (pkg.hasExternalPackageDeps()) {
                        for (String origin : pkg.getPackageOrigins()) {
                            final FeaturePackConfig fpDepConfig;
                            try {
                                fpDepConfig = fpSpec.getFeaturePackDep(origin);
                            } catch (ProvisioningDescriptionException e) {
                                throw new ProvisioningDescriptionException(Errors.unknownFeaturePackDependencyName(fpSpec.getGav(), pkg.getName(), origin), e);
                            }
                            final FeaturePackLayout fpDepLayout = featurePacks.get(fpDepConfig.getGav().toGa());
                            if (fpDepLayout == null) {
                                throw new ProvisioningDescriptionException(Errors.unknownFeaturePack(fpDepConfig.getGav()));
                            }
                            for (PackageDependencySpec pkgDep : pkg.getExternalPackageDeps(origin)) {
                                final String pkgDepName = pkgDep.getName();
                                if (!fpDepLayout.hasPackage(pkgDepName)) {
                                    throw new ProvisioningDescriptionException(Errors.unsatisfiedExternalPackageDependency(
                                            fpSpec.getGav(), pkg.getName(), fpDepConfig.getGav(), pkgDep.getName()));
                                }
                                if (fpDepConfig.isPackageExcluded(pkgDepName) && !pkgDep.isOptional()) {
                                    throw new ProvisioningDescriptionException(Errors.unsatisfiedExternalPackageDependency(
                                            fpSpec.getGav(), pkg.getName(), fpDepConfig.getGav(), pkgDep.getName()));
                                }
                            }
                        }
                    }
                }

                // TODO verify not resolved packages
            }
            return new ProvisioningLayout(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final Map<ArtifactCoords.Ga, FeaturePackLayout> featurePacks;

    private ProvisioningLayout(Builder builder) {
        featurePacks = CollectionUtils.unmodifiable(builder.featurePacks);
    }

    public boolean hasFeaturePacks() {
        return !featurePacks.isEmpty();
    }

    public boolean contains(ArtifactCoords.Gav fpGav) {
        return featurePacks.containsKey(fpGav.toGa());
    }

    public FeaturePackLayout getFeaturePack(ArtifactCoords.Gav fpGav) {
        return featurePacks.get(fpGav.toGa());
    }

    public Collection<FeaturePackLayout> getFeaturePacks() {
        return featurePacks.values();
    }
}
