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
package org.jboss.galleon.state;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.StringUtils;

/**
 * Describes a feature-pack as it was provisioned.
 *
 * @author Alexey Loubyansky
 */
public class ProvisionedFeaturePack implements FeaturePack<ProvisionedPackage> {

    public static class Builder {
        private ArtifactCoords.Gav gav;
        private Map<String, ProvisionedPackage> packages = Collections.emptyMap();

        private Builder(ArtifactCoords.Gav gav) {
            this.gav = gav;
        }

        public Builder addPackage(String name) {
            return addPackage(ProvisionedPackage.newInstance(name));
        }

        public Builder addPackage(ProvisionedPackage provisionedPkg) {
            packages = CollectionUtils.putLinked(packages, provisionedPkg.getName(), provisionedPkg);
            return this;
        }

        public boolean hasPackage(String name) {
            return packages.containsKey(name);
        }

        public ProvisionedFeaturePack build() {
            return new ProvisionedFeaturePack(gav, CollectionUtils.unmodifiable(packages));
        }
    }

    public static Builder builder(ArtifactCoords.Gav gav) {
        return new Builder(gav);
    }

    public static ProvisionedFeaturePack forGav(ArtifactCoords.Gav gav) {
        return new ProvisionedFeaturePack(gav, Collections.emptyMap());
    }

    private final ArtifactCoords.Gav gav;
    private final Map<String, ProvisionedPackage> packages;

    ProvisionedFeaturePack(ArtifactCoords.Gav gav, Map<String, ProvisionedPackage> packages) {
        this.gav = gav;
        this.packages = packages;
    }

    @Override
    public ArtifactCoords.Gav getGav() {
        return gav;
    }

    @Override
    public boolean hasPackages() {
        return !packages.isEmpty();
    }

    @Override
    public boolean containsPackage(String name) {
        return packages.containsKey(name);
    }

    @Override
    public Set<String> getPackageNames() {
        return packages.keySet();
    }

    @Override
    public Collection<ProvisionedPackage> getPackages() {
        return packages.values();
    }

    @Override
    public ProvisionedPackage getPackage(String name) {
        return packages.get(name);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((gav == null) ? 0 : gav.hashCode());
        result = prime * result + ((packages == null) ? 0 : packages.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProvisionedFeaturePack other = (ProvisionedFeaturePack) obj;
        if (gav == null) {
            if (other.gav != null)
                return false;
        } else if (!gav.equals(other.gav))
            return false;
        if (packages == null) {
            if (other.packages != null)
                return false;
        } else if (!packages.equals(other.packages))
            return false;
        return true;
    }

    public String toString() {
        final StringBuilder buf = new StringBuilder().append('[').append(gav);
        if(!packages.isEmpty()) {
            buf.append(' ');
            StringUtils.append(buf, packages.values());
        }
        return buf.append(']').toString();
    }
}
