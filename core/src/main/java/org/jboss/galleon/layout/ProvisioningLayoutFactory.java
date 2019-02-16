/*
 * Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.Constants;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.progresstracking.DefaultProgressTracker;
import org.jboss.galleon.progresstracking.NoOpProgressCallback;
import org.jboss.galleon.progresstracking.ProgressCallback;
import org.jboss.galleon.progresstracking.ProgressTracker;
import org.jboss.galleon.spec.FeaturePackSpec;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.Universe;
import org.jboss.galleon.universe.UniverseFeaturePackInstaller;
import org.jboss.galleon.universe.UniverseResolver;
import org.jboss.galleon.util.ZipUtils;
import org.jboss.galleon.xml.FeaturePackXmlParser;

/**
 *
 * @author Alexey Loubyansky
 */
public class ProvisioningLayoutFactory implements Closeable {

    private static ProgressTracker<?> NO_OP_PROGRESS_TRACKER;

    public static final String TRACK_LAYOUT_BUILD = "LAYOUT_BUILD";
    public static final String TRACK_UPDATES = "UPDATES";
    public static final String TRACK_PACKAGES = "PACKAGES";
    public static final String TRACK_CONFIGS = "CONFIGS";

    public static ProvisioningLayoutFactory getInstance() throws ProvisioningException {
        return getInstance(UniverseResolver.builder().build());
    }

    public static ProvisioningLayoutFactory getInstance(UniverseResolver universeResolver) {
        return new ProvisioningLayoutFactory(universeResolver);
    }

    @SuppressWarnings("unchecked")
    public static <T> ProgressTracker<T> getNoOpProgressTracker() {
        return (ProgressTracker<T>) (NO_OP_PROGRESS_TRACKER == null ?
                NO_OP_PROGRESS_TRACKER = new DefaultProgressTracker<>(new NoOpProgressCallback<>()) :
                    NO_OP_PROGRESS_TRACKER);
    }

    private final UniverseResolver universeResolver;
    private AtomicInteger openHandles = new AtomicInteger();
    private Map<String, UniverseFeaturePackInstaller> universeInstallers;
    private Map<String, ProgressTracker<?>> progressTrackers = new HashMap<>();
    private final Map<FPID, FileSystem> cachedPacks = new HashMap<>();

    private ProvisioningLayoutFactory(UniverseResolver universeResolver) {
        this.universeResolver = universeResolver;
    }

    public void setProgressCallback(String id, ProgressCallback<?> callback) {
        if (callback == null) {
            progressTrackers.remove(id);
        } else {
            progressTrackers.put(id, new DefaultProgressTracker<>(callback));
        }
    }

