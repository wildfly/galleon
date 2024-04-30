/*
 * Copyright 2016-2024 Red Hat, Inc. and/or its affiliates
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jboss.galleon.BaseErrors;

import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.spec.ConfigLayerSpec;
import org.jboss.galleon.spec.FeaturePackSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.spec.PackageDependencySpec;
import org.jboss.galleon.spec.PackageSpec;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.util.CollectionUtils;

/**
 *
 * This class combines the feature-pack and the package specs that belong
 * to the feature-pack.
 *
 * @author Alexey Loubyansky
 */
public class FeaturePackDescription {

    public static class Builder {

        private final FeaturePackLocation.FPID fpid;
        private final FeaturePackSpec.Builder spec;
        private Map<String, PackageSpec> packages = Collections.emptyMap();
        private Map<String, ConfigLayerSpec> layers = Collections.emptyMap();
        private Map<String, FeatureSpec> features = Collections.emptyMap();
        private Map<String, Map<String, ConfigModel>> configModels = Collections.emptyMap();

        private Builder(FeaturePackLocation.FPID fpid, FeaturePackSpec.Builder spec) {
            this.fpid = fpid;
            this.spec = spec;
        }

        public Builder addPackage(PackageSpec pkg) {
            packages = CollectionUtils.put(packages, pkg.getName(), pkg);
            return this;
        }

        public Builder addLayer(ConfigLayerSpec layer) {
            layers = CollectionUtils.put(layers, layer.getName(), layer);
            return this;
        }

        public Builder addFeature(FeatureSpec spec) {
            features = CollectionUtils.put(features, spec.getName(), spec);
            return this;
        }

        public boolean hasPackage(String name) {
            return packages.containsKey(name);
        }

        public FeaturePackSpec.Builder getSpecBuilder() {
            return spec;
        }

        public FeaturePackDescription build() throws ProvisioningDescriptionException {
            return new FeaturePackDescription(this);
        }

        public Builder addConfigModel(String modelName, Map<String, ConfigModel> configs) {
            configModels = CollectionUtils.put(configModels, modelName, configs);
            return this;
        }
    }

    public static Builder builder(FeaturePackSpec.Builder spec) {
        return new Builder(spec.getFPID(), spec);
    }

    private final FeaturePackLocation.FPID fpid;
    private final FeaturePackSpec spec;
    private final Map<String, PackageSpec> packages;
    private final Map<String, ConfigLayerSpec> layers;
    private final Map<String, FeatureSpec> features;
    private final Map<String, Map<String, ConfigModel>> configModels;
    final List<String> unresolvedLocalPkgs;
    final boolean externalPkgDeps;

    private FeaturePackDescription(Builder builder) throws ProvisioningDescriptionException {
        fpid = builder.fpid;
        spec = builder.spec.build();
        this.packages = CollectionUtils.unmodifiable(builder.packages);
        for(String name : spec.getDefaultPackageNames()) {
            if(!packages.containsKey(name)) {
                throw new ProvisioningDescriptionException(Errors.unknownPackage(fpid, name));
            }
        }

        List<String> notFound = Collections.emptyList();
        boolean externalPkgDeps = false;
        // package dependency check
        if (!packages.isEmpty()) {
            for (PackageSpec pkg : packages.values()) {
                if (pkg.hasLocalPackageDeps()) {
                    for(PackageDependencySpec pkgDep : pkg.getLocalPackageDeps()) {
                        final PackageSpec depSpec = packages.get(pkgDep.getName());
                        if(depSpec == null) {
                            if(notFound.isEmpty()) {
                                notFound = new ArrayList<>();
                            }
                            notFound.add(pkgDep.getName());
                        }
                    }
                    if(!spec.hasFeaturePackDeps() && !notFound.isEmpty()) {
                        throw new ProvisioningDescriptionException(Errors.unsatisfiedPackageDependencies(fpid, pkg.getName(), notFound));
                    }
                }
                if(pkg.hasExternalPackageDeps()) {
                    for(String origin : pkg.getPackageOrigins()) {
                        try {
                            spec.getFeaturePackDep(origin);
                        } catch(ProvisioningDescriptionException e) {
                            throw new ProvisioningDescriptionException(BaseErrors.unknownFeaturePackDependencyName(fpid, pkg.getName(), origin), e);
                        }
                    }
                    externalPkgDeps = true;
                }
            }
        }
        this.layers = CollectionUtils.unmodifiable(builder.layers);
        this.features = CollectionUtils.unmodifiable(builder.features);
        this.configModels = CollectionUtils.unmodifiable(builder.configModels);
        this.externalPkgDeps = externalPkgDeps;
        this.unresolvedLocalPkgs = CollectionUtils.unmodifiable(notFound);
    }

    public FPID getFPID() {
        return fpid;
    }

    public FeaturePackSpec getSpec() {
        return spec;
    }

    public boolean hasPackages() {
        return !packages.isEmpty();
    }

    public boolean hasPackage(String name) {
        return packages.containsKey(name);
    }

    public PackageSpec getPackage(String name) {
        return packages.get(name);
    }

    public Set<String> getPackageNames() {
        return packages.keySet();
    }

    public Collection<PackageSpec> getPackages() {
        return packages.values();
    }
    public Collection<ConfigLayerSpec> getLayers() {
        return layers.values();
    }
    public Collection<FeatureSpec> getFeatures() {
        return features.values();
    }
    public Map<String, Map<String, ConfigModel>> getConfigs() {
        return configModels;
    }
}