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
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
import org.jboss.galleon.runtime.ProvisioningRuntime.PluginVisitor;
import org.jboss.galleon.spec.FeaturePackSpec;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.UniverseSpec;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.IoUtils;
import org.jboss.galleon.xml.FeaturePackXmlParser;

/**
 *
 * @author Alexey Loubyansky
 */
public class ProvisioningLayout<F extends ProvisioningLayout.FeaturePackLayout> implements Closeable {

    public static final String STAGED = "staged";
    public static final String TMP = "tmp";

    public interface FeaturePackLayoutFactory<F extends FeaturePackLayout> {
        F repoFeaturePack(FeaturePackLocation fpl, FeaturePackSpec spec, Path dir);
    }

    public interface FeaturePackLayoutTransformer<F extends FeaturePackLayout, O extends FeaturePackLayout> {
        F transform(O other) throws ProvisioningException;
    }

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

    private ProvisioningConfig config;

    private final ProvisioningLayoutFactory layoutFactory;
    private Set<ProducerSpec> missingVersions = Collections.emptySet();
    private Map<ProducerSpec, Set<FPID>> conflicts = Collections.emptyMap();
    private Map<ProducerSpec, F> loaded = new HashMap<>();
    private List<F> ordered = new ArrayList<>();

    private final Handle handle;

    ProvisioningLayout(ProvisioningLayoutFactory layoutFactory, ProvisioningConfig config, FeaturePackLayoutFactory<F> fpFactory) throws ProvisioningException {
        this.layoutFactory = layoutFactory;
        this.config = config;
        this.handle = layoutFactory.createHandle();
        try {
            build(fpFactory);
        } catch (ProvisioningException | RuntimeException | Error e) {
            handle.close();
            throw e;
        }
    }

    <O extends FeaturePackLayout> ProvisioningLayout(ProvisioningLayout<O> other, FeaturePackLayoutFactory<F> fpFactory) throws ProvisioningException {
        this(other, new FeaturePackLayoutTransformer<F, O>() {
            @Override
            public F transform(O other) {
                return fpFactory.repoFeaturePack(other.getFPID().getLocation(), other.getSpec(), other.getDir());
            }
        });
    }

