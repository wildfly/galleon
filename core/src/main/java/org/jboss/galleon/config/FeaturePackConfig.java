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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.StringUtils;

/**
 * This class represents a feature-pack configuration to be installed.
 *
 * @author Alexey Loubyansky
 */
public class FeaturePackConfig extends ConfigCustomizations {

    public static class Builder extends ConfigCustomizationsBuilder<Builder> {

        protected final FeaturePackLocation fpl;
        protected boolean inheritPackages;
        protected boolean transitive;
        protected Set<String> excludedPackages = Collections.emptySet();
        protected Map<String, PackageConfig> includedPackages = Collections.emptyMap();
        protected Set<FPID> patches = Collections.emptySet();

        protected Builder(FeaturePackLocation fpl) {
            this(fpl, true);
        }

        protected Builder(FeaturePackLocation fpl, boolean inheritPackages) {
            this(fpl, inheritPackages, false);
        }

        protected Builder(FeaturePackLocation fpl, boolean inheritPackages, boolean transitive) {
            this.fpl = fpl;
            this.inheritPackages = inheritPackages;
            this.transitive = transitive;
        }

        protected Builder(FeaturePackConfig config) {
            this.fpl = config.getLocation();
            init(config);
        }

        public Builder init(FeaturePackConfig fpConfig) {
            super.initConfigs(fpConfig);
            inheritPackages = fpConfig.inheritPackages;
            excludedPackages = CollectionUtils.clone(fpConfig.excludedPackages);
            includedPackages = CollectionUtils.clone(fpConfig.includedPackages);
            transitive = fpConfig.transitive;
            if(!fpConfig.patches.isEmpty()) {
                if(fpConfig.patches.size() == 1) {
                    patches = Collections.singleton(fpConfig.patches.get(0));
                } else {
                    patches = new LinkedHashSet<>(fpConfig.patches.size());
                    for(FPID patchId : fpConfig.patches) {
                        patches.add(patchId);
                    }
                }
            }
            return this;
        }

        public Builder addPatch(FPID patchId) throws ProvisioningDescriptionException {
            final int size = patches.size();
            patches = CollectionUtils.addLinked(patches, patchId);
            if(size == patches.size()) {
                throw new ProvisioningDescriptionException("Patch " + patchId + " has already been configured for " + fpl);
            }
            return this;
        }

        public Builder removePatch(FPID patchId) throws ProvisioningDescriptionException {
            final int size = patches.size();
            patches = CollectionUtils.remove(patches, patchId);
            if(size == patches.size()) {
                throw new ProvisioningDescriptionException("Patch " + patchId + " was not configured for " + fpl);
            }
            return this;
        }

        public Builder setInheritPackages(boolean inheritSelectedPackages) {
            this.inheritPackages = inheritSelectedPackages;
            return this;
        }

        public Builder excludePackage(String packageName) throws ProvisioningDescriptionException {
            if(includedPackages.containsKey(packageName)) {
                throw new ProvisioningDescriptionException(Errors.packageExcludeInclude(packageName));
            }
            excludedPackages = CollectionUtils.add(excludedPackages, packageName);
            return this;
        }

        public Builder removeExcludedPackage(String pkg) throws ProvisioningDescriptionException {
            if (!excludedPackages.contains(pkg)) {
                throw new ProvisioningDescriptionException("Package " + pkg + " is not excluded from the configuration");
            }
            excludedPackages = CollectionUtils.remove(excludedPackages, pkg);
            return this;
        }

        public Builder excludeAllPackages(Collection<String> packageNames) throws ProvisioningDescriptionException {
            for(String packageName : packageNames) {
                excludePackage(packageName);
            }
            return this;
        }

        public boolean isPackageExcluded(String packageName) {
            return excludedPackages.contains(packageName);
        }

        public Builder includeAllPackages(Collection<PackageConfig> packageConfigs) throws ProvisioningDescriptionException {
            for(PackageConfig packageConfig : packageConfigs) {
                includePackage(packageConfig);
            }
            return this;
        }

        public Builder includePackage(String packageName) throws ProvisioningDescriptionException {
            return includePackage(PackageConfig.forName(packageName));
        }

        public Builder removeIncludedPackage(String pkg) throws ProvisioningDescriptionException {
            if (!includedPackages.containsKey(pkg)) {
                throw new ProvisioningDescriptionException("Package " + pkg + " is not included into the configuration");
            }
            includedPackages = CollectionUtils.remove(includedPackages, pkg);
            return this;
        }

        private Builder includePackage(PackageConfig packageConfig) throws ProvisioningDescriptionException {
            if(excludedPackages.contains(packageConfig.getName())) {
                throw new ProvisioningDescriptionException(Errors.packageExcludeInclude(packageConfig.getName()));
            }
            includedPackages = CollectionUtils.put(includedPackages, packageConfig.getName(), packageConfig);
            return this;
        }

        public boolean isPackageIncluded(String packageName) {
            return includedPackages.containsKey(packageName);
        }

        @Override
        public Builder addConfig(ConfigModel config) throws ProvisioningDescriptionException {
            if(transitive) {
                transitiveDoesNotDefineConfigs();
            }
            return super.addConfig(config);
        }

        @Override
        public Builder removeConfig(ConfigId id) throws ProvisioningDescriptionException {
            if(transitive) {
                transitiveDoesNotDefineConfigs();
            }
            return super.removeConfig(id);
        }

