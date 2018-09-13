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
package org.jboss.galleon.spec;

import java.util.Collections;
import java.util.Set;

import org.jboss.galleon.ProvisioningDescriptionException;
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

    public static class Builder extends FeaturePackDepsConfigBuilder<Builder> {

        private FPID fpid;
        private Set<String> defPackages = Collections.emptySet();
        private FPID patchFor;

        protected Builder() {
        }

        public Builder setFPID(FPID fpid) {
            this.fpid = fpid;
            return this;
        }

        public FPID getFPID() {
            return fpid;
        }

        public Builder setPatchFor(FPID patchFor) {
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

        public FeaturePackSpec build() throws ProvisioningDescriptionException {
            try {
                return new FeaturePackSpec(this);
            } catch(ProvisioningDescriptionException e) {
                throw new ProvisioningDescriptionException("Failed to build feature-pack spec for " + fpid, e);
            }
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
    private final FPID patchFor;

    protected FeaturePackSpec(Builder builder) throws ProvisioningDescriptionException {
        super(builder);
        this.fpid = builder.fpid;
        this.defPackages = CollectionUtils.unmodifiable(builder.defPackages);
        this.patchFor = builder.patchFor;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((defPackages == null) ? 0 : defPackages.hashCode());
        result = prime * result + ((fpid == null) ? 0 : fpid.hashCode());
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
            buf.append("; dependencies: ");
            StringUtils.append(buf, fpDeps.keySet());
        }
        if(!definedConfigs.isEmpty()) {
            buf.append("; defaultConfigs: ");
            StringUtils.append(buf, definedConfigs.values());
        }
        if(!defPackages.isEmpty()) {
            buf.append("; defaultPackages: ");
            StringUtils.append(buf, defPackages);
        }
        return buf.append("]").toString();
    }
}