    public void setProgressTracker(String id, ProgressTracker<?> tracker) {
        if (tracker == null) {
            progressTrackers.remove(id);
        } else {
            progressTrackers.put(id, tracker);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> ProgressTracker<T> getProgressTracker(String id) {
        final ProgressTracker<?> callback = progressTrackers.get(id);
        return callback == null ? getNoOpProgressTracker() : (ProgressTracker<T>) callback;
    }

    public boolean hasProgressCallback(String id) {
        return progressTrackers.containsKey(id);
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
     * @return  feature-pack location which was added to the local cache
     * @throws ProvisioningException  in case of a failure
     */
    public synchronized FeaturePackLocation addLocal(Path featurePack, boolean installInUniverse) throws ProvisioningException {
        final FPID fpid = FeaturePackDescriber.readSpec(featurePack).getFPID();
        put(featurePack, fpid);
        if(!installInUniverse) {
            return fpid.getLocation();
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
        return fpid.getLocation();
    }

    /**
     * Builds a layout for the configuration including the feature-pack contained in the local archive.
     * Optionally, installs the feature-pack archive to the universe repository.
     *
     * @param featurePack  feature-pack archive
     * @param installInUniverse  whether to install the feature-pack into the universe repository
     * @return layout
     * @throws ProvisioningException  in case of a failure
     */
    public ProvisioningLayout<FeaturePackLayout> newConfigLayout(Path featurePack, boolean installInUniverse) throws ProvisioningException {
        return newConfigLayout(ProvisioningConfig.builder().addFeaturePackDep(addLocal(featurePack, installInUniverse)).build());
    }

    public ProvisioningLayout<FeaturePackLayout> newConfigLayout(ProvisioningConfig config) throws ProvisioningException {
        return newConfigLayout(config, new FeaturePackLayoutFactory<FeaturePackLayout>() {
            @Override
            public FeaturePackLayout newFeaturePack(FeaturePackLocation fpl, FeaturePackSpec fpSpec, Path dir, int type) {
                return new FeaturePackLayout(fpl.getFPID(), dir, type) {
                    @Override
                    public FeaturePackSpec getSpec() {
                        return fpSpec;
                    }
                };
            }}, false);
    }

    public <F extends FeaturePackLayout> ProvisioningLayout<F> newConfigLayout(ProvisioningConfig config, FeaturePackLayoutFactory<F> factory, boolean initOptions) throws ProvisioningException {
        return new ProvisioningLayout<>(this, config, factory, initOptions);
    }

    public <F extends FeaturePackLayout> ProvisioningLayout<F> newConfigLayout(ProvisioningConfig config, FeaturePackLayoutFactory<F> factory, Map<String, String> pluginOptions) throws ProvisioningException {
        return new ProvisioningLayout<>(this, config, factory, pluginOptions);
    }

    public <F extends FeaturePackLayout> F resolveFeaturePack(FeaturePackLocation location, int type, FeaturePackLayoutFactory<F> factory)
            throws ProvisioningException {
        final Path fpDir = resolveFeaturePackDir(location);
        final Path fpXml = fpDir.resolve(Constants.FEATURE_PACK_XML);
        if (!Files.exists(fpXml)) {
            throw new ProvisioningDescriptionException(Errors.pathDoesNotExist(fpXml));
        }
        try (BufferedReader reader = Files.newBufferedReader(fpXml)) {
            return factory.newFeaturePack(location, FeaturePackXmlParser.getInstance().parse(reader), fpDir, type);
        } catch (IOException | XMLStreamException e) {
            throw new ProvisioningException(Errors.parseXml(fpXml), e);
        }
    }

    private synchronized Path resolveFeaturePackDir(FeaturePackLocation fpl) throws ProvisioningException {
        final FPID fpid = fpl.getFPID();
        FileSystem packFs = cachedPacks.get(fpid);
        if(packFs != null) {
            return getFpDir(packFs);
        }
        final Path resolvedPath = universeResolver.resolve(fpl);
        try {
            packFs = ZipUtils.newFileSystem(resolvedPath);
        } catch (IOException e) {
            throw new ProvisioningException(Errors.openFile(resolvedPath), e);
        }
        cachedPacks.put(fpid, packFs);
        return getFpDir(packFs);
    }

    private Path put(Path featurePack, FeaturePackLocation.FPID fpid) throws ProvisioningException {
        final FileSystem packFs;
        try {
            packFs = ZipUtils.newFileSystem(featurePack);
        } catch (IOException e) {
            throw new ProvisioningException(Errors.openFile(featurePack), e);
        }
        cachedPacks.put(fpid, packFs);
        return getFpDir(packFs);
    }

    private static Path getFpDir(FileSystem packFs) {
        return packFs.getRootDirectories().iterator().next();
    }

    ProvisioningLayout.Handle createHandle() {
        final ProvisioningLayout.Handle handle = new ProvisioningLayout.Handle(this);
        openHandles.incrementAndGet();
        return handle;
    }

    void handleClosed() {
        openHandles.decrementAndGet();
    }

    @Override
    public void close() {
        for(FileSystem fs : cachedPacks.values()) {
            try {
                fs.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        checkOpenLayouts();
    }

    public void checkOpenLayouts() {
        if (openHandles.get() != 0) {
            throw new IllegalStateException("Remaining open handles: " + openHandles.get());
        }
    }
}
