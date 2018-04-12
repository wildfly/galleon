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
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.util.CollectionUtils;

/**
 * This class represents a feature-pack configuration to be installed.
 *
 * @author Alexey Loubyansky
 */
public class FeaturePackConfig extends ConfigCustomizations {

    public static class Builder extends ConfigCustomizationsBuilder<Builder> {

        protected final ArtifactCoords.Gav gav;
        protected boolean inheritPackages = true;
        protected Set<String> excludedPackages = Collections.emptySet();
        protected Map<String, PackageConfig> includedPackages = Collections.emptyMap();

        protected Builder(ArtifactCoords.Gav gav) {
            this(gav, true);
        }

        protected Builder(ArtifactCoords.Gav gav, boolean inheritPackages) {
            this.gav = gav;
            this.inheritPackages = inheritPackages;
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

        public FeaturePackConfig build() {
            return new FeaturePackConfig(this);
        }
    }

    public static Builder builder(ArtifactCoords.Ga ga) {
        return new Builder(ga.toGav());
    }

    public static Builder builder(ArtifactCoords.Gav gav) {
        return new Builder(gav);
    }

    public static Builder builder(ArtifactCoords.Gav gav, boolean inheritPackageSet) {
        return new Builder(gav, inheritPackageSet);
    }

    public static FeaturePackConfig forGav(ArtifactCoords.Gav gav) {
        return new Builder(gav).build();
    }

    public static String getDefaultOriginName(ArtifactCoords.Gav gav) {
        return gav.toGa().toString();
    }

    private final ArtifactCoords.Gav gav;
    protected final boolean inheritPackages;
    protected final Set<String> excludedPackages;
    protected final Map<String, PackageConfig> includedPackages;
    private final Builder builder;

    protected FeaturePackConfig(Builder builder) {
        super(builder);
        assert builder.gav != null : "gav is null";
        this.gav = builder.gav;
        this.inheritPackages = builder.inheritPackages;
        this.excludedPackages = CollectionUtils.unmodifiable(builder.excludedPackages);
        this.includedPackages = CollectionUtils.unmodifiable(builder.includedPackages);
        this.builder = builder;
    }

    public Builder getBuilder() {
        return builder;
    }

    public ArtifactCoords.Gav getGav() {
        return gav;
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
        result = prime * result + ((gav == null) ? 0 : gav.hashCode());
        result = prime * result + ((includedPackages == null) ? 0 : includedPackages.hashCode());
        result = prime * result + (inheritPackages ? 1231 : 1237);
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
        if (gav == null) {
            if (other.gav != null)
                return false;
        } else if (!gav.equals(other.gav))
            return false;
        if (includedPackages == null) {
            if (other.includedPackages != null)
                return false;
        } else if (!includedPackages.equals(other.includedPackages))
            return false;
        if (inheritPackages != other.inheritPackages)
            return false;
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("[").append(gav.toString());
        append(builder);
        return builder.append("]").toString();
    }
}
