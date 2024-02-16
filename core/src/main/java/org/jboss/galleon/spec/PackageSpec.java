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
package org.jboss.galleon.spec;

import org.jboss.galleon.Stability;

/**
 * This class describes a package as it appears in a feature-pack specification.
 *
 * @author Alexey Loubyansky
 */
public class PackageSpec extends PackageDepsSpec {

    public static PackageSpec forName(String name) {
        return new PackageSpec(name);
    }

    public static class Builder extends PackageDepsSpecBuilder<Builder> {

        private String name;
        private Stability stability;

        protected Builder() {
            this(null);
        }

        protected Builder(String name) {
            this.name = name;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }
        public Builder setStability(String stability) {
            if (stability != null) {
                this.stability = Stability.fromString(stability);
            }
            return this;
        }
        public Builder setStability(Stability stability) {
            this.stability = stability;
            return this;
        }

        public PackageSpec build() {
            return new PackageSpec(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    private final String name;
    private final Stability stability;

    protected PackageSpec(String name) {
        super();
        this.name = name;
        this.stability = null;
    }

    protected PackageSpec(Builder builder) {
        super(builder);
        this.name = builder.name;
        this.stability = builder.stability;
    }

    public String getName() {
        return name;
    }

    public Stability getStability() {
        return stability;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        PackageSpec other = (PackageSpec) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append('[').append(name);
        if(stability != null) {
            buf.append(" stability=").append(stability);
        }
        if(!localPkgDeps.isEmpty()) {
            buf.append(" depends on ").append(localPkgDeps);
        }
        if(!externalPkgDeps.isEmpty()) {
            buf.append(", ").append(externalPkgDeps);
        }
        buf.append(']');
        return buf.toString();
    }
}
