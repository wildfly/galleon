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
package org.jboss.galleon.runtime;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.jboss.galleon.Constants;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.spec.PackageDependencySpec;
import org.jboss.galleon.spec.PackageSpec;
import org.jboss.galleon.state.ProvisionedPackage;
import org.jboss.galleon.util.CollectionUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class PackageRuntime implements ProvisionedPackage {

    static final int ON_DEP_BRANCH      = 0b000001;
    static final int SCHEDULED          = 0b000010;
    static final int INCLUDED           = 0b000100;
    static final int PARENT_INCLUDED    = 0b001000;
    static final int ROOT               = 0b010000;
    static final int VISITED            = 0b100000;

    static class Builder {
        final FeaturePackRuntimeBuilder fp;
        final Path dir;
        final PackageSpec spec;
        final int id;
        private int status;
        private List<PackageRuntime.Builder> requiredDeps = Collections.emptyList();
        private List<PackageRuntime.Builder> optionalDeps = Collections.emptyList();
        int type = PackageDependencySpec.OPTIONAL;

        private Builder(FeaturePackRuntimeBuilder fp, PackageSpec spec, Path dir, int id) {
            this.fp = fp;
            this.dir = dir;
            this.spec = spec;
            this.id = id;
        }

        boolean isFlagOn(int flag) {
            return (status & flag) > 0;
        }

        boolean setFlag(int flag) {
            if((status & flag) > 0) {
                return false;
            }
            status ^= flag;
            return true;
        }

        void clearFlag(int flag) {
            if((status & flag) > 0) {
                status ^= flag;
            }
        }

        void schedule() {
            if(setFlag(SCHEDULED)) {
                fp.pkgOrder.add(spec.getName());
            }
        }

        void referencedAs(int type) {
            if(type == PackageDependencySpec.PASSIVE) {
                this.type = type;
            }
        }

        void addPackageDep(PackageRuntime.Builder dep, int type, int includedOptionalDeps) {
            if(type == PackageDependencySpec.REQUIRED) {
                if (isFlagOn(INCLUDED)) {
                    dep.include();
                } else {
                    requiredDeps = CollectionUtils.add(requiredDeps, dep);
                }
                return;
            }

            if(includedOptionalDeps == ProvisioningRuntimeBuilder.PKG_DEP_REQUIRED ||
                 // special case in FeaturePackRuntime
                    includedOptionalDeps == ProvisioningRuntimeBuilder.PKG_DEP_ALL) {
                return;
            }

            if (includedOptionalDeps == ProvisioningRuntimeBuilder.PKG_DEP_PASSIVE_PLUS ||
                    type == PackageDependencySpec.PASSIVE
                    /* redundant check && includedOptionalDeps != ProvisioningRuntimeBuilder.PKG_DEP_REQUIRED*/) {
                optionalDeps = CollectionUtils.add(optionalDeps, dep);
                if (isFlagOn(INCLUDED)) {
                    dep.setFlag(PARENT_INCLUDED);
                }
            }
        }

        void include() {
            if(!setFlag(INCLUDED)) {
                return;
            }
            if(!requiredDeps.isEmpty()) {
                for (PackageRuntime.Builder dep : requiredDeps) {
                    dep.include();
                }
            }
            if(!optionalDeps.isEmpty()) {
                for (PackageRuntime.Builder dep : optionalDeps) {
                    dep.setFlag(PARENT_INCLUDED);
                }
            }
        }

        boolean isPassiveWithSatisfiedDeps() {
            if(type != PackageDependencySpec.PASSIVE) {
                return false;
            }
            final int specRequiredDeps = spec.getRequiredPackageDepsTotal();
            if(specRequiredDeps == 0 || !setFlag(VISITED)) {
                return true;
            }
            if(specRequiredDeps != requiredDeps.size()) {
                return false;
            }
            for(PackageRuntime.Builder dep : requiredDeps) {
                if(!dep.isFlagOn(INCLUDED)) {
                    return false;
                }
            }
            clearFlag(VISITED);
            return true;
        }

        PackageRuntime build(FeaturePackRuntime fp) {
            return new PackageRuntime(this, fp);
        }
    }

    static Builder builder(FeaturePackRuntimeBuilder fp, PackageSpec spec, Path dir, int id) {
        return new Builder(fp, spec, dir, id);
    }

    private final FeaturePackRuntime fp;
    private final PackageSpec spec;
    private final Path layoutDir;

    private PackageRuntime(Builder builder, FeaturePackRuntime fp) {
        this.fp = fp;
        this.spec = builder.spec;
        this.layoutDir = builder.dir;
    }

    public FeaturePackRuntime getFeaturePackRuntime() {
        return fp;
    }

    public PackageSpec getSpec() {
        return spec;
    }

    @Override
    public String getName() {
        return spec.getName();
    }

    /**
     * Returns a resource path for a package.
     *
     * @param path  path to the resource relative to the package resources directory
     * @return  file-system path for the resource
     * @throws ProvisioningDescriptionException  in case the feature-pack or package were not found in the layout
     */
    public Path getResource(String... path) throws ProvisioningDescriptionException {
        if(path.length == 0) {
            throw new IllegalArgumentException("Resource path is null");
        }
        if(path.length == 1) {
            return layoutDir.resolve(path[0]);
        }
        Path p = layoutDir;
        for(String name : path) {
            p = p.resolve(name);
        }
        return p;
    }

    public Path getContentDir() {
        return layoutDir.resolve(Constants.CONTENT);
    }
}
