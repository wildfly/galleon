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
package org.jboss.galleon.layout;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.spec.FeaturePackSpec;
import org.jboss.galleon.spec.PackageDependencySpec;
import org.jboss.galleon.spec.PackageSpec;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.ChannelSpec;
import org.jboss.galleon.util.CollectionUtils;

/**
 * This class describes a layout of feature-packs from which
 * the target installation is provisioned.
 *
 * @author Alexey Loubyansky
 */
public class ProvisioningLayout {

    public static class Builder {

        private Map<FeaturePackLocation.ChannelSpec, FeaturePackLayout> featurePacks = Collections.emptyMap();

        private Builder() {
        }

        public Builder addFeaturePack(FeaturePackLayout fp) throws ProvisioningDescriptionException {
            assert fp != null : "fp is null";
            final FeaturePackLocation.ChannelSpec channel = fp.getFPID().getChannel();
            if(featurePacks.containsKey(channel)) {
                final FeaturePackLocation.FPID existingId = featurePacks.get(channel).getFPID();
                if(existingId.getBuild().equals(fp.getFPID().getBuild())) {
                    return this;
                }
                throw new ProvisioningDescriptionException(Errors.featurePackVersionConflict(fp.getFPID(), existingId));
            }
            featurePacks = CollectionUtils.put(featurePacks, channel, fp);
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
                                throw new ProvisioningDescriptionException(Errors.unknownFeaturePackDependencyName(fp.getFPID(), pkg.getName(), origin), e);
                            }
                            final FeaturePackLayout fpDepLayout = featurePacks.get(fpDepConfig.getLocation().getChannel());
                            if (fpDepLayout == null) {
                                throw new ProvisioningDescriptionException(Errors.unknownFeaturePack(fpDepConfig.getLocation().getFPID()));
                            }
                            for (PackageDependencySpec pkgDep : pkg.getExternalPackageDeps(origin)) {
                                final String pkgDepName = pkgDep.getName();
                                if (!fpDepLayout.hasPackage(pkgDepName)) {
                                    throw new ProvisioningDescriptionException(Errors.unsatisfiedExternalPackageDependency(
                                            fp.getFPID(), pkg.getName(), fpDepConfig.getLocation().getFPID(), pkgDep.getName()));
                                }
                                if (fpDepConfig.isPackageExcluded(pkgDepName) && !pkgDep.isOptional()) {
                                    throw new ProvisioningDescriptionException(Errors.unsatisfiedExternalPackageDependency(
                                            fp.getFPID(), pkg.getName(), fpDepConfig.getLocation().getFPID(), pkgDep.getName()));
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

    private final Map<FeaturePackLocation.ChannelSpec, FeaturePackLayout> featurePacks;

    private ProvisioningLayout(Builder builder) {
        featurePacks = CollectionUtils.unmodifiable(builder.featurePacks);
    }

    public boolean hasFeaturePacks() {
        return !featurePacks.isEmpty();
    }

    public boolean contains(ChannelSpec channel) {
        return featurePacks.containsKey(channel);
    }

    public FeaturePackLayout getFeaturePack(ChannelSpec channel) {
        return featurePacks.get(channel);
    }

    public Collection<FeaturePackLayout> getFeaturePacks() {
        return featurePacks.values();
    }
}
