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
package org.jboss.galleon.spec;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class PackageDepsSpec {

    protected final List<PackageDependencySpec> localPkgDeps;
    protected final Map<String, List<PackageDependencySpec>> externalPkgDeps;

    protected PackageDepsSpec() {
        localPkgDeps = Collections.emptyList();
        externalPkgDeps = Collections.emptyMap();
    }

    protected PackageDepsSpec(PackageDepsSpec src) {
        localPkgDeps = src.localPkgDeps;
        externalPkgDeps = src.externalPkgDeps;
    }

    protected PackageDepsSpec(PackageDepsSpecBuilder<?> builder) {
        this.localPkgDeps = builder.buildLocalPackageDeps();
        this.externalPkgDeps = builder.buildExternalPackageDeps();
    }

    public boolean hasPackageDeps() {
        return !(localPkgDeps.isEmpty() && externalPkgDeps.isEmpty());
    }

    public boolean hasLocalPackageDeps() {
        return !localPkgDeps.isEmpty();
    }

    public Collection<PackageDependencySpec> getLocalPackageDeps() {
        return localPkgDeps;
    }

    public boolean hasExternalPackageDeps() {
        return !externalPkgDeps.isEmpty();
    }

    public Collection<String> getPackageOrigins() {
        return externalPkgDeps.keySet();
    }

    public Collection<PackageDependencySpec> getExternalPackageDeps(String origin) {
        final List<PackageDependencySpec> fpDeps = externalPkgDeps.get(origin);
        return fpDeps == null ? Collections.emptyList() : fpDeps;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((externalPkgDeps == null) ? 0 : externalPkgDeps.hashCode());
        result = prime * result + ((localPkgDeps == null) ? 0 : localPkgDeps.hashCode());
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
        PackageDepsSpec other = (PackageDepsSpec) obj;
        if (externalPkgDeps == null) {
            if (other.externalPkgDeps != null)
                return false;
        } else if (!externalPkgDeps.equals(other.externalPkgDeps))
            return false;
        if (localPkgDeps == null) {
            if (other.localPkgDeps != null)
                return false;
        } else if (!localPkgDeps.equals(other.localPkgDeps))
            return false;
        return true;
    }
}
