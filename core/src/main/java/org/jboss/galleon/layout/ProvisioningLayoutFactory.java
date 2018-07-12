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

package org.jboss.galleon.layout;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.layout.ProvisioningLayout.FeaturePackLayout;
import org.jboss.galleon.spec.FeaturePackSpec;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.Universe;
import org.jboss.galleon.universe.UniverseFeaturePackInstaller;
import org.jboss.galleon.universe.UniverseResolver;
import org.jboss.galleon.util.IoUtils;
import org.jboss.galleon.util.LayoutUtils;
import org.jboss.galleon.util.ZipUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class ProvisioningLayoutFactory implements Closeable {

    public static ProvisioningLayoutFactory getInstance() throws ProvisioningException {
        return getInstance(UniverseResolver.builder().build());
    }

    public static ProvisioningLayoutFactory getInstance(UniverseResolver universeResolver) {
        return new ProvisioningLayoutFactory(IoUtils.createRandomTmpDir(), universeResolver);
    }

    public static ProvisioningLayoutFactory getInstance(Path home, UniverseResolver universeResolver) {
        return new ProvisioningLayoutFactory(home, universeResolver);
    }

    private final Path home;
    private final UniverseResolver universeResolver;
    private AtomicInteger openHandles = new AtomicInteger();
    private Map<String, UniverseFeaturePackInstaller> universeInstallers;

    private ProvisioningLayoutFactory(Path home, UniverseResolver universeResolver) {
        this.home = home;
        this.universeResolver = universeResolver;
    }

    public UniverseResolver getUniverseResolver() {
        return universeResolver;
    }

    /**
     * Adds feature-pack archive to the local provisioning feature-pack cache.
     * Optionally, installs the feature-pack archive to the universe repository.
     *
     * @param featurePack  feature-pack archive
     * @param installInUniverse  whether to install the feature-pack into the universe repository
     * @throws ProvisioningException  in case of a failure
     */
    public synchronized void addLocal(Path featurePack, boolean installInUniverse) throws ProvisioningException {
        final FPID fpid = FeaturePackDescriber.readSpec(featurePack).getFPID();
        final Path fpDir = LayoutUtils.getFeaturePackDir(home, fpid, false);
        if(Files.exists(fpDir)) {
            IoUtils.recursiveDelete(fpDir);
        }
        unpack(fpDir, featurePack);
        if(!installInUniverse) {
            return;
        }
        if(universeInstallers == null) {
            universeInstallers = UniverseFeaturePackInstaller.load();
        }
        final Universe<?> universe = universeResolver.getUniverse(fpid.getUniverse());
        final UniverseFeaturePackInstaller fpInstaller = universeInstallers.get(universe.getFactoryId());
        if(fpInstaller == null) {
            throw new ProvisioningException(Errors.featurePackInstallerNotFound(universe.getFactoryId(), universeInstallers.keySet()));
        }
        fpInstaller.install(universe, fpid, featurePack);
    }

    public ProvisioningLayout<FeaturePackLayout> newConfigLayout(ProvisioningConfig config) throws ProvisioningException {
        return newConfigLayout(config, new FeaturePackLayoutFactory<FeaturePackLayout>() {
            @Override
            public FeaturePackLayout newFeaturePack(FeaturePackLocation fpl, FeaturePackSpec spec, Path dir) {
                return new FeaturePackLayout() {
                    @Override
                    public FPID getFPID() {
                        return fpl.getFPID();
                    }

                    @Override
                    public FeaturePackSpec getSpec() {
                        return spec;
                    }

                    @Override
                    public Path getDir() {
                        return dir;
                    }
                };
            }});
    }

    public <F extends FeaturePackLayout> ProvisioningLayout<F> newConfigLayout(ProvisioningConfig config, FeaturePackLayoutFactory<F> factory) throws ProvisioningException {
        return new ProvisioningLayout<>(this, config, factory);
    }

    synchronized Path resolveFeaturePackDir(FeaturePackLocation fpl) throws ProvisioningException {
        final Path fpDir = LayoutUtils.getFeaturePackDir(home, fpl.getFPID(), false);
        if(Files.exists(fpDir)) {
            return fpDir;
        }
        unpack(fpDir, universeResolver.resolve(fpl));
        return fpDir;
    }

    private void unpack(final Path fpDir, final Path artifactPath) throws ProvisioningException {
        try {
            Files.createDirectories(fpDir);
        } catch (IOException e) {
            throw new ProvisioningException(Errors.mkdirs(fpDir), e);
        }
        try {
            ZipUtils.unzip(artifactPath, fpDir);
        } catch (IOException e) {
            throw new ProvisioningException("Failed to unzip " + artifactPath + " to " + fpDir, e);
        }
    }

    ProvisioningLayout.Handle createHandle() {
        final ProvisioningLayout.Handle handle = new ProvisioningLayout.Handle(this);
        openHandles.incrementAndGet();
        return handle;
    }

    void handleClosed() {
        openHandles.decrementAndGet();
    }

    Path newConfigLayoutDir() {
        return IoUtils.createRandomDir(home);
    }

    @Override
    public void close() {
        IoUtils.recursiveDelete(home);
        if(openHandles.get() != 0) {
            throw new IllegalStateException("Remaining open handles: " + openHandles.get());
        }
    }
}
