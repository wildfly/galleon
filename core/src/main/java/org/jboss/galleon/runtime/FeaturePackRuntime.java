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

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.galleon.Constants;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ArtifactCoords.Gav;
import org.jboss.galleon.spec.FeaturePackSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.state.FeaturePack;
import org.jboss.galleon.xml.FeatureSpecXmlParser;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeaturePackRuntime implements FeaturePack<PackageRuntime> {

    static FeaturePackRuntimeBuilder builder(FeaturePackSpec spec, Path dir) {
        return new FeaturePackRuntimeBuilder(spec, dir);
    }

    private final ProvisioningRuntime runtime;
    private final FeaturePackSpec spec;
    private final Path dir;
    private final Map<String, PackageRuntime> packages;
    private final Map<String, ResolvedFeatureSpec> featureSpecs;

    FeaturePackRuntime(FeaturePackRuntimeBuilder builder, ProvisioningRuntime runtime) throws ProvisioningException {
        this.runtime = runtime;
        this.spec = builder.spec;
        this.dir = builder.dir;
        this.featureSpecs = builder.featureSpecs;

        Map<String, PackageRuntime> tmpPackages = new LinkedHashMap<>();
        for(String pkgName : builder.pkgOrder) {
            final PackageRuntime.Builder pkgRtBuilder = builder.pkgBuilders.get(pkgName);
            tmpPackages.put(pkgName, pkgRtBuilder.build(this));
        }

        packages = Collections.unmodifiableMap(tmpPackages);
    }

    public ProvisioningRuntime getProvisioningRuntime() {
        return runtime;
    }

    public FeaturePackSpec getSpec() {
        return spec;
    }

    @Override
    public Gav getGav() {
        return spec.getGav();
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

    public FeatureSpec getFeatureSpec(String name) throws ProvisioningDescriptionException {
        if (featureSpecs.containsKey(name)) {
            return featureSpecs.get(name).xmlSpec;
        }
        final Path specXml = dir.resolve(Constants.FEATURES).resolve(name).resolve(Constants.SPEC_XML);
        if (Files.exists(specXml)) {
            try (BufferedReader reader = Files.newBufferedReader(specXml)) {
                return FeatureSpecXmlParser.getInstance().parse(reader);
            } catch (Exception e) {
                throw new ProvisioningDescriptionException(Errors.parseXml(specXml), e);
            }
        }
        return null;
    }

    public ResolvedFeatureSpec getResolvedFeatureSpec(String name) throws ProvisioningDescriptionException {
        return featureSpecs.get(name);
    }

    /**
     * Returns a resource path for a feature-pack.
     *
     * @param path  path to the resource relative to the feature-pack resources directory
     * @return  file-system path for the resource
     * @throws ProvisioningDescriptionException  in case the feature-pack was not found in the layout
     */
    public Path getResource(String... path) throws ProvisioningDescriptionException {
        if(path.length == 0) {
            throw new IllegalArgumentException("Resource path is null");
        }
        if(path.length == 1) {
            return dir.resolve(Constants.RESOURCES).resolve(path[0]);
        }
        Path p = dir.resolve(Constants.RESOURCES);
        for(String name : path) {
            p = p.resolve(name);
        }
        return p;
    }
}
