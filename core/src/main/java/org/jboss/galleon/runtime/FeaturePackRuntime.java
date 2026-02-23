/*
 * Copyright 2016-2026 Red Hat, Inc. and/or its affiliates
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.Stability;
import org.jboss.galleon.layout.FeaturePackLayout;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.spec.PackageDependencySpec;
import org.jboss.galleon.state.FeaturePack;
import org.jboss.galleon.api.GalleonFeaturePackRuntime;
import org.jboss.galleon.api.GalleonPackageRuntime;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeaturePackRuntime extends FeaturePackLayout implements FeaturePack<PackageRuntime>, GalleonFeaturePackRuntime {

    private final Map<String, PackageRuntime> packages;
    private final Map<String, ResolvedFeatureSpec> featureSpecs;
    private final Stability effectivePackageStability;
    private final Stability effectiveConfigStability;

    FeaturePackRuntime(FeaturePackRuntimeBuilder builder, ProvisioningRuntimeBuilder rt) throws ProvisioningException {
        super(builder.producer.getLocation().getFPID(), builder.getDir(), builder.getType());
        this.spec = builder.getSpec();
        this.featureSpecs = builder.featureSpecs == null ? Collections.emptyMap() : builder.featureSpecs;
        this.effectiveConfigStability = rt.getConfigStability(builder.getSpec().getConfigStability());
        this.effectivePackageStability = rt.getPackageStability(builder.getSpec().getPackageStability());
        final Map<String, PackageRuntime> tmpPackages = new LinkedHashMap<>(builder.pkgOrder.size());

        switch(rt.includedPkgDeps) {
            case ProvisioningRuntimeBuilder.PKG_DEP_ALL:
                for(String pkgName : builder.pkgOrder) {
                    tmpPackages.put(pkgName, builder.pkgBuilders.get(pkgName).build(this));
                }
                break;
            case ProvisioningRuntimeBuilder.PKG_DEP_PASSIVE_PLUS:
            case ProvisioningRuntimeBuilder.PKG_DEP_PASSIVE:
                final List<PackageRuntime> included = new ArrayList<>(builder.pkgOrder.size());
                int i;
                do {
                    i = included.size();
                    if(builder.pkgOrder.isEmpty()) {
                        break;
                    }
                    final Iterator<String> pkgNames = builder.pkgOrder.descendingIterator();
                    while(pkgNames.hasNext()) {
                        final String pkgName = pkgNames.next();
                        final PackageRuntime.Builder pkgBuilder = builder.pkgBuilders.get(pkgName);
                        if(pkgBuilder.isFlagOn(PackageRuntime.INCLUDED)) {
                            included.add(pkgBuilder.build(this));
                            pkgNames.remove();
                        } else if ((pkgBuilder.isFlagOn(PackageRuntime.PARENT_INCLUDED) || pkgBuilder.isFlagOn(PackageRuntime.ROOT))
                                && (rt.includedPkgDeps == ProvisioningRuntimeBuilder.PKG_DEP_PASSIVE
                                        && pkgBuilder.isPassiveWithSatisfiedDeps()
                                        || rt.includedPkgDeps == ProvisioningRuntimeBuilder.PKG_DEP_PASSIVE_PLUS
                                                && ((pkgBuilder.type & PackageDependencySpec.OPTIONAL) > 0
                                                        && pkgBuilder.type != PackageDependencySpec.PASSIVE
                                                        || pkgBuilder.isPassiveWithSatisfiedDeps()))) {
                            pkgBuilder.include();
                            included.add(pkgBuilder.build(this));
                            pkgNames.remove();
                        }
                    }
                } while(included.size() != i);

                while (--i >= 0) {
                    final PackageRuntime pkg = included.get(i);
                    tmpPackages.put(pkg.getName(), pkg);
                }
                break;
            case ProvisioningRuntimeBuilder.PKG_DEP_REQUIRED:
                for(String pkgName : builder.pkgOrder) {
                    final PackageRuntime.Builder pkgBuilder = builder.pkgBuilders.get(pkgName);
                    if(pkgBuilder.isFlagOn(PackageRuntime.INCLUDED)) {
                        tmpPackages.put(pkgName, pkgBuilder.build(this));
                    }
                }
                break;
            default:
                throw new ProvisioningException("Unexpected package dependency mask " + Integer.toBinaryString(rt.includedPkgDeps));
        }
        // Filter out the packages that are not at the right stability level
        final Map<String, PackageRuntime> filteredPackages = new LinkedHashMap<>(tmpPackages.size());
        Stability stability= rt.getPackageStability(getSpec().getPackageStability());
        for(Map.Entry<String, PackageRuntime> entry : tmpPackages.entrySet()) {
            Stability packageStability = entry.getValue().getSpec().getStability() == null ? Stability.DEFAULT : entry.getValue().getSpec().getStability();
            if (stability.enables(packageStability)) {
                filteredPackages.put(entry.getKey(), entry.getValue());
            } else {
                if (rt.getMessageWriter().isVerboseEnabled()) {
                    rt.getMessageWriter().verbose("Excluding package '" + entry.getKey() + "'. Its stability '" + packageStability + "' is lower than the expected '" + stability +"' stability");
                }
            }
        }
        packages = Collections.unmodifiableMap(filteredPackages);
    }

    public Set<String> getSystemPaths() {
        return spec.getSystemPaths();
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
    public Collection<PackageRuntime> getPackages() {
        return packages.values();
    }

    @Override
    public PackageRuntime getPackage(String name) {
        return packages.get(name);
    }

    @Override
    public GalleonPackageRuntime getGalleonPackage(String name) {
        return packages.get(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<GalleonPackageRuntime> getGalleonPackages() {
        List<GalleonPackageRuntime> lst = new ArrayList<>();
        for (PackageRuntime rt : packages.values()) {
            lst.add((GalleonPackageRuntime) rt);
        }
        return lst;
    }

    public Set<String> getFeatureSpecNames() {
        return featureSpecs.keySet();
    }

    public Collection<ResolvedFeatureSpec> getFeatureSpecs() {
        return featureSpecs.values();
    }

    public FeatureSpec getFeatureSpec(String name) throws ProvisioningException {
        if (featureSpecs.containsKey(name)) {
            return featureSpecs.get(name).xmlSpec;
        }
        return loadFeatureSpec(name);
    }

    public ResolvedFeatureSpec getResolvedFeatureSpec(String name) throws ProvisioningDescriptionException {
        return featureSpecs.get(name);
    }

    public Stability getPackageStability() {
        return effectivePackageStability;
    }

    public Stability getConfigStability() {
        return effectiveConfigStability;
    }
}
