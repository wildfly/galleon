/*
 * Copyright 2016-2025 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.spec;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.Stability;
import org.jboss.galleon.config.FeaturePackDepsConfig;
import org.jboss.galleon.config.FeaturePackDepsConfigBuilder;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.UniverseSpec;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.StringUtils;

/**
 * This class describes the feature-pack as it is available in the repository.
 *
 * @author Alexey Loubyansky
 */
public class FeaturePackSpec extends FeaturePackDepsConfig {

    public static class Family {

        public static class Criteria {

            private final String name;
            private final boolean inherited;

            public Criteria(String name, boolean inherited) {
                Objects.requireNonNull(name);
                this.name = name;
                this.inherited = inherited;
            }

            public String getName() {
                return name;
            }

            public boolean isInherited() {
                return inherited;
            }

            @Override
            public String toString() {
                return name + (inherited ? "[inherited]" : "");
            }

            @Override
            public boolean equals(Object other) {
                if (!(other instanceof Criteria)) {
                    return false;
                }
                Criteria otherCriteria = (Criteria) other;
                return name.equals(otherCriteria.name);
            }

            @Override
            public int hashCode() {
                int hash = 3;
                hash = 71 * hash + Objects.hashCode(this.name);
                return hash;
            }

        }
        private final String name;
        private final Set<Criteria> criteria;
        private final Set<Criteria> localCriteria;

        public Family(String name, Set<Criteria> criteria) throws ProvisioningDescriptionException {
            Objects.requireNonNull(name);
            Objects.requireNonNull(criteria);
            if (criteria.isEmpty()) {
                throw new ProvisioningDescriptionException("The set of criteria is empty, at least one criteria is expected");
            }
            this.name = name;
            this.criteria = Collections.unmodifiableSet(criteria);
            localCriteria = criteria.stream().filter((c) -> !c.inherited).
                    sorted((c1, c2) -> c1.name.compareTo(c2.name)).
                    collect(Collectors.toCollection(LinkedHashSet::new));
        }

        public static Family fromString(String dep) throws ProvisioningDescriptionException {
            dep = dep.trim();
            int familySeperatorIndex = dep.indexOf(":");
            if (familySeperatorIndex <= 0) {
                throw new ProvisioningDescriptionException("Invalid family string, no family name in " + dep);
            }
            if (dep.endsWith("+")) {
                throw new ProvisioningDescriptionException("Invalid family string, empty criteria in " + dep);
            }
            String name = dep.substring(0, familySeperatorIndex);
            String[] split = dep.substring(familySeperatorIndex + 1).split("\\+");
            if (split.length == 0) {
                throw new ProvisioningDescriptionException("Invalid family string, no criteria in " + dep);
            }
            Set<Criteria> criteria = new HashSet<>();
            for (int i = 0; i < split.length; i++) {
                String c = split[i].trim();
                if(c.isEmpty()) {
                    throw new ProvisioningDescriptionException("Invalid family string, empty criteria in " + dep);
                }
                criteria.add(new Criteria(split[i], false));
            }
            return new Family(name, criteria);
        }

        public String getName() {
            return name;
        }

        public Set<Criteria> getCriteria() {
            return criteria;
        }

        public Set<Criteria> getLocalCriteria() {
            return localCriteria;
        }

        // Return a cannonical form, ignoring inherited criteria
        public String getMemberFamilyID() {
            StringBuilder str = new StringBuilder();
            str.append(name);
            for (Criteria c : criteria) {
                if (!str.isEmpty()) {
                    str.append("+");
                }
                str.append(c);
            }
            return str.toString();
        }

        @Override
        public String toString() {
            StringBuilder str = new StringBuilder();
            str.append(name);
            str.append(":");
            StringBuilder criteriaBuilder = new StringBuilder();
            for (Criteria c : criteria) {
                if (!criteriaBuilder.isEmpty()) {
                    criteriaBuilder.append("+");
                }
                criteriaBuilder.append(c);
            }
            str.append(criteriaBuilder.toString());
            return str.toString();
        }
    }

    public static class Builder extends FeaturePackDepsConfigBuilder<Builder> {

        private FPID fpid;
        private Set<String> defPackages = Collections.emptySet();
        private FPID patchFor;
        private Map<String, FeaturePackPlugin> plugins = Collections.emptyMap();
        private Set<String> systemPaths = Collections.emptySet();
        private String galleonMinVersion;
        private Stability configStability;
        private Stability packageStability;
        private Family family;

        protected Builder() {
        }

        public Builder setFPID(FPID fpid) {
            this.fpid = fpid;
            return this;
        }

        public Builder setGalleonMinVersion(String version) {
            this.galleonMinVersion = version;
            return this;
        }

        public Builder setFamily(Family family) {
            this.family = family;
            return this;
        }

        public String getGalleonMinVersion() {
            return galleonMinVersion;
        }

        public Builder setConfigStability(String configLevel) {
            if (configLevel != null) {
                this.configStability = Stability.fromString(configLevel);
            }
            return this;
        }

        public Builder setConfigStability(Stability configLevel) {
            this.configStability = configLevel;
            return this;
        }

        public Stability getConfigStability() {
            return configStability;
        }

        public Builder setPackageStability(String packageLevel) {
            if (packageLevel != null) {
                this.packageStability = Stability.fromString(packageLevel);
            }
            return this;
        }

        public Builder setPackageStability(Stability packageLevel) {
            this.packageStability = packageLevel;
            return this;
        }

