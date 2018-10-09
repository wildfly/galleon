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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.util.CollectionUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class PackageDepsSpecBuilder<T extends PackageDepsSpecBuilder<T>> {

    protected Map<String, PackageDependencySpec> localPkgDeps = Collections.emptyMap();
    protected Map<String, Map<String, PackageDependencySpec>> externalPkgDeps = Collections.emptyMap();
    protected int requiredDeps;

    public T addPackageDep(String packageName) {
        return addPackageDep(packageName, false);
    }

    public T addPackageDep(String packageName, boolean optional) {
        return addPackageDep(optional ? PackageDependencySpec.optional(packageName) : PackageDependencySpec.required(packageName));
    }

    public T addPackageDep(String packageName, int type) throws ProvisioningDescriptionException {
        return addPackageDep(PackageDependencySpec.newInstance(packageName, type));
    }

    @SuppressWarnings("unchecked")
    public T addPackageDep(PackageDependencySpec dep) {
        localPkgDeps = CollectionUtils.putLinked(localPkgDeps, dep.getName(), dep);
        if(!dep.isOptional()) {
            ++requiredDeps;
        }
        return (T) this;
    }

    public T addPackageDep(String origin, String packageName) {
        return addPackageDep(origin, packageName, false);
    }

    public T addPackageDep(String origin, String packageName, boolean optional) {
        return addPackageDep(origin, optional ? PackageDependencySpec.optional(packageName) : PackageDependencySpec.required(packageName));
    }

    public T addPackageDep(String origin, String packageName, int type) throws ProvisioningDescriptionException {
        return addPackageDep(origin, PackageDependencySpec.newInstance(packageName, type));
    }

    @SuppressWarnings("unchecked")
    public T addPackageDep(String origin, PackageDependencySpec dep) {
        if(origin == null) {
            return addPackageDep(dep);
        }
        if(!dep.isOptional()) {
            ++requiredDeps;
        }
        Map<String, PackageDependencySpec> deps = externalPkgDeps.get(origin);
        if(deps == null) {
            externalPkgDeps = CollectionUtils.put(externalPkgDeps, origin, Collections.singletonMap(dep.getName(), dep));
            return (T) this;
        }
        if(deps.size() == 1) {
            if(deps.containsKey(dep.getName())) {
                deps = Collections.singletonMap(origin, dep);
            } else {
                final Map.Entry<String, PackageDependencySpec> first = deps.entrySet().iterator().next();
                deps = new HashMap<>(2);
                deps.put(first.getKey(), first.getValue());
                deps.put(dep.getName(), dep);
            }
            if(externalPkgDeps.size() == 1) {
                externalPkgDeps = Collections.singletonMap(origin, deps);
            } else {
                externalPkgDeps.put(origin, deps);
            }
            return (T) this;
        }
        deps.put(dep.getName(), dep);
        return (T) this;
    }

    public boolean hasPackageDeps() {
        return localPkgDeps != null || !externalPkgDeps.isEmpty();
    }

    protected List<PackageDependencySpec> buildLocalPackageDeps() {
        return getValueList(localPkgDeps);
    }

    protected Map<String, List<PackageDependencySpec>> buildExternalPackageDeps() {
        if(externalPkgDeps.isEmpty()) {
            return Collections.emptyMap();
        }
        if(externalPkgDeps.size() == 1) {
            final Map.Entry<String, Map<String, PackageDependencySpec>> first = externalPkgDeps.entrySet().iterator().next();
            return Collections.singletonMap(first.getKey(), getValueList(first.getValue()));
        }
        final Map<String, List<PackageDependencySpec>> tmp = new HashMap<>(externalPkgDeps.size());
        for (Map.Entry<String, Map<String, PackageDependencySpec>> externalEntry : externalPkgDeps.entrySet()) {
            tmp.put(externalEntry.getKey(), getValueList(externalEntry.getValue()));
        }
        return CollectionUtils.unmodifiable(tmp);
    }

    private static List<PackageDependencySpec> getValueList(Map<String, PackageDependencySpec> localPkgDeps) {
        final List<PackageDependencySpec> list;
        if(localPkgDeps.isEmpty()) {
            list = Collections.emptyList();
        } else if(localPkgDeps.size() == 1) {
            list = Collections.singletonList(localPkgDeps.entrySet().iterator().next().getValue());
        } else {
            final List<PackageDependencySpec> tmp = new ArrayList<>(localPkgDeps.size());
            for(Map.Entry<String, PackageDependencySpec> entry : localPkgDeps.entrySet()) {
                tmp.add(entry.getValue());
            }
            list = Collections.unmodifiableList(tmp);
        }
        return list;
    }
}