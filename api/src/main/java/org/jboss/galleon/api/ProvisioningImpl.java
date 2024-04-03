/*
 * Copyright 2016-2024 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.api;

import java.io.InputStream;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jboss.galleon.BaseErrors;

import org.jboss.galleon.DefaultMessageWriter;
import org.jboss.galleon.MessageWriter;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.api.config.GalleonConfigurationWithLayers;
import org.jboss.galleon.api.config.GalleonConfigurationWithLayersBuilderItf;
import org.jboss.galleon.api.config.GalleonFeaturePackConfig;
import org.jboss.galleon.api.config.GalleonProvisioningConfig;
import org.jboss.galleon.core.builder.LocalFP;
import org.jboss.galleon.core.builder.ProvisioningContext;
import org.jboss.galleon.progresstracking.DefaultProgressTracker;
import org.jboss.galleon.progresstracking.ProgressCallback;
import org.jboss.galleon.progresstracking.ProgressTracker;
import org.jboss.galleon.universe.UniverseResolver;
import org.jboss.galleon.core.builder.ProvisioningContextBuilder;
import org.jboss.galleon.diff.FsDiff;
import org.jboss.galleon.impl.ProvisioningUtil;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.UniverseSpec;
import org.jboss.galleon.util.PathsUtils;

class ProvisioningImpl implements Provisioning {

    private final Path home;
    private final MessageWriter log;
    private boolean logTime;

    private final UniverseResolver universeResolver;
    private boolean recordState;
    private final Map<String, ProgressTracker<?>> progressTrackers = new HashMap<>();

    private final Map<FPID, LocalFP> locals;

    private final String coreVersion;
    private final URLClassLoader loader;

    private final List<ProvisioningContext> contexts = new ArrayList<>();

    ProvisioningImpl(ProvisioningBuilder builder) throws ProvisioningException {
        this.home = builder.getInstallationHome();
        this.log = builder.getMessageWriter() == null ? DefaultMessageWriter.getDefaultInstance() : builder.getMessageWriter();
        this.coreVersion = builder.getGalleonCoreVersion();
        universeResolver = builder.getUniverseResolver();
        this.logTime = builder.isLogTime();
        this.locals = builder.getLocals();
        this.recordState = builder.isRecordState();
        loader = GalleonBuilder.getCallerClassLoader(coreVersion, universeResolver);
    }

    /**
     * Location of the installation.
     *
     * @return location of the installation
     */
    @Override
    public Path getInstallationHome() {
        return home;
    }

    /**
     * Whether to log provisioning time
     *
     * @return Whether provisioning time should be logged at the end
     */
    @Override
    public boolean isLogTime() {
        return logTime;
    }

    /**
     * Whether provisioning state will be recorded after (re-)provisioning.
     *
     * @return true if the provisioning state is recorded after provisioning,
     * otherwise false
     */
    @Override
    public boolean isRecordState() {
        return recordState;
    }

    @Override
    public void setProgressCallback(String id, ProgressCallback<?> callback) {
        if (callback == null) {
            progressTrackers.remove(id);
        } else {
            progressTrackers.put(id, new DefaultProgressTracker<>(callback));
        }
    }

    @Override
    public void setProgressTracker(String id, ProgressTracker<?> tracker) {
        if (tracker == null) {
            progressTrackers.remove(id);
        } else {
            progressTrackers.put(id, tracker);
        }
    }

    private ProvisioningContext buildProvisioningContext() throws ProvisioningException {
        try {
            //System.out.println("REQUIRED CORE VERSION is " + coreVersion);
            Class<?> callerClass = ProvisioningUtil.getCallerClass(loader);

            try {
                ProvisioningContextBuilder provisioner = (ProvisioningContextBuilder) callerClass.getConstructor().newInstance();
                ProvisioningContext ctx = provisioner.buildProvisioningContext(loader,
                        home,
                        log,
                        logTime,
                        recordState,
                        universeResolver,
                        progressTrackers,
                        locals);
                contexts.add(ctx);
                return ctx;
            } catch (Exception ex) {
                if (ex instanceof ProvisioningException) {
                    throw (ProvisioningException) ex;
                }
                throw new ProvisioningException(ex);
            }

        } catch (Exception ex) {
            if (ex instanceof ProvisioningException) {
                throw (ProvisioningException) ex;
            }
            throw new ProvisioningException(ex);
        }
    }

    @Override
    public void close() {
        try {
            for (ProvisioningContext ctx : contexts) {
                ctx.close();
            }
        } finally {
            try {
                GalleonBuilder.releaseUsage(coreVersion);
            } catch (ProvisioningException ex) {
                System.err.println("Error releasing classloader " + ex.getLocalizedMessage());
            }
        }
    }

    // Required by CLI
    /**
     * Add named universe spec to the provisioning configuration
     *
     * @param name universe name
     * @param universeSpec universe spec
     * @throws ProvisioningException in case of an error
     */
    @Override
    public void addUniverse(String name, UniverseSpec universeSpec) throws ProvisioningException {
        final GalleonProvisioningConfig config = GalleonProvisioningConfig.builder(getProvisioningConfig()).addUniverse(name, universeSpec).build();
        try {
            storeProvisioningConfig(config, PathsUtils.getProvisioningXml(home));
        } catch (Exception e) {
            if (e instanceof ProvisioningException) {
                throw (ProvisioningException) e;
            }
            throw new ProvisioningException(BaseErrors.writeFile(PathsUtils.getProvisioningXml(home)), e);
        }
    }

    /**
     * Removes universe spec associated with the name from the provisioning
     * configuration
     *
     * @param name name of the universe spec or null for the default universe
     * spec
     * @throws ProvisioningException in case of an error
     */
    @Override
    public void removeUniverse(String name) throws ProvisioningException {
        GalleonProvisioningConfig config = getProvisioningConfig();
        if (config == null || !config.hasUniverse(name)) {
            return;
        }
        config = GalleonProvisioningConfig.builder(config).removeUniverse(name).build();
        try {
            storeProvisioningConfig(config, PathsUtils.getProvisioningXml(home));
        } catch (Exception e) {
            if (e instanceof ProvisioningException) {
                throw (ProvisioningException) e;
            }
            throw new ProvisioningException(BaseErrors.writeFile(PathsUtils.getProvisioningXml(home)), e);
        }
    }

    /**
     * Set the default universe spec for the installation
     *
     * @param universeSpec universe spec
     * @throws ProvisioningException in case of an error
     */
    @Override
    public void setDefaultUniverse(UniverseSpec universeSpec) throws ProvisioningException {
        addUniverse(null, universeSpec);
    }

    @Override
    public GalleonProvisioningConfig getProvisioningConfig() throws ProvisioningException {
        if (home == null || !Files.exists(home)) {
            throw new ProvisioningException(BaseErrors.homeDirNotUsable(home));
        }
        Path provisioning = PathsUtils.getProvisioningXml(home);
        ProvisioningContext ctx = buildProvisioningContext();
        return ctx.parseProvisioningFile(provisioning);
    }

    @Override
    public List<String> getInstalledPacks(Path dir) throws ProvisioningException {
        ProvisioningContext ctx = buildProvisioningContext();
        return ctx.getInstalledPacks(dir);
    }

    @Override
    public GalleonProvisioningConfig loadProvisioningConfig(InputStream is) throws ProvisioningException {
        try {
            ProvisioningContext ctx = buildProvisioningContext();
            return ctx.loadProvisioningConfig(is);
        } catch (Exception ex) {
            if (ex instanceof ProvisioningException) {
                throw (ProvisioningException) ex;
            }
            throw new ProvisioningException(ex);
        }
    }

    @Override
    public void storeProvisioningConfig(GalleonProvisioningConfig config, Path target) throws ProvisioningException {
        try {
            ProvisioningContext ctx = buildProvisioningContext();
            ctx.storeProvisioningConfig(config, target);
        } catch (Exception ex) {
            if (ex instanceof ProvisioningException) {
                throw (ProvisioningException) ex;
            }
            throw new ProvisioningException(ex);
        }
    }

    @Override
    public void provision(GalleonProvisioningConfig config, List<Path> customConfigs, Map<String, String> options) throws ProvisioningException {
        try {
            ProvisioningContext ctx = buildProvisioningContext();
            ctx.provision(config, customConfigs, options);
        } catch (Exception ex) {
            if (ex instanceof ProvisioningException) {
                throw (ProvisioningException) ex;
            }
            throw new ProvisioningException(ex);
        }
    }

    @Override
    public void provision(Path config, Map<String, String> options) throws ProvisioningException {
        try {
            ProvisioningContext ctx = buildProvisioningContext();
            ctx.provision(config, options);
        } catch (Exception ex) {
            if (ex instanceof ProvisioningException) {
                throw (ProvisioningException) ex;
            }
            throw new ProvisioningException(ex);
        }
    }

    @Override
    public GalleonProvisioningRuntime getProvisioningRuntime(GalleonProvisioningConfig config) throws ProvisioningException {
        try {
            ProvisioningContext ctx = buildProvisioningContext();
            return ctx.getProvisioningRuntime(config);
        } catch (Exception ex) {
            if (ex instanceof ProvisioningException) {
                throw (ProvisioningException) ex;
            }
            throw new ProvisioningException(ex);
        }
    }

    @Override
    public GalleonConfigurationWithLayersBuilderItf buildConfigurationBuilder(GalleonConfigurationWithLayers config) throws ProvisioningException {
        ProvisioningContext ctx = buildProvisioningContext();
        return ctx.buildConfigurationBuilder(config);
    }

    @Override
    public boolean hasOrderedFeaturePacksConfig(GalleonProvisioningConfig config, ConfigId cfg) throws ProvisioningException {
        ProvisioningContext ctx = buildProvisioningContext();
        return ctx.hasOrderedFeaturePacksConfig(config, cfg);
    }

    @Override
    public Set<String> getOrderedFeaturePackPluginLocations(GalleonProvisioningConfig config) throws ProvisioningException {
        ProvisioningContext ctx = buildProvisioningContext();
        return ctx.getOrderedFeaturePackPluginLocations(config);
    }

    @Override
    public FsDiff getFsDiff() throws ProvisioningException {
        ProvisioningContext ctx = buildProvisioningContext();
        return ctx.getFsDiff();
    }

    @Override
    public void install(FeaturePackLocation loc) throws ProvisioningException {
        ProvisioningContext ctx = buildProvisioningContext();
        ctx.install(loc);
    }

    @Override
    public void install(GalleonFeaturePackConfig config) throws ProvisioningException {
        ProvisioningContext ctx = buildProvisioningContext();
        ctx.install(config);
    }

    @Override
    public void uninstall(FeaturePackLocation.FPID loc) throws ProvisioningException {
        ProvisioningContext ctx = buildProvisioningContext();
        ctx.uninstall(loc);
    }

    @Override
    public GalleonProvisioningLayout newProvisioningLayout(GalleonProvisioningConfig config) throws ProvisioningException {
        ProvisioningContext ctx = buildProvisioningContext();
        return ctx.newProvisioningLayout(config);
    }

    @Override
    public GalleonProvisioningLayout newProvisioningLayout(Path file, boolean install) throws ProvisioningException {
        ProvisioningContext ctx = buildProvisioningContext();
        return ctx.newProvisioningLayout(file, install);
    }

    @Override
    public UniverseResolver getUniverseResolver() {
        return universeResolver;
    }
}
