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
package org.jboss.galleon.runtime;

import java.nio.file.Path;

import org.jboss.galleon.Constants;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.spec.PackageSpec;
import org.jboss.galleon.state.ProvisionedPackage;

/**
 *
 * @author Alexey Loubyansky
 */
public class PackageRuntime implements ProvisionedPackage {

    static class Builder {
        final Path dir;
        final PackageSpec spec;

        private Builder(PackageSpec spec, Path dir) {
            this.dir = dir;
            this.spec = spec;
        }

        PackageRuntime build() {
            return new PackageRuntime(this);
        }
    }

    static Builder builder(PackageSpec spec, Path dir) {
        return new Builder(spec, dir);
    }

    private final PackageSpec spec;
    private final Path layoutDir;

    private PackageRuntime(Builder builder) {
        this.spec = builder.spec;
        this.layoutDir = builder.dir;
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
     * @param fpGav  GAV of the feature-pack containing the package
     * @param pkgName  name of the package
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
