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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.layout.FeaturePackLayout;
import org.jboss.galleon.spec.FeaturePackSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.state.FeaturePack;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeaturePackRuntime implements FeaturePack<PackageRuntime>, FeaturePackLayout {

    private final FPID fpid;
    private final FeaturePackSpec spec;
    private final Path dir;
    private final Map<String, PackageRuntime> packages;
    private final Map<String, ResolvedFeatureSpec> featureSpecs;
    private final int type;

    private ArtifactCoords.Gav legacyGav;

    FeaturePackRuntime(FeaturePackRuntimeBuilder builder) throws ProvisioningException {
        this.fpid = builder.producer.getLocation().getFPID();
        this.spec = builder.spec;
        this.dir = builder.dir;
        this.featureSpecs = builder.featureSpecs;
        this.type = builder.getType();

        Map<String, PackageRuntime> tmpPackages = new LinkedHashMap<>();
        for(String pkgName : builder.pkgOrder) {
            final PackageRuntime.Builder pkgRtBuilder = builder.pkgBuilders.get(pkgName);
            tmpPackages.put(pkgName, pkgRtBuilder.build(this));
        }

        packages = Collections.unmodifiableMap(tmpPackages);
    }

    @Override
    public FeaturePackSpec getSpec() {
        return spec;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public Path getDir() {
        return dir;
    }
//
//    /**
//     * @deprecated
//     *
//     * @return  Feature-pack Maven artifact GAV
//     */
//    public ArtifactCoords.Gav getGav() {
//        if(legacyGav == null) {
//            try {
//                legacyGav = LegacyGalleon1Universe.toArtifactCoords(fpid.getLocation()).toGav();
//            } catch (ProvisioningException e) {
//                throw new IllegalStateException("Failed to translate fpl to gav", e);
//            }
//        }
//        return legacyGav;
//    }

    @Override
    public FPID getFPID() {
        return fpid;
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
}
