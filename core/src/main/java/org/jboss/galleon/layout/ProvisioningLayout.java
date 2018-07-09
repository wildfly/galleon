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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.Constants;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.FeaturePackDepsConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.plugin.ProvisioningPlugin;
import org.jboss.galleon.spec.FeaturePackSpec;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.IoUtils;
import org.jboss.galleon.xml.FeaturePackXmlParser;

/**
 *
 * @author Alexey Loubyansky
 */
public class ProvisioningLayout<F extends ProvisioningLayout.FeaturePackLayout> implements AutoCloseable {

    public static final String STAGED = "staged";
    public static final String TMP = "tmp";

    public interface FeaturePackLayout {

        FPID getFPID();

        FeaturePackSpec getSpec();

        Path getDir();
    }

    public static class Handle implements Closeable {
        private final ProvisioningLayoutFactory layoutFactory;
        private Path workDir;
        private ClassLoader pluginsCl;
        private boolean closePluginsCl;
        private Path pluginsDir;
        private Path resourcesDir;
        private Path tmpDir;

        private int refs;

        Handle(ProvisioningLayoutFactory layoutFactory) {
            this.layoutFactory = layoutFactory;
            refs = 1;
        }

        protected void cleanup() {
            if(closePluginsCl) {
                if(closePluginsCl) {
                    try {
                        ((java.net.URLClassLoader)pluginsCl).close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if(pluginsDir != null) {
                IoUtils.recursiveDelete(pluginsDir);
                pluginsDir = null;
            }
            if(resourcesDir != null) {
                IoUtils.recursiveDelete(resourcesDir);
                resourcesDir = null;
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
                try(DirectoryStream<Path> stream = Files.newDirectoryStream(stagedDir)) {
                    for(Path p : stream) {
                        IoUtils.recursiveDelete(p);
                    }
                } catch (IOException e) {
                    throw new ProvisioningException(Errors.readDirectory(stagedDir), e);
                }
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
            if(closePluginsCl) {
                try {
                    ((java.net.URLClassLoader)pluginsCl).close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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

    private Map<ProducerSpec, FeaturePackLocation> resolvedVersions;
    private Set<ProducerSpec> transitiveDeps;
    private Map<ProducerSpec, Set<FPID>> conflicts = Collections.emptyMap();
    private Map<ProducerSpec, F> featurePacks = new HashMap<>();
    private List<F> ordered = new ArrayList<>();
    private Map<FPID, F> allPatches = Collections.emptyMap();
    private Map<FPID, List<F>> fpPatches = Collections.emptyMap();

    ProvisioningLayout(ProvisioningLayoutFactory layoutFactory, ProvisioningConfig config, FeaturePackLayoutFactory<F> fpFactory) throws ProvisioningException {
        this.layoutFactory = layoutFactory;
        this.fpFactory = fpFactory;
        this.config = config;
        this.handle = layoutFactory.createHandle();
        build(false);
    }

    <O extends FeaturePackLayout> ProvisioningLayout(ProvisioningLayout<O> other, FeaturePackLayoutFactory<F> fpFactory) throws ProvisioningException {
        this(other, fpFactory, new FeaturePackLayoutTransformer<F, O>() {
            @Override
            public F transform(O other) throws ProvisioningException {
                return fpFactory.newFeaturePack(other.getFPID().getLocation(), other.getSpec(), other.getDir());
            }
        });
    }

    <O extends FeaturePackLayout> ProvisioningLayout(ProvisioningLayout<O> other, FeaturePackLayoutTransformer<F, O> transformer) throws ProvisioningException {
        this(other, new FeaturePackLayoutFactory<F>() {
            final FeaturePackLayoutFactory<O> fpFactory = other.fpFactory;
            @Override
            public F newFeaturePack(FeaturePackLocation fpl, FeaturePackSpec spec, Path dir) throws ProvisioningException {
                return transformer.transform(fpFactory.newFeaturePack(fpl, spec, dir));
            }
        }, transformer);
    }

    private <O extends FeaturePackLayout> ProvisioningLayout(ProvisioningLayout<O> other, FeaturePackLayoutFactory<F> fpFactory, FeaturePackLayoutTransformer<F, O> transformer) throws ProvisioningException {
        this.layoutFactory = other.layoutFactory;
        this.fpFactory = fpFactory;
        this.config = other.config;
        for(O otherFp : other.ordered) {
            final F fp = transformer.transform(otherFp);
            featurePacks.put(fp.getFPID().getProducer(), fp);
            ordered.add(fp);
        }
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

    public <O extends FeaturePackLayout> ProvisioningLayout<O> transform(FeaturePackLayoutFactory<O> fpFactory) throws ProvisioningException {
        return new ProvisioningLayout<>(this, fpFactory);
    }

    public <O extends FeaturePackLayout> ProvisioningLayout<O> transform(FeaturePackLayoutTransformer<O, F> transformer) throws ProvisioningException {
        return new ProvisioningLayout<>(this, transformer);
    }

    public void install(FeaturePackLocation fpl) throws ProvisioningException {
        install(FeaturePackConfig.forLocation(fpl));
    }

    public void install(FeaturePackConfig fpConfig) throws ProvisioningException {
        final FPID fpid = fpConfig.getLocation().getFPID();
        if(allPatches.containsKey(fpid)) {
            throw new ProvisioningException(Errors.patchAlreadyApplied(fpid));
        }
        final ProvisioningConfig.Builder configBuilder = ProvisioningConfig.builder(config);
        final F installedFp = featurePacks.get(fpid.getProducer());
        if(installedFp != null && installedFp.getFPID().getBuild().equals(fpid.getBuild())) {
            throw new ProvisioningException(Errors.featurePackAlreadyConfigured(fpid.getProducer()));
        }
        final FeaturePackSpec fpSpec = createFeaturePack(fpid.getLocation()).getSpec();
        if(fpSpec.isPatch()) {
            F patchTarget = featurePacks.get(fpSpec.getPatchFor().getProducer());
            if(patchTarget == null || !patchTarget.getFPID().equals(fpSpec.getPatchFor())) {
                throw new ProvisioningException(Errors.patchNotApplicable(fpid, fpSpec.getPatchFor()));
            }
            FeaturePackConfig installedFpConfig = config.getFeaturePackDep(fpSpec.getPatchFor().getProducer());
            if(installedFpConfig == null) {
                installedFpConfig = config.getTransitiveDep(fpSpec.getPatchFor().getProducer());
            }
            if(installedFpConfig == null) {
                configBuilder.addFeaturePackDep(FeaturePackConfig.transitiveBuilder(patchTarget.getFPID().getLocation()).addPatch(fpid).build());
            } else {
                configBuilder.updateFeaturePackDep(FeaturePackConfig.builder(installedFpConfig.getLocation()).init(installedFpConfig).addPatch(fpid).build());
            }
        } else if(installedFp == null) {
            configBuilder.addFeaturePackDep(fpConfig);
        } else {
            FeaturePackConfig installedFpConfig = config.getFeaturePackDep(fpid.getProducer());
            if(installedFpConfig == null) {
                installedFpConfig = config.getTransitiveDep(fpid.getProducer());
                if(installedFpConfig != null) {
                    configBuilder.removeTransitiveDep(fpid.getLocation().getFPID());
                }
                configBuilder.addFeaturePackDep(fpConfig);
            } else {
                configBuilder.updateFeaturePackDep(fpConfig).build();
            }
        }
        rebuild(configBuilder.build(), false);
    }

    public void uninstall(FPID fpid) throws ProvisioningException {
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
            IoUtils.recursiveDelete(layoutFactory.resolveFeaturePackDir(patchTarget.getLocation())); // to clear patches
            rebuild(ProvisioningConfig.builder(config).updateFeaturePackDep(FeaturePackConfig.builder(targetConfig).removePatch(fpid).build()).build(), false);
            return;
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
        final ProvisioningConfig.Builder configBuilder = ProvisioningConfig.builder(config).removeFeaturePackDep(fpid.getLocation());
        if(!configBuilder.hasFeaturePackDeps()) {
            configBuilder.clearFeaturePackDeps();
        }
        rebuild(configBuilder.build(), true);
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
        final ClassLoader pluginsCl = getPluginsClassLoader();
        final Thread thread = Thread.currentThread();
        final ServiceLoader<T> pluginLoader = ServiceLoader.load(clazz, pluginsCl);
        final Iterator<T> pluginIterator = pluginLoader.iterator();
        if (pluginIterator.hasNext()) {
            final ClassLoader ocl = thread.getContextClassLoader();
            try {
                thread.setContextClassLoader(pluginsCl);
                final T plugin = pluginIterator.next();
                visitor.visitPlugin(plugin);
                while (pluginIterator.hasNext()) {
                    visitor.visitPlugin(pluginIterator.next());
                }
            } finally {
                thread.setContextClassLoader(ocl);
            }
        }
    }

    public Path newStagedDir() throws ProvisioningException {
        return handle.newStagedDir();
    }

    private void rebuild(ProvisioningConfig config, boolean cleanupTransitive) throws ProvisioningException {
        featurePacks.clear();
        ordered.clear();
        allPatches = Collections.emptyMap();
        fpPatches = Collections.emptyMap();
        this.config = config;
        handle.cleanup();
        build(cleanupTransitive);
    }

    private void build(boolean cleanupTransitive) throws ProvisioningException {
        try {
            doBuild(cleanupTransitive);
        } catch (ProvisioningException | RuntimeException | Error e) {
            handle.close();
            throw e;
        }
    }

    private void doBuild(boolean cleanupTransitive) throws ProvisioningException {

        final Map<ProducerSpec, FPID> depBranch = new HashMap<>();
        layout(config, depBranch);
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
            boolean rewriteResources = false;
            boolean rewritePlugins = false;
            for (F f : ordered) {
                final List<F> patches = fpPatches.get(f.getFPID());
                if(patches == null) {
                    if(rewriteResources) {
                        final Path fpResources = f.getDir().resolve(Constants.RESOURCES);
                        if(Files.exists(fpResources)) {
                            patchDir(getResources(), fpResources);
                        }
                    }
                    if(rewritePlugins) {
                        final Path fpPlugins = f.getDir().resolve(Constants.PLUGINS);
                        if(Files.exists(fpPlugins)) {
                            patchDir(getPluginsDir(), fpPlugins);
                        }
                    }
                    continue;
                }
                final Path fpDir = f.getDir();
                for(F patch : patches) {
                    final Path patchDir = patch.getDir();
                    Path patchContent = patchDir.resolve(Constants.PACKAGES);
                    if(Files.exists(patchContent)) {
                        patchDir(fpDir.resolve(Constants.PACKAGES), patchContent);
                    }
                    patchContent = patchDir.resolve(Constants.FEATURES);
                    if(Files.exists(patchContent)) {
                        patchDir(fpDir.resolve(Constants.FEATURES), patchContent);
                    }
                    patchContent = patchDir.resolve(Constants.FEATURE_GROUPS);
                    if(Files.exists(patchContent)) {
                        patchDir(fpDir.resolve(Constants.FEATURE_GROUPS), patchContent);
                    }
                    patchContent = patchDir.resolve(Constants.PLUGINS);
                    if(Files.exists(patchContent)) {
                        rewritePlugins = true;
                        patchDir(getPluginsDir(), patchContent);
                    }
                    patchContent = patchDir.resolve(Constants.RESOURCES);
                    if(Files.exists(patchContent)) {
                        rewriteResources = true;
                        patchDir(fpDir.resolve(Constants.RESOURCES), patchContent);
                        patchDir(getResources(), patchContent);
                    }
                }
            }
        }
    }

    private void patchDir(final Path fpDir, final Path patchDir) throws ProvisioningException {
        try {
            IoUtils.copy(patchDir, fpDir);
        } catch (IOException e) {
            throw new ProvisioningException(Errors.copyFile(patchDir, fpDir));
        }
    }

    private void layout(FeaturePackDepsConfig config, Map<ProducerSpec, FPID> branch) throws ProvisioningException {
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
                fpl = new FeaturePackLocation(fpl.getUniverse(), fpl.getProducerName(), fpl.getChannelName(), fpl.getFrequency(), branchId.getBuild());
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

            fp = createFeaturePack(fpl);
            featurePacks.put(fpl.getProducer(), fp);

            queue.add(fp);

            if(branchId == null) {
                branch.put(fpl.getProducer(), fpl.getFPID());
                added = CollectionUtils.add(added, fpl.getProducer());
            }
        }
        if(!queue.isEmpty()) {
            for(F p : queue) {
                layout(p.getSpec(), branch);
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
        final F patchFp = createFeaturePack(patchId.getLocation());
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

    private F createFeaturePack(FeaturePackLocation location)
            throws ProvisioningException {
        final Path fpDir = layoutFactory.resolveFeaturePackDir(location);
        final Path fpXml = fpDir.resolve(Constants.FEATURE_PACK_XML);
        if (!Files.exists(fpXml)) {
            throw new ProvisioningDescriptionException(Errors.pathDoesNotExist(fpXml));
        }
        try (BufferedReader reader = Files.newBufferedReader(fpXml)) {
            return fpFactory.newFeaturePack(location, FeaturePackXmlParser.getInstance().parse(reader), fpDir);
        } catch (IOException | XMLStreamException e) {
            throw new ProvisioningException(Errors.parseXml(fpXml), e);
        }
    }

    @Override
    public void close() {
        handle.close();
    }
}
