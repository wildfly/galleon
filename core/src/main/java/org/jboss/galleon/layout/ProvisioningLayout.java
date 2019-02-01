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

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Stream;

import org.jboss.galleon.Constants;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningOption;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.FeaturePackDepsConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.plugin.InstallPlugin;
import org.jboss.galleon.plugin.ProvisioningPlugin;
import org.jboss.galleon.progresstracking.ProgressTracker;
import org.jboss.galleon.spec.FeaturePackSpec;
import org.jboss.galleon.universe.Channel;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;
import org.jboss.galleon.universe.Universe;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.IoUtils;
import org.jboss.galleon.util.LayoutUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class ProvisioningLayout<F extends FeaturePackLayout> implements AutoCloseable {

    public static final String PATCHED = "patched";
    public static final String STAGED = "staged";
    public static final String TMP = "tmp";

    public static class Handle implements Closeable {
        private final ProvisioningLayoutFactory layoutFactory;
        private Path workDir;
        private ClassLoader pluginsCl;
        private boolean closePluginsCl;
        private Map<String, List<ProvisioningPlugin>> loadedPlugins = Collections.emptyMap();
        private Path patchedDir;
        private Path pluginsDir;
        private Path resourcesDir;
        private Path tmpDir;

        private int refs;

        Handle(ProvisioningLayoutFactory layoutFactory) {
            this.layoutFactory = layoutFactory;
            refs = 1;
        }

        protected void reset() {
            if(closePluginsCl) {
                try {
                    ((java.net.URLClassLoader)pluginsCl).close();
                } catch (IOException e) {
                    //e.printStackTrace();
                }
                closePluginsCl = false;
            }
            pluginsCl = null;
            loadedPlugins = Collections.emptyMap();
            if(workDir != null) {
                try(DirectoryStream<Path> stream = Files.newDirectoryStream(workDir)) {
                    for(Path p : stream) {
                        IoUtils.recursiveDelete(p);
                    }
                } catch (IOException e) {
                    IoUtils.recursiveDelete(workDir);
                    workDir = null;
                }
                patchedDir = null;
                resourcesDir = null;
                pluginsDir = null;
                tmpDir = null;
            }
        }

        protected void incrementRefs() {
            ++refs;
        }

        private void copyResources(Path fpDir) throws ProvisioningException {
            // resources should be copied last overriding the dependency resources
            final Path fpResources = fpDir.resolve(Constants.RESOURCES);
            if(Files.exists(fpResources)) {
                resourcesDir = getWorkDir().resolve(Constants.RESOURCES);
                try {
                    IoUtils.copy(fpResources, resourcesDir);
                } catch (IOException e) {
                    throw new ProvisioningException(Errors.copyFile(fpResources, resourcesDir), e);
                }
            }

            final Path fpPlugins = fpDir.resolve(Constants.PLUGINS);
            if(Files.exists(fpPlugins)) {
                if(pluginsDir == null) {
                    pluginsDir = getWorkDir().resolve(Constants.PLUGINS);
                }
                try {
                    IoUtils.copy(fpPlugins, pluginsDir);
                } catch (IOException e) {
                    throw new ProvisioningException(Errors.copyFile(fpPlugins, pluginsDir), e);
                }
            }
        }

        protected Path newStagedDir() throws ProvisioningException {
            final Path stagedDir = getWorkDir().resolve(STAGED);
            if(Files.exists(stagedDir)) {
                IoUtils.emptyDir(stagedDir);
            } else {
                try {
                    Files.createDirectories(stagedDir);
                } catch (IOException e) {
                    throw new ProvisioningException(Errors.mkdirs(stagedDir), e);
                }
            }
            return stagedDir;
        }

        protected Path getResource(String... path) throws ProvisioningException {
            if(resourcesDir == null) {
                throw new ProvisioningException("Configuration does not include resources");
            }
            if(path.length == 0) {
                throw new IllegalArgumentException("Resource path is null");
            }
            if(path.length == 1) {
                return resourcesDir.resolve(path[0]);
            }
            Path p = resourcesDir;
            for(String name : path) {
                p = p.resolve(name);
            }
            return p;
        }

        protected Path getTmpPath(String... path) {
            if(path.length == 0) {
                return getTmpDir();
            }
            if(path.length == 1) {
                return getTmpDir().resolve(path[0]);
            }
            Path p = getTmpDir();
            for(String name : path) {
                p = p.resolve(name);
            }
            return p;
        }

        protected ClassLoader getPluginsClassLoader() throws ProvisioningException {
            if(pluginsCl != null) {
                return pluginsCl;
            }
            pluginsCl = Thread.currentThread().getContextClassLoader();
            if (pluginsDir != null) {
                List<java.net.URL> urls = new ArrayList<>();
                try (Stream<Path> stream = Files.list(pluginsDir)) {
                    final Iterator<Path> i = stream.iterator();
                    while(i.hasNext()) {
                        urls.add(i.next().toUri().toURL());
                    }
                } catch (IOException e) {
                    throw new ProvisioningException(Errors.readDirectory(pluginsDir), e);
                }
                if (!urls.isEmpty()) {
                    closePluginsCl = true;
                    pluginsCl = new java.net.URLClassLoader(urls.toArray(
                            new java.net.URL[urls.size()]), pluginsCl);
                }
            }
            return pluginsCl;
        }

        @SuppressWarnings("unchecked")
        protected <T extends ProvisioningPlugin> void visitPlugins(FeaturePackPluginVisitor<T> visitor, Class<T> clazz) throws ProvisioningException {
            List<ProvisioningPlugin> plugins = loadedPlugins.get(clazz.getName());
            if (plugins == null) {
                final ClassLoader pluginsCl = getPluginsClassLoader();
                final Iterator<T> pluginIterator = ServiceLoader.load(clazz, pluginsCl).iterator();
                plugins = Collections.emptyList();
                if (pluginIterator.hasNext()) {
                    final Thread thread = Thread.currentThread();
                    final ClassLoader ocl = thread.getContextClassLoader();
                    try {
                        thread.setContextClassLoader(pluginsCl);
                        T plugin = pluginIterator.next();
                        plugins = CollectionUtils.add(plugins, plugin);
                        visitor.visitPlugin(plugin);
                        while (pluginIterator.hasNext()) {
                            plugin = pluginIterator.next();
                            plugins = CollectionUtils.add(plugins, plugin);
                            visitor.visitPlugin(plugin);
                        }
                    } finally {
                        thread.setContextClassLoader(ocl);
                    }
                }
                loadedPlugins = CollectionUtils.put(loadedPlugins, clazz.getName(), plugins);
                return;
            }

            if(plugins.isEmpty()) {
                return;
            }

            final Thread thread = Thread.currentThread();
            final ClassLoader ocl = thread.getContextClassLoader();
            thread.setContextClassLoader(getPluginsClassLoader());
            try {
                for (ProvisioningPlugin plugin : plugins) {
                    if (!clazz.isAssignableFrom(plugin.getClass())) {
                        continue;
                    }
                    visitor.visitPlugin((T) plugin);
                }
            } finally {
                thread.setContextClassLoader(ocl);
            }
        }

        private Path getPatchedDir() {
            return patchedDir == null ? patchedDir = getWorkDir().resolve(PATCHED) : patchedDir;
        }

        private Path getTmpDir() {
            return tmpDir == null ? tmpDir = getWorkDir().resolve(TMP) : tmpDir;
        }

        private Path getWorkDir() {
            return workDir == null ? workDir = layoutFactory.newConfigLayoutDir() : workDir;
        }

        public boolean isClosed() {
            return refs == 0;
        }

        @Override
        public void close() {
            if(refs == 0 || --refs > 0) {
                return;
            }
            reset();
            if(workDir != null) {
                IoUtils.recursiveDelete(workDir);
            }
            layoutFactory.handleClosed();
        }
    }

    private final ProvisioningLayoutFactory layoutFactory;
    private final FeaturePackLayoutFactory<F> fpFactory;
    private final Handle handle;
    private ProvisioningConfig config;
    private Map<String, String> options = Collections.emptyMap();

    private Map<ProducerSpec, FeaturePackLocation> resolvedVersions;
    private Set<ProducerSpec> transitiveDeps;
    private Map<ProducerSpec, Set<FPID>> conflicts = Collections.emptyMap();
    private Map<ProducerSpec, F> featurePacks = new HashMap<>();
    private ArrayList<F> ordered = new ArrayList<>();
    private Map<FPID, F> allPatches = Collections.emptyMap();
    private Map<FPID, List<F>> fpPatches = Collections.emptyMap();

    private ProgressTracker<ProducerSpec> updatesTracker;
    private ProgressTracker<FPID> buildTracker;

    ProvisioningLayout(ProvisioningLayoutFactory layoutFactory, ProvisioningConfig config, FeaturePackLayoutFactory<F> fpFactory, boolean initPluginOptions)
            throws ProvisioningException {
        this.layoutFactory = layoutFactory;
        this.fpFactory = fpFactory;
        this.config = config;
        this.handle = layoutFactory.createHandle();
        if(config.hasFeaturePackDeps()) {
            try {
                build(false, true);
                if (initPluginOptions) {
                    initPluginOptions(Collections.emptyMap(), false);
                }
            } catch(Throwable t) {
                handle.close();
                throw t;
            }
        }
    }

    ProvisioningLayout(ProvisioningLayoutFactory layoutFactory, ProvisioningConfig config, FeaturePackLayoutFactory<F> fpFactory, Map<String, String> extraOptions)
            throws ProvisioningException {
        this.layoutFactory = layoutFactory;
        this.fpFactory = fpFactory;
        this.config = config;
        this.handle = layoutFactory.createHandle();
        if(config.hasFeaturePackDeps()) {
            try {
                build(false, true);
                initPluginOptions(extraOptions, false);
            } catch(Throwable t) {
                handle.close();
                throw t;
            }
        }
    }

    <O extends FeaturePackLayout> ProvisioningLayout(ProvisioningLayout<O> other, FeaturePackLayoutFactory<F> fpFactory) throws ProvisioningException {
        this(other, fpFactory, new FeaturePackLayoutTransformer<F, O>() {
            @Override
            public F transform(O other) throws ProvisioningException {
                return fpFactory.newFeaturePack(other.getFPID().getLocation(), other.getSpec(), other.getDir(), other.getType());
            }
        });
    }

    <O extends FeaturePackLayout> ProvisioningLayout(ProvisioningLayout<O> other, FeaturePackLayoutTransformer<F, O> transformer) throws ProvisioningException {
        this(other, new FeaturePackLayoutFactory<F>() {
            final FeaturePackLayoutFactory<O> fpFactory = other.fpFactory;
            @Override
            public F newFeaturePack(FeaturePackLocation fpl, FeaturePackSpec spec, Path dir, int type) throws ProvisioningException {
                return transformer.transform(fpFactory.newFeaturePack(fpl, spec, dir, type));
            }
        }, transformer);
    }

    private <O extends FeaturePackLayout> ProvisioningLayout(ProvisioningLayout<O> other, FeaturePackLayoutFactory<F> fpFactory, FeaturePackLayoutTransformer<F, O> transformer) throws ProvisioningException {
        this.layoutFactory = other.layoutFactory;
        this.fpFactory = fpFactory;
        this.config = other.config;
        this.options = CollectionUtils.clone(other.options);

        // feature-packs are processed in the reverse order and then re-ordered again
        // this is necessary to properly analyze and include optional package and their external dependencies
        int i = other.ordered.size();
        ordered.ensureCapacity(i);
        while(--i >= 0) {
            final O otherFp = other.ordered.get(i);
            final F fp = transformer.transform(otherFp);
            featurePacks.put(fp.getFPID().getProducer(), fp);
            ordered.add(fp);
        }
        Collections.reverse(ordered);

        if(!other.fpPatches.isEmpty()) {
            fpPatches = new HashMap<>(other.fpPatches.size());
            for (Map.Entry<FPID, List<O>> patchEntry : other.fpPatches.entrySet()) {
                final List<O> patches = patchEntry.getValue();
                final List<F> convertedPatches = new ArrayList<>(patches.size());
                for(O o : patches) {
                    convertedPatches.add(transformer.transform(o));
                }
                fpPatches.put(patchEntry.getKey(), convertedPatches);
            }
        }
        this.handle = other.handle;
        handle.incrementRefs();
    }

    public ProvisioningLayoutFactory getFactory() {
        return layoutFactory;
    }

    public FeaturePackLayoutFactory<F> getFeaturePackFactory() {
        return fpFactory;
    }

    public <O extends FeaturePackLayout> ProvisioningLayout<O> transform(FeaturePackLayoutFactory<O> fpFactory) throws ProvisioningException {
        return new ProvisioningLayout<>(this, fpFactory);
    }

    public <O extends FeaturePackLayout> ProvisioningLayout<O> transform(FeaturePackLayoutTransformer<O, F> transformer) throws ProvisioningException {
        return new ProvisioningLayout<>(this, transformer);
    }

    public void apply(ProvisioningPlan plan) throws ProvisioningException {
        apply(plan, Collections.emptyMap());
    }

    public void apply(ProvisioningPlan plan, Map<String, String> pluginOptions) throws ProvisioningException {
        if(plan.isEmpty()) {
            return;
        }

        final ProvisioningConfig.Builder configBuilder = ProvisioningConfig.builder(config);

        if(plan.hasUpdates()) {
            Map<ProducerSpec, FeaturePackUpdatePlan> updates = plan.getUpdateMap();
            Set<ProducerSpec> processed = new HashSet<>(updates.size());
            for(FeaturePackConfig fpConfig : config.getFeaturePackDeps()) {
                final ProducerSpec producer = fpConfig.getLocation().getProducer();
                final FeaturePackUpdatePlan fpPlan = updates.get(producer);
                if(fpPlan != null && !fpPlan.isEmpty()) {
                    if(!fpPlan.getInstalledLocation().equals(fpConfig.getLocation())) {
                        throw new ProvisioningException("Location in the update plan " + fpPlan.getInstalledLocation() + " does not match the installed location " + fpConfig.getLocation());
                    }
                    final FeaturePackConfig.Builder fpBuilder = FeaturePackConfig.builder(fpPlan.getNewLocation()).init(fpConfig);
                    if(fpPlan.hasNewPatches()) {
                        for(FPID patchId : fpPlan.getNewPatches()) {
                            fpBuilder.addPatch(patchId);
                        }
                    }
                    configBuilder.updateFeaturePackDep(fpBuilder.build());
                    processed.add(producer);
                }
            }

            for(FeaturePackConfig fpConfig : config.getTransitiveDeps()) {
                final ProducerSpec producer = fpConfig.getLocation().getProducer();
                final FeaturePackUpdatePlan fpPlan = updates.get(producer);
                if(fpPlan != null && !fpPlan.isEmpty()) {
                    if(fpConfig.getLocation().getBuild() != null && !fpPlan.getInstalledLocation().equals(fpConfig.getLocation())) {
                        throw new ProvisioningException("Update plan build " + fpPlan.getInstalledLocation() + " does not match the installed build " + fpConfig.getLocation());
                    }
                    final FeaturePackConfig.Builder fpBuilder = FeaturePackConfig.transitiveBuilder(fpPlan.getNewLocation()).init(fpConfig);
                    if(fpPlan.hasNewPatches()) {
                        for(FPID patchId : fpPlan.getNewPatches()) {
                            fpBuilder.addPatch(patchId);
                        }
                    }
                    configBuilder.updateFeaturePackDep(fpBuilder.build());
                    processed.add(producer);
                }
            }

            if(processed.size() < updates.size()) {
                for (Map.Entry<ProducerSpec, FeaturePackUpdatePlan> entry : updates.entrySet()) {
                    if (processed.contains(entry.getKey())) {
                        continue;
                    }
                    final FeaturePackUpdatePlan update = entry.getValue();
                    final FeaturePackConfig.Builder fpBuilder = FeaturePackConfig.transitiveBuilder(update.getNewLocation());
                    if (update.hasNewPatches()) {
                        for (FPID patchId : update.getNewPatches()) {
                            fpBuilder.addPatch(patchId);
                        }
                    }
                    configBuilder.addFeaturePackDep(fpBuilder.build());
                }
            }
        }

        if(plan.hasInstall()) {
            for(FeaturePackConfig fpConfig : plan.getInstall()) {
                install(fpConfig, configBuilder);
            }
        }

        if(plan.hasUninstall()) {
            for(ProducerSpec producer : plan.getUninstall()) {
                uninstall(producer.getLocation().getFPID(), configBuilder);
            }
        }

        rebuild(configBuilder.build(), true);
        initPluginOptions(pluginOptions, plan.hasUninstall());
    }

    /**
     * Adds a feature-pack to the configuration and rebuilds the layout
     *
     * @param fpl  the feature-pack to add to the configuration
     * @throws ProvisioningException  in case of a failure
     */
    public void install(FeaturePackLocation fpl) throws ProvisioningException {
        install(FeaturePackConfig.forLocation(fpl));
    }

    /**
     * Adds a feature-pack to the configuration and rebuilds the layout
     *
     * @param fpConfig  the feature-pack to add to the configuration
     * @throws ProvisioningException  in case of a failure
     */
    public void install(FeaturePackConfig fpConfig) throws ProvisioningException {
        install(fpConfig, Collections.emptyMap());
    }

    public void install(FeaturePackConfig fpConfig, Map<String, String> pluginOptions) throws ProvisioningException {
        rebuild(install(fpConfig, ProvisioningConfig.builder(config)).build(), false);
        initPluginOptions(pluginOptions, false);
    }

    private ProvisioningConfig.Builder install(FeaturePackConfig fpConfig, ProvisioningConfig.Builder configBuilder) throws ProvisioningException {
        FeaturePackLocation fpl = fpConfig.getLocation();
        final FPID fpid = fpl.getFPID();
        if(allPatches.containsKey(fpid)) {
            throw new ProvisioningException(Errors.patchAlreadyApplied(fpid));
        }
        if(!fpl.hasBuild()) {
            fpl = layoutFactory.getUniverseResolver().resolveLatestBuild(fpl);
        }
        final FeaturePackSpec fpSpec = layoutFactory.resolveFeaturePack(fpl, FeaturePackLayout.DIRECT_DEP, fpFactory).getSpec();
        if(fpSpec.isPatch()) {
            F patchTarget = featurePacks.get(fpSpec.getPatchFor().getProducer());
            if(patchTarget == null || !patchTarget.getFPID().equals(fpSpec.getPatchFor())) {
                throw new ProvisioningException(Errors.patchNotApplicable(fpid, fpSpec.getPatchFor()));
            }
            FeaturePackConfig installedFpConfig = config.getFeaturePackDep(fpSpec.getPatchFor().getProducer());
            if(installedFpConfig == null) {
                installedFpConfig = config.getTransitiveDep(fpSpec.getPatchFor().getProducer());
            }
            if (installedFpConfig == null) {
                return configBuilder.addFeaturePackDep(
                        FeaturePackConfig.transitiveBuilder(patchTarget.getFPID().getLocation()).addPatch(fpid).build());
            }
            return configBuilder.updateFeaturePackDep(
                    FeaturePackConfig.builder(installedFpConfig.getLocation()).init(installedFpConfig).addPatch(fpid).build());
        }

        final F installedFp = featurePacks.get(fpid.getProducer());
        if(installedFp != null) {
            if(installedFp.isTransitiveDep() == fpConfig.isTransitive()) {
                return configBuilder.updateFeaturePackDep(fpConfig);
            }
            if(installedFp.isTransitiveDep()) {
                // transitive becomes direct
                if (config.hasTransitiveDep(fpid.getProducer())) {
                    configBuilder.removeTransitiveDep(fpid);
                }
                return configBuilder.addFeaturePackDep(getIndexForDepToInstall(configBuilder, fpid.getProducer()), fpConfig);
            }
            // direct becomes transitive
            configBuilder.removeFeaturePackDep(fpid.getLocation());
        }
        return configBuilder.addFeaturePackDep(fpConfig);
    }

    private int getIndexForDepToInstall(ProvisioningConfig.Builder configBuilder, ProducerSpec producer) throws ProvisioningException {
        int index = Integer.MAX_VALUE;
        final Set<ProducerSpec> visitedFps = new HashSet<>(featurePacks.size());
        visitedFps.add(producer);
        for(F f : featurePacks.values()) {
            if(!f.isTransitiveDep() && dependsOn(f, producer, visitedFps)) {
                index = Math.min(index, configBuilder.getFeaturePackDepIndex(f.getFPID().getLocation()));
            }
        }
        return index;
    }

    private boolean dependsOn(F f, ProducerSpec dep, Set<ProducerSpec> visitedFps) throws ProvisioningException {
        final FeaturePackSpec spec = f.getSpec();
        if(!spec.hasFeaturePackDeps()) {
            return false;
        }
        if(spec.hasFeaturePackDep(dep) || spec.hasTransitiveDep(dep)) {
            return true;
        }
        for(FeaturePackConfig fpConfig : spec.getFeaturePackDeps()) {
            final ProducerSpec producer = fpConfig.getLocation().getProducer();
            if(!visitedFps.add(producer)) {
                continue;
            }
            if(dependsOn(featurePacks.get(producer), dep, visitedFps)) {
                return true;
            }
            visitedFps.remove(producer);
        }
        return false;
    }

    /**
     * Removes a feature-pack from the configuration and re-builds the layout
     *
     * @param fpid  the feature-pack to remove from the configuration
     * @throws ProvisioningException  in case of a failure
     */
    public void uninstall(FPID fpid) throws ProvisioningException {
        uninstall(fpid, Collections.emptyMap());
    }

    public void uninstall(FPID fpid, Map<String, String> pluginOptions) throws ProvisioningException {
        rebuild(uninstall(fpid, ProvisioningConfig.builder(config)).build(), true);
        initPluginOptions(pluginOptions, true);
    }

    private ProvisioningConfig.Builder uninstall(FPID fpid, ProvisioningConfig.Builder configBuilder) throws ProvisioningException {
        if(allPatches.containsKey(fpid)) {
            final F patchFp = allPatches.get(fpid);
            final ProducerSpec patchTarget = patchFp.getSpec().getPatchFor().getProducer();
            FeaturePackConfig targetConfig = config.getFeaturePackDep(patchTarget);
            if(targetConfig == null) {
                targetConfig = config.getTransitiveDep(patchTarget);
                if(targetConfig == null) {
                    throw new ProvisioningException("Target feature-pack for patch " + fpid + " could not be found");
                }
            }
            return configBuilder.updateFeaturePackDep(FeaturePackConfig.builder(targetConfig).removePatch(fpid).build());
        }
        final F installedFp = featurePacks.get(fpid.getProducer());
        if(installedFp == null) {
            throw new ProvisioningException(Errors.unknownFeaturePack(fpid));
        }
        if(fpid.getBuild() != null && !installedFp.getFPID().getBuild().equals(fpid.getBuild())) {
            throw new ProvisioningException(Errors.unknownFeaturePack(fpid));
        }
        final FeaturePackConfig fpConfig = config.getFeaturePackDep(fpid.getProducer());
        if(fpConfig == null) {
            throw new ProvisioningException(Errors.unsatisfiedFeaturePackDep(fpid.getProducer()));
        }
        configBuilder.removeFeaturePackDep(fpid.getLocation());
        if (!configBuilder.hasFeaturePackDeps()) {
            configBuilder.clearFeaturePackDeps();
            configBuilder.clearOptions();
        }
        return configBuilder;
    }

    /**
     * Query for available updates and patches for feature-packs in this layout.
     *
     * @param includeTransitive  whether to include transitive dependencies into the result
     * @return  available updates
     * @throws ProvisioningException in case of a failure
     */
    public ProvisioningPlan getUpdates(boolean includeTransitive) throws ProvisioningException {
        return getUpdatesInternal(includeTransitive ? featurePacks.keySet() : config.getProducers());
    }

    /**
     * Query for available updates and patches for specific producers.
     * If no producer is passed as an argument, the method will return
     * the update plan for only the feature-packs installed directly by the user.
     *
     * @param producers  producers to include into the update plan
     * @return  update plan
     * @throws ProvisioningException in case of a failure
     */
    public ProvisioningPlan getUpdates(ProducerSpec... producers) throws ProvisioningException {
        if(producers.length == 0) {
            return getUpdates(false);
        }
        return getUpdatesInternal(Arrays.asList(producers));
    }

    private ProvisioningPlan getUpdatesInternal(Collection<ProducerSpec> producers) throws ProvisioningException {
        final ProvisioningPlan plan = ProvisioningPlan.builder();
        updatesTracker = getUpdatesTracker();
        updatesTracker.starting(producers.size());
        for(ProducerSpec producer : producers) {
            updatesTracker.processing(producer);
            final FeaturePackUpdatePlan fpPlan = getFeaturePackUpdate(producer);
            if(!fpPlan.isEmpty()) {
                plan.update(fpPlan);
            }
            updatesTracker.processed(producer);
        }
        updatesTracker.complete();
        return plan;
    }

    /**
     * Query for available version update and patches for the specific producer.
     *
     * @param producer  the producer to check the updates for
     * @return  available updates for the producer
     * @throws ProvisioningException  in case of a failure
     */
    public FeaturePackUpdatePlan getFeaturePackUpdate(ProducerSpec producer) throws ProvisioningException {
        final F f = featurePacks.get(producer);
        if(f == null) {
            throw new ProvisioningException(Errors.unknownFeaturePack(producer.getLocation().getFPID()));
        }
        final FeaturePackLocation fpl = f.getFPID().getLocation();
        final Universe<?> universe = layoutFactory.getUniverseResolver().getUniverse(fpl.getUniverse());
        final Channel channel = universe.getProducer(fpl.getProducerName()).getChannel(fpl.getChannelName());
        final List<F> patches = fpPatches.get(fpl.getFPID());
        final Set<FPID> patchIds;
        if (patches == null || patches.isEmpty()) {
            patchIds = Collections.emptySet();
        } else if (patches.size() == 1) {
            patchIds = Collections.singleton(patches.get(0).getFPID());
        } else {
            final Set<FPID> tmp = new HashSet<>(patches.size());
            for (F p : patches) {
                tmp.add(p.getFPID());
            }
            patchIds = CollectionUtils.unmodifiable(tmp);
        }
        return channel.getUpdatePlan(FeaturePackUpdatePlan.request(fpl, patchIds, f.isTransitiveDep()));
    }

    public ProvisioningConfig getConfig() {
        return config;
    }

    public boolean hasFeaturePacks() {
        return !featurePacks.isEmpty();
    }

    public boolean hasFeaturePack(ProducerSpec producer) {
        return featurePacks.containsKey(producer);
    }

    public F getFeaturePack(ProducerSpec producer) throws ProvisioningException {
        final F p = featurePacks.get(producer);
        if(p == null) {
            throw new ProvisioningException(Errors.unknownFeaturePack(producer.getLocation().getFPID()));
        }
        return p;
    }

    public List<F> getOrderedFeaturePacks() {
        return ordered;
    }

    public List<F> getPatches(FPID fpid) {
        final List<F> patches = fpPatches.get(fpid);
        return patches == null ? Collections.emptyList() : patches;
    }

    public boolean hasPlugins() {
        return handle.pluginsDir != null;
    }

    public Path getPluginsDir() {
        return handle.pluginsDir;
    }

    public boolean hasResources() {
        return handle.resourcesDir != null;
    }

    public Path getResources() {
        return handle.resourcesDir;
    }

    /**
     * Returns a resource path for the provisioning setup.
     *
     * @param path  path to the resource relative to the global resources directory
     * @return  file-system path for the resource
     * @throws ProvisioningException  in case the layout does not include any resources
     */
    public Path getResource(String... path) throws ProvisioningException {
        return handle.getResource(path);
    }

    /**
     * Returns a path for a temporary file-system resource.
     *
     * @param path  path relative to the global tmp directory
     * @return  temporary file-system path
     */
    public Path getTmpPath(String... path) {
        return handle.getTmpPath(path);
    }

    public ClassLoader getPluginsClassLoader() throws ProvisioningException {
        return handle.getPluginsClassLoader();
    }

    public <T extends ProvisioningPlugin> void visitPlugins(FeaturePackPluginVisitor<T> visitor, Class<T> clazz) throws ProvisioningException {
        handle.visitPlugins(visitor, clazz);
    }

    public Path newStagedDir() throws ProvisioningException {
        return handle.newStagedDir();
    }

    public boolean hasPatches() {
        return !allPatches.isEmpty();
    }

    private void rebuild(ProvisioningConfig config, boolean cleanupTransitive) throws ProvisioningException {
        final boolean trackProgress = featurePacks.isEmpty();
        featurePacks.clear();
        ordered.clear();
        allPatches = Collections.emptyMap();
        fpPatches = Collections.emptyMap();
        this.config = config;
        handle.reset();
        build(cleanupTransitive, trackProgress);
    }

    private void build(boolean cleanupTransitive, boolean trackProgress) throws ProvisioningException {
        try {
            doBuild(cleanupTransitive, trackProgress);
        } catch (ProvisioningException | RuntimeException | Error e) {
            handle.close();
            throw e;
        }
    }

    private void doBuild(boolean cleanupTransitive, boolean trackProgress) throws ProvisioningException {

        buildTracker = getBuildTracker(trackProgress);
        buildTracker.starting(-1);
        final Map<ProducerSpec, FPID> depBranch = new HashMap<>();
        layout(config, depBranch, FeaturePackLayout.DIRECT_DEP);
        if (!conflicts.isEmpty()) {
            throw new ProvisioningDescriptionException(Errors.fpVersionCheckFailed(conflicts.values()));
        }

        if (transitiveDeps != null) {
            ProvisioningConfig.Builder newConfig = null;
            List<ProducerSpec> notUsedTransitive = Collections.emptyList();
            for(ProducerSpec producer : transitiveDeps) {
                if(featurePacks.containsKey(producer)) {
                    continue;
                }
                if(cleanupTransitive && config.hasTransitiveDep(producer)) {
                    if(newConfig == null) {
                        newConfig = ProvisioningConfig.builder(config);
                    }
                    newConfig.removeTransitiveDep(producer.getLocation().getFPID());
                    continue;
                }
                notUsedTransitive = CollectionUtils.add(notUsedTransitive, producer);
            }
            if(!notUsedTransitive.isEmpty()) {
                throw new ProvisioningDescriptionException(
                        Errors.transitiveDependencyNotFound(notUsedTransitive.toArray(new ProducerSpec[notUsedTransitive.size()])));
            }
            if(newConfig != null) {
                config = newConfig.build();
            }
            transitiveDeps = null;
        }

        if(resolvedVersions != null) {
            final ProvisioningConfig.Builder builder = ProvisioningConfig.builder(config);
            for (FeaturePackConfig fpConfig : config.getFeaturePackDeps()) {
                final ProducerSpec producer = fpConfig.getLocation().getProducer();
                final FeaturePackLocation resolvedFpl = resolvedVersions.remove(producer);
                if(resolvedFpl != null) {
                    builder.updateFeaturePackDep(config.originOf(producer), FeaturePackConfig.builder(resolvedFpl).init(fpConfig).build());
                }
            }
            if(!resolvedVersions.isEmpty()) {
                for(FeaturePackLocation fpl : resolvedVersions.values()) {
                    builder.addTransitiveDep(fpl);
                }
            }
            config = builder.build();
            resolvedVersions = null;
        }

        // apply patches
        if(!fpPatches.isEmpty()) {
            for (F f : ordered) {
                final List<F> patches = fpPatches.get(f.getFPID());
                if(patches == null) {
                    final Path fpResources = f.getDir().resolve(Constants.RESOURCES);
                    if (Files.exists(fpResources)) {
                        patchDir(getResources(), fpResources);
                    }
                    final Path fpPlugins = f.getDir().resolve(Constants.PLUGINS);
                    if (Files.exists(fpPlugins)) {
                        patchDir(getPluginsDir(), fpPlugins);
                    }
                    continue;
                }

                final Path fpDir = LayoutUtils.getFeaturePackDir(handle.getPatchedDir(), f.getFPID(), false);
                try {
                    Files.createDirectories(fpDir);
                    IoUtils.copy(f.getDir(), fpDir);
                } catch (IOException e) {
                    throw new ProvisioningException("Failed to patch feature-pack dir for " + f.getFPID(), e);
                }
                f.dir = fpDir;

                for(F patch : patches) {
                    final Path patchDir = patch.getDir();
                    patchFpDir(fpDir, patchDir, Constants.PACKAGES);
                    patchFpDir(fpDir, patchDir, Constants.FEATURES);
                    patchFpDir(fpDir, patchDir, Constants.FEATURE_GROUPS);
                    patchFpDir(fpDir, patchDir, Constants.CONFIGS);
                    patchFpDir(fpDir, patchDir, Constants.LAYERS);
                    Path patchContent = patchDir.resolve(Constants.PLUGINS);
                    if(Files.exists(patchContent)) {
                        patchDir(fpDir.resolve(Constants.PLUGINS), patchContent);
                        patchDir(getPluginsDir(), patchContent);
                    }
                    patchContent = patchDir.resolve(Constants.RESOURCES);
                    if(Files.exists(patchContent)) {
                        patchDir(fpDir.resolve(Constants.RESOURCES), patchContent);
                        patchDir(getResources(), patchContent);
                    }
                }
            }
        }

        buildTracker.complete();
    }

    private void patchFpDir(final Path fpDir, final Path patchDir, String dirName) throws ProvisioningException {
        Path patchContent = patchDir.resolve(dirName);
        if(Files.exists(patchContent)) {
            patchDir(fpDir.resolve(dirName), patchContent);
        }
    }

    private void patchDir(final Path fpDir, final Path patchDir) throws ProvisioningException {
        try {
            IoUtils.copy(patchDir, fpDir);
        } catch (IOException e) {
            throw new ProvisioningException(Errors.copyFile(patchDir, fpDir), e);
        }
    }

    private void layout(FeaturePackDepsConfig config, Map<ProducerSpec, FPID> branch, int type) throws ProvisioningException {
        if(!config.hasFeaturePackDeps()) {
            return;
        }

        List<ProducerSpec> added = Collections.emptyList();
        if(config.hasTransitiveDeps()) {
            if(transitiveDeps == null) {
                transitiveDeps = new HashSet<>();
            }
            for(FeaturePackConfig transitiveConfig : config.getTransitiveDeps()) {
                final FeaturePackLocation fpl = transitiveConfig.getLocation();
                transitiveDeps.add(fpl.getProducer());
                if(transitiveConfig.hasPatches()) {
                    addPatches(transitiveConfig);
                }
                if(branch.containsKey(fpl.getProducer())) {
                    continue;
                }
                if(fpl.getBuild() == null) {
                    continue;
                }
                branch.put(fpl.getProducer(), fpl.getFPID());
                added = CollectionUtils.add(added, fpl.getProducer());
            }
        }

        final Collection<FeaturePackConfig> fpDeps = config.getFeaturePackDeps();
        List<F> queue = new ArrayList<>(fpDeps.size());
        for(FeaturePackConfig fpConfig : fpDeps) {
            FeaturePackLocation fpl = fpConfig.getLocation();
            if(fpConfig.hasPatches()) {
                addPatches(fpConfig);
            }

            final FPID branchId = branch.get(fpl.getProducer());
            if(branchId == null) {
                if(fpl.getBuild() == null) {
                    fpl = layoutFactory.getUniverseResolver().resolveLatestBuild(fpl);
                    if(resolvedVersions == null) {
                        resolvedVersions = new LinkedHashMap<>();
                    }
                    resolvedVersions.put(fpl.getProducer(), fpl);
                }
            } else if(!branchId.getBuild().equals(fpl.getBuild())) {
                fpl = fpl.replaceBuild(branchId.getBuild());
            }

            F fp = featurePacks.get(fpl.getProducer());
            if(fp != null) {
                if(branchId == null && !fpl.getBuild().equals(fp.getFPID().getBuild())) {
                    Set<FPID> versions = conflicts.get(fp.getFPID().getProducer());
                    if(versions != null) {
                        versions.add(fpl.getFPID());
                        continue;
                    }
                    versions = new LinkedHashSet<>();
                    versions.add(fp.getFPID());
                    versions.add(fpl.getFPID());
                    conflicts = CollectionUtils.putLinked(conflicts, fpl.getProducer(), versions);
                }
                continue;
            }

            buildTracker.processing(fpl.getFPID());
            fp = layoutFactory.resolveFeaturePack(fpl, type, fpFactory);
            buildTracker.processed(fpl.getFPID());
            featurePacks.put(fpl.getProducer(), fp);

            queue.add(fp);

            if(branchId == null) {
                branch.put(fpl.getProducer(), fpl.getFPID());
                added = CollectionUtils.add(added, fpl.getProducer());
            }
        }
        if(!queue.isEmpty()) {
            for(F p : queue) {
                layout(p.getSpec(), branch, FeaturePackLayout.TRANSITIVE_DEP);
                handle.copyResources(p.getDir());
                ordered.add(p);
            }
        }
        if (!added.isEmpty()) {
            for (ProducerSpec producer : added) {
                branch.remove(producer);
            }
        }
    }

    private void addPatches(FeaturePackConfig fpConfig) throws ProvisioningException {
        for(FPID patchId : fpConfig.getPatches()) {
            if(allPatches.containsKey(patchId)) {
                continue;
            }
            loadPatch(patchId);
        }
    }

    private void loadPatch(FPID patchId) throws ProvisioningException {
        final F patchFp = layoutFactory.resolveFeaturePack(patchId.getLocation(), FeaturePackLayout.PATCH, fpFactory);
        final FeaturePackSpec spec = patchFp.getSpec();
        if(!spec.isPatch()) {
            throw new ProvisioningDescriptionException(patchId + " is not a patch but listed as one");
        }
        allPatches = CollectionUtils.put(allPatches, patchId, patchFp);
        if(spec.hasFeaturePackDeps()) {
            for(FeaturePackConfig patchDep : spec.getFeaturePackDeps()) {
                final FPID patchDepId = patchDep.getLocation().getFPID();
                if(allPatches.containsKey(patchDepId)) {
                    continue;
                }
                loadPatch(patchDepId);
            }
        }
        final FPID patchFor = spec.getPatchFor();
        List<F> patchList = fpPatches.get(patchFor);
        if(patchList == null) {
            fpPatches = CollectionUtils.put(fpPatches, patchFor, Collections.singletonList(patchFp));
        } else if(patchList.size() == 1) {
            final List<F> tmp = new ArrayList<>(2);
            tmp.add(patchList.get(0));
            tmp.add(patchFp);
            fpPatches = CollectionUtils.put(fpPatches, patchFor, tmp);
        } else {
            patchList.add(patchFp);
        }
    }

    private ProgressTracker<ProducerSpec> getUpdatesTracker() {
        return updatesTracker == null
                ? updatesTracker = layoutFactory.getProgressTracker(ProvisioningLayoutFactory.TRACK_UPDATES)
                : updatesTracker;
    }

    private ProgressTracker<FPID> getBuildTracker(boolean trackProgress) {
        if(!trackProgress) {
            return ProvisioningLayoutFactory.getNoOpProgressTracker();
        }
        return buildTracker == null
                ? buildTracker = layoutFactory.getProgressTracker(ProvisioningLayoutFactory.TRACK_LAYOUT_BUILD)
                : buildTracker;
    }

    public boolean isOptionSet(String name) {
        return options.containsKey(name);
    }

    public String getOptionValue(String name) {
        return options.get(name);
    }

    public String getOptionValue(String name, String defValue) {
        final String setValue = options.get(name);
        return setValue == null ? defValue : setValue;
    }

    public String getOptionValue(ProvisioningOption option) throws ProvisioningException {
        final String setValue = options.get(option.getName());
        if(setValue == null) {
            if(option.isRequired() && (!options.containsKey(option.getName()) && option.getDefaultValue() == null)) {
                throw new ProvisioningException(Errors.pluginOptionRequired(option.getName()));
            }
            return option.getDefaultValue();
        }
        if(!option.getValueSet().isEmpty() && !option.getValueSet().contains(setValue)) {
            throw new ProvisioningException(Errors.pluginOptionIllegalValue(option.getName(), setValue, option.getValueSet()));
        }
        return setValue;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    private void initPluginOptions(Map<String, String> extraOptions, boolean cleanupConfigOptions) throws ProvisioningException {
        options = config.getOptions();
        if(!extraOptions.isEmpty()) {
            options = CollectionUtils.putAll(CollectionUtils.clone(options), extraOptions);
        }

        final Map<String, ProvisioningOption> recognizedOptions;
        final List<ProvisioningOption> overridenOptions;
        if(options.isEmpty()) {
            recognizedOptions = Collections.emptyMap();
            overridenOptions = Collections.emptyList();
        } else {
            final int size = options.size();
            recognizedOptions = new HashMap<>(size);
            overridenOptions = new ArrayList<>(size);
        }

        // process built-in options
        processOptions(ProvisioningOption.getStandardList(), extraOptions, recognizedOptions, overridenOptions);

        // process plugin options
        handle.visitPlugins(new FeaturePackPluginVisitor<InstallPlugin>() {
            @Override
            public void visitPlugin(InstallPlugin plugin) throws ProvisioningException {
                processOptions(plugin.getOptions().values(), extraOptions, recognizedOptions, overridenOptions);
            }}, InstallPlugin.class);

        ProvisioningConfig.Builder configBuilder = null;
        if(recognizedOptions.size() != options.size()) {
            final Set<String> nonRecognized = new HashSet<>(options.keySet());
            nonRecognized.removeAll(recognizedOptions.keySet());
            if(cleanupConfigOptions) {
                final Iterator<String> i = nonRecognized.iterator();
                while(i.hasNext()) {
                    final String optionName = i.next();
                    if(!config.hasOption(optionName)) {
                        continue;
                    }
                    if(configBuilder == null) {
                        configBuilder = ProvisioningConfig.builder(config);
                    }
                    configBuilder.removeOption(optionName);
                    i.remove();
                }
            }
            if(!nonRecognized.isEmpty()) {
                throw new ProvisioningException(Errors.pluginOptionsNotRecognized(nonRecognized));
            }
        }

        if(!overridenOptions.isEmpty()) {
            if(configBuilder == null) {
                configBuilder = ProvisioningConfig.builder(config);
            }
            for(ProvisioningOption option : overridenOptions) {
                final String optionName = option.getName();
                if(!extraOptions.containsKey(optionName)) {
                    continue;
                }
                final String value = extraOptions.get(optionName);
                if(option.isPersistent()) {
                    configBuilder.addOption(optionName, value);
                } else if (value == null) {
                    if (config.getOption(optionName) != null) {
                        configBuilder.removeOption(optionName);
                    }
                } else if (!value.equals(config.getOption(optionName))) {
                    configBuilder.removeOption(optionName);
                }
            }
            config = configBuilder.build();
        } else if(configBuilder != null) {
            config = configBuilder.build();
        }

        options = CollectionUtils.unmodifiable(options);
    }

    private void processOptions(Iterable<? extends ProvisioningOption> pluginOptions, Map<String, String> extraOptions,
            final Map<String, ProvisioningOption> recognizedOptions, final List<ProvisioningOption> overridenOptions)
            throws ProvisioningException {
        for(ProvisioningOption pluginOption : pluginOptions) {
            final String optionName = pluginOption.getName();
            if(!options.containsKey(optionName)) {
                if(pluginOption.isRequired()) {
                    throw new ProvisioningException(Errors.pluginOptionRequired(optionName));
                }
                continue;
            }
            final ProvisioningOption existing = recognizedOptions.put(optionName, pluginOption);
            // options should probably not be shared between plugins but just in case make sure non-persistent option
            // doesn't override a persistent one
            if (existing != null && existing.isPersistent() && !pluginOption.isPersistent()) {
                recognizedOptions.put(existing.getName(), existing);
            } else if (pluginOption.isPersistent() || extraOptions.containsKey(optionName) && config.hasOption(optionName)) {
                overridenOptions.add(pluginOption);
            }
        }
    }

    @Override
    public void close() {
        handle.close();
    }
}