    <O extends FeaturePackLayout> ProvisioningLayout(ProvisioningLayout<O> other, FeaturePackLayoutTransformer<F, O> transformer) throws ProvisioningException {
        this.layoutFactory = other.layoutFactory;
        this.config = other.config;
        for(O otherFp : other.ordered) {
            final F fp = transformer.transform(otherFp);
            loaded.put(fp.getFPID().getProducer(), fp);
            ordered.add(fp);
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

    public ProvisioningConfig getConfig() {
        return config;
    }

    public boolean hasFeaturePacks() {
        return !loaded.isEmpty();
    }

    public boolean hasFeaturePack(ProducerSpec producer) {
        return loaded.containsKey(producer);
    }

    public F getFeaturePack(ProducerSpec producer) throws ProvisioningException {
        final F p = loaded.get(producer);
        if(p == null) {
            throw new ProvisioningException(Errors.unknownFeaturePack(producer.getLocation().getFPID()));
        }
        return p;
    }

    public List<F> getOrderedFeaturePacks() {
        return ordered;
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
     * @throws ProvisioningException
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

    public <T extends ProvisioningPlugin> void visitPlugins(PluginVisitor<T> visitor, Class<T> clazz) throws ProvisioningException {
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

    protected void build(FeaturePackLayoutFactory<F> factory) throws ProvisioningException {
        final List<ProducerSpec> depBranch = new ArrayList<>();

        while (true) {
            layout(config, depBranch, factory);
            if (!conflicts.isEmpty()) {
                throw new ProvisioningDescriptionException(Errors.fpVersionCheckFailed(conflicts.values()));
            }
            if (missingVersions.isEmpty()) {
                break;
            }

            final ProvisioningConfig.Builder builder = ProvisioningConfig.builder();
            if (config.hasDefaultUniverse()) {
                builder.setDefaultUniverse(config.getDefaultUniverse());
            }
            for (Map.Entry<String, UniverseSpec> universe : config.getUniverseNamedSpecs().entrySet()) {
                builder.addUniverse(universe.getKey(), universe.getValue());
            }
            for (FeaturePackConfig fpConfig : config.getFeaturePackDeps()) {
                final ProducerSpec producer = fpConfig.getLocation().getProducer();
                if (missingVersions.contains(producer)) {
                    fpConfig = FeaturePackConfig.builder(layoutFactory.getUniverseResolver().resolveLatestBuild(fpConfig.getLocation()))
                            .init(fpConfig).build();
                    missingVersions = CollectionUtils.remove(missingVersions, producer);
                }
                builder.addFeaturePackDep(config.originOf(producer), fpConfig);
            }
            for (ProducerSpec producer : missingVersions) {
                builder.addFeaturePackDep(
                        FeaturePackConfig.forLocation(layoutFactory.getUniverseResolver().resolveLatestBuild(producer.getLocation())));
            }
            config = builder.build();
            missingVersions = Collections.emptySet();
            ordered.clear();
        }
    }

    private void layout(FeaturePackDepsConfig fpDepsConfig, List<ProducerSpec> branch, FeaturePackLayoutFactory<F> factory) throws ProvisioningException {
        if(!fpDepsConfig.hasFeaturePackDeps()) {
            return;
        }
        final int branchSize = branch.size();
        final Collection<FeaturePackConfig> fpDeps = fpDepsConfig.getFeaturePackDeps();
        List<F> queue = new ArrayList<>(fpDeps.size());
        for(FeaturePackConfig fpConfig : fpDeps) {
            final FeaturePackLocation fpl = fpConfig.getLocation();
            if(fpl.getBuild() == null) {
                missingVersions = CollectionUtils.addLinked(missingVersions, fpl.getProducer());
                continue;
            }
            final F fp = loaded.get(fpl.getProducer());
            if(fp != null) {
                final FPID loadedFpid = fp.getFPID();
                if(!loadedFpid.equals(fpl.getFPID()) && !branch.contains(fpl.getProducer())) {
                    Set<FPID> versions = conflicts.get(loadedFpid.getProducer());
                    if(versions != null) {
                        versions.add(fpl.getFPID());
                        continue;
                    }
                    versions = new LinkedHashSet<>();
                    versions.add(loadedFpid);
                    versions.add(fpl.getFPID());
                    conflicts = CollectionUtils.putLinked(conflicts, fpl.getProducer(), versions);
                }
                continue;
            }

            final Path fpDir = layoutFactory.resolveFeaturePackDir(fpl);
            final Path fpXml = fpDir.resolve(Constants.FEATURE_PACK_XML);
            if (!Files.exists(fpXml)) {
                throw new ProvisioningDescriptionException(Errors.pathDoesNotExist(fpXml));
            }

            final FeaturePackSpec spec;
            try (BufferedReader reader = Files.newBufferedReader(fpXml)) {
                 spec = FeaturePackXmlParser.getInstance().parse(reader);
            } catch (IOException | XMLStreamException e) {
                throw new ProvisioningException(Errors.parseXml(fpXml), e);
            }
            F producer = factory.repoFeaturePack(fpl, spec, fpDir);
            loaded.put(fpl.getProducer(), producer);

            queue.add(producer);

            if(!missingVersions.isEmpty()) {
                missingVersions = CollectionUtils.remove(missingVersions, fpl.getProducer());
            }
            branch.add(fpl.getProducer());
        }
        if(!queue.isEmpty()) {
            for(F p : queue) {
                layout(p.getSpec(), branch, factory);
                handle.copyResources(p.getDir());
                ordered.add(p);
            }
        }
        for(int i = 0; i < branch.size() - branchSize; ++i) {
            branch.remove(branch.size() - 1);
        }
    }

    @Override
    public void close() {
        handle.close();
    }
}