        private void transitiveDoesNotDefineConfigs() throws ProvisioningDescriptionException {
            throw new ProvisioningDescriptionException("Transitive dependency does not define configs");
        }

        public FeaturePackConfig build() {
            return new FeaturePackConfig(this);
        }
    }

    /**
     * Creates a new config builder for feature-pack with the specified location
     *
     * @param fpl  feature-pack location
     * @return  feature-pack configuration
     */
    public static Builder builder(FeaturePackLocation fpl) {
        return new Builder(fpl);
    }

    public static Builder builder(FeaturePackLocation fpl, boolean inheritPackages) {
        return new Builder(fpl, inheritPackages);
    }

    /**
     * Creates the default configuration for a feature-pack
     *
     * @param fpl  feature-pack location
     * @return  feature-pack configuration
     */
    public static FeaturePackConfig forLocation(FeaturePackLocation fpl) {
        return new Builder(fpl).build();
    }

    /**
     * Creates a new config builder for a transitive feature-pack dependency
     * with the specified location
     *
     * @param fpl  feature-pack location of the transitive dependency
     * @return  feature-pack transitive dependency configuration
     */
    public static Builder transitiveBuilder(FeaturePackLocation fpl) {
        return new Builder(fpl, true, true);
    }

    public static Builder builder(FeaturePackConfig config) {
        return new Builder(config);
    }

    /**
     * Creates the default configuration for a transitive feature-pack dependency
     *
     * @param fpl  feature-pack location of the transitive dependency
     * @return  feature-pack transitive dependency configuration
     */
    public static FeaturePackConfig forTransitiveDep(FeaturePackLocation fpl) {
        return new Builder(fpl, true, true).build();
    }

    public static String getDefaultOriginName(FeaturePackLocation fpl) {
        return fpl.getProducer().toString();
    }

    private final FeaturePackLocation fpl;
    protected final boolean inheritPackages;
    protected final Set<String> excludedPackages;
    protected final Map<String, PackageConfig> includedPackages;
    protected final boolean transitive;
    protected final List<FPID> patches;
    private final Builder builder;

    protected FeaturePackConfig(Builder builder) {
        super(builder);
        assert builder.fpl != null : "location is null";
        this.fpl = builder.fpl;
        this.inheritPackages = builder.inheritPackages;
        this.excludedPackages = CollectionUtils.unmodifiable(builder.excludedPackages);
        this.includedPackages = CollectionUtils.unmodifiable(builder.includedPackages);
        this.transitive = builder.transitive;
        switch(builder.patches.size()) {
            case 0:
                patches = Collections.emptyList();
                break;
            case 1:
                patches = Collections.singletonList(builder.patches.iterator().next());
                break;
            default:
                final List<FPID> tmp = new ArrayList<>(builder.patches.size());
                for(FPID fpid : builder.patches) {
                    tmp.add(fpid);
                }
                patches = Collections.unmodifiableList(tmp);
        }
        this.builder = builder;
    }

    public Builder getBuilder() {
        return builder;
    }

    public FeaturePackLocation getLocation() {
        return fpl;
    }

    public boolean isTransitive() {
        return transitive;
    }

    public boolean hasPatches() {
        return !patches.isEmpty();
    }

    public List<FPID> getPatches() {
        return patches;
    }

    public boolean isInheritPackages() {
        return inheritPackages;
    }

    public boolean hasIncludedPackages() {
        return !includedPackages.isEmpty();
    }

    public boolean isPackageIncluded(String packageName) {
        return includedPackages.containsKey(packageName);
    }

    public Collection<PackageConfig> getIncludedPackages() {
        return includedPackages.values();
    }

    public boolean hasExcludedPackages() {
        return !excludedPackages.isEmpty();
    }

    public boolean isPackageExcluded(String packageName) {
        return excludedPackages.contains(packageName);
    }

    public Set<String> getExcludedPackages() {
        return excludedPackages;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((excludedPackages == null) ? 0 : excludedPackages.hashCode());
        result = prime * result + ((fpl == null) ? 0 : fpl.hashCode());
        result = prime * result + ((includedPackages == null) ? 0 : includedPackages.hashCode());
        result = prime * result + (inheritPackages ? 1231 : 1237);
        result = prime * result + ((patches == null) ? 0 : patches.hashCode());
        result = prime * result + (transitive ? 1231 : 1237);
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
        FeaturePackConfig other = (FeaturePackConfig) obj;
        if (excludedPackages == null) {
            if (other.excludedPackages != null)
                return false;
        } else if (!excludedPackages.equals(other.excludedPackages))
            return false;
        if (fpl == null) {
            if (other.fpl != null)
                return false;
        } else if (!fpl.equals(other.fpl))
            return false;
        if (includedPackages == null) {
            if (other.includedPackages != null)
                return false;
        } else if (!includedPackages.equals(other.includedPackages))
            return false;
        if (inheritPackages != other.inheritPackages)
            return false;
        if (patches == null) {
            if (other.patches != null)
                return false;
        } else if (!patches.equals(other.patches))
            return false;
        if (transitive != other.transitive)
            return false;
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append('[');
        if(transitive) {
            buf.append("transitive ");
        }
        buf.append(fpl.toString());
        if(!patches.isEmpty()) {
            buf.append(" patches=");
            StringUtils.append(buf, patches);
        }
        append(buf);
        return buf.append(']').toString();
    }
}