        public Stability getPackageStability() {
            return packageStability;
        }

        public FPID getFPID() {
            return fpid;
        }

        public Builder setPatchFor(FPID patchFor) {
            if(patchFor == null) {
                this.patchFor = null;
                return this;
            }
            if(patchFor.getBuild() == null) {
                throw new IllegalArgumentException("FPID is missing build number");
            }
            this.patchFor = patchFor;
            return this;
        }

        @Override
        public boolean hasDefaultUniverse() {
            return true;
        }

        @Override
        public UniverseSpec getDefaultUniverse() {
            return this.defaultUniverse == null ? fpid.getLocation().getUniverse() : this.defaultUniverse;
        }

        public Builder addDefaultPackage(String packageName) {
            assert packageName != null : "packageName is null";
            defPackages = CollectionUtils.addLinked(defPackages, packageName);
            return this;
        }

        public Builder addDefaultPackages(Set<String> packageNames) {
            assert packageNames != null : "packageNames is null";
            if(!packageNames.isEmpty()) {
                defPackages = CollectionUtils.addAllLinked(defPackages, packageNames);
            }
            return this;
        }

        public Builder addPlugin(FeaturePackPlugin plugin) {
            plugins = CollectionUtils.putLinked(plugins, plugin.getId(), plugin);
            return this;
        }

        public FeaturePackSpec build() throws ProvisioningDescriptionException {
            try {
                return new FeaturePackSpec(this);
            } catch(ProvisioningDescriptionException e) {
                throw new ProvisioningDescriptionException("Failed to build feature-pack spec for " + fpid, e);
            }
        }

        public Builder addSystemPaths(String systemPath) {
            systemPaths = CollectionUtils.add(systemPaths, systemPath);
            return this;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(FPID fpid) {
        return new Builder().setFPID(fpid);
    }

    private final FPID fpid;
    private final Set<String> defPackages;
    private final Map<String, FeaturePackPlugin> plugins;
    private final FPID patchFor;
    private final Set<String> systemPaths;
    private final String galleonMinVersion;
    private final Stability configStability;
    private final Stability packageStability;
    private final Family family;

    protected FeaturePackSpec(Builder builder) throws ProvisioningDescriptionException {
        super(builder);
        this.fpid = builder.fpid;
        this.defPackages = CollectionUtils.unmodifiable(builder.defPackages);
        this.plugins = CollectionUtils.unmodifiable(builder.plugins);
        this.patchFor = builder.patchFor;
        this.systemPaths = CollectionUtils.unmodifiable(builder.systemPaths);
        this.galleonMinVersion = builder.galleonMinVersion;
        this.configStability = builder.configStability == null ? null : builder.configStability;
        this.packageStability = builder.packageStability == null ? null : builder.packageStability;
        this.family = builder.family;
    }

    public Family getFamily() {
        return family;
    }

    public String getGalleonMinVersion() {
        return galleonMinVersion;
    }

    public Stability getConfigStability() {
        return configStability;
    }

    public Stability getPackageStability() {
        return packageStability;
    }

    public FPID getFPID() {
        return fpid;
    }

    public boolean isPatch() {
        return patchFor != null;
    }

    public FPID getPatchFor() {
        return patchFor;
    }

    public boolean hasDefaultPackages() {
        return !defPackages.isEmpty();
    }

    public Set<String> getDefaultPackageNames() {
        return defPackages;
    }

    public boolean isDefaultPackage(String name) {
        return defPackages.contains(name);
    }

    public boolean hasPlugins() {
        return !plugins.isEmpty();
    }

    public boolean hasFamily() {
        return family != null;
    }

    public Map<String, FeaturePackPlugin> getPlugins() {
        return plugins;
    }

    public boolean hasSystemPaths() {
        return !systemPaths.isEmpty();
    }

    public Set<String> getSystemPaths() {
        return systemPaths;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((defPackages == null) ? 0 : defPackages.hashCode());
        result = prime * result + ((fpid == null) ? 0 : fpid.hashCode());
        result = prime * result + ((patchFor == null) ? 0 : patchFor.hashCode());
        result = prime * result + ((plugins == null) ? 0 : plugins.hashCode());
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
        FeaturePackSpec other = (FeaturePackSpec) obj;
        if (defPackages == null) {
            if (other.defPackages != null)
                return false;
        } else if (!defPackages.equals(other.defPackages))
            return false;
        if (fpid == null) {
            if (other.fpid != null)
                return false;
        } else if (!fpid.equals(other.fpid))
            return false;
        if (patchFor == null) {
            if (other.patchFor != null)
                return false;
        } else if (!patchFor.equals(other.patchFor))
            return false;
        if (plugins == null) {
            if (other.plugins != null)
                return false;
        } else if (!plugins.equals(other.plugins))
            return false;
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append('[').append(fpid);
        if(patchFor != null) {
            buf.append(" patch-for=").append(patchFor);
        }
        if(!fpDeps.isEmpty()) {
            StringUtils.append(buf.append(" dependencies="), fpDeps.keySet());
        }
        if(!definedConfigs.isEmpty()) {
            StringUtils.append(buf.append(" defaultConfigs="), definedConfigs.values());
        }
        if(!defPackages.isEmpty()) {
            StringUtils.append(buf.append(" defaultPackages="), defPackages);
        }
        if(!plugins.isEmpty()) {
            StringUtils.append(buf.append(" plugins="), plugins.values());
        }
        return buf.append("]").toString();
    }
}
