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

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.runtime.ProvisioningLayout.FeaturePackLayout;
import org.jboss.galleon.runtime.ProvisioningLayout.FeaturePackLayoutFactory;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.Universe;
import org.jboss.galleon.universe.UniverseResolver;
import org.jboss.galleon.util.IoUtils;
import org.jboss.galleon.util.LayoutUtils;
import org.jboss.galleon.util.ZipUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class ProvisioningLayoutFactory implements Closeable {

    public static ProvisioningLayoutFactory getInstance(UniverseResolver universeResolver) {
        return new ProvisioningLayoutFactory(IoUtils.createRandomTmpDir(), universeResolver);
    }

    public static ProvisioningLayoutFactory getInstance(Path home, UniverseResolver universeResolver) {
        return new ProvisioningLayoutFactory(home, universeResolver);
    }

    private final Path home;
    private final UniverseResolver universeResolver;
    private AtomicInteger openHandles = new AtomicInteger();

    private ProvisioningLayoutFactory(Path home, UniverseResolver universeResolver) {
        this.home = home;
        this.universeResolver = universeResolver;
    }

    public UniverseResolver getUniverseResolver() {
        return universeResolver;
    }

    public <F extends FeaturePackLayout> ProvisioningLayout<F> newConfigLayout(ProvisioningConfig config, FeaturePackLayoutFactory<F> factory) throws ProvisioningException {
        return new ProvisioningLayout<>(this, config, factory);
    }

    Path resolveFeaturePackDir(FeaturePackLocation fpl) throws ProvisioningException {
        final Path fpDir = LayoutUtils.getFeaturePackDir(home, fpl.getFPID(), false);
        if(Files.exists(fpDir)) {
            return fpDir;
        }
        final Universe<?> universe = universeResolver.getUniverse(fpl.getUniverse());
        final Path artifactPath = universe.getProducer(fpl.getProducerName()).getChannel(fpl.getChannelName()).resolve(fpl);
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
        return fpDir;
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
