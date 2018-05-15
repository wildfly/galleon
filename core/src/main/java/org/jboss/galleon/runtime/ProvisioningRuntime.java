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

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.ArtifactException;
import org.jboss.galleon.ArtifactRepositoryManager;
import org.jboss.galleon.Constants;
import org.jboss.galleon.Errors;
import org.jboss.galleon.MessageWriter;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.diff.FileSystemDiffResult;
import org.jboss.galleon.plugin.DiffPlugin;
import org.jboss.galleon.plugin.InstallPlugin;
import org.jboss.galleon.plugin.PluginOption;
import org.jboss.galleon.plugin.ProvisioningPlugin;
import org.jboss.galleon.plugin.UpgradePlugin;
import org.jboss.galleon.repomanager.FeaturePackBuilder;
import org.jboss.galleon.repomanager.FeaturePackRepositoryManager;
import org.jboss.galleon.state.FeaturePackSet;
import org.jboss.galleon.state.ProvisionedConfig;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.FeaturePackInstallException;
import org.jboss.galleon.util.IoUtils;
import org.jboss.galleon.util.PathsUtils;
import org.jboss.galleon.util.StringUtils;
import org.jboss.galleon.xml.ProvisionedStateXmlWriter;
import org.jboss.galleon.xml.ProvisioningXmlWriter;

/**
 *
 * @author Alexey Loubyansky
 */
public class ProvisioningRuntime implements FeaturePackSet<FeaturePackRuntime>, AutoCloseable {

    public interface PluginVisitor<T extends ProvisioningPlugin> {
        void visitPlugin(T plugin) throws ProvisioningException;
    }

    public static void install(ProvisioningRuntime runtime) throws ProvisioningException {
        // copy package content
        for(FeaturePackRuntime fp : runtime.fpRuntimes.values()) {
            final ArtifactCoords.Gav fpGav = fp.getGav();
            runtime.messageWriter.verbose("Installing %s", fpGav);
            for(PackageRuntime pkg : fp.getPackages()) {
                final Path pkgSrcDir = pkg.getContentDir();
                if (Files.exists(pkgSrcDir)) {
                    try {
                        IoUtils.copy(pkgSrcDir, runtime.stagedDir);
                    } catch (IOException e) {
                        throw new FeaturePackInstallException(Errors.packageContentCopyFailed(pkg.getName()), e);
                    }
                }
            }
        }

        // execute the plug-ins
        runtime.executePlugins();

        // save the config
        try {
            ProvisioningXmlWriter.getInstance().write(runtime.config, PathsUtils.getProvisioningXml(runtime.stagedDir));
        } catch (XMLStreamException | IOException e) {
            throw new FeaturePackInstallException(Errors.writeFile(PathsUtils.getProvisioningXml(runtime.stagedDir)), e);
        }

        // save the provisioned state
        try {
            ProvisionedStateXmlWriter.getInstance().write(runtime, PathsUtils.getProvisionedStateXml(runtime.stagedDir));
        } catch (XMLStreamException | IOException e) {
            throw new FeaturePackInstallException(Errors.writeFile(PathsUtils.getProvisionedStateXml(runtime.stagedDir)), e);
        }
        runtime.messageWriter.verbose("Moving the provisioned installation from the staged directory to %s", runtime.installDir);
        // copy from the staged to the target installation directory
        if (Files.exists(runtime.installDir)) {
            IoUtils.recursiveDelete(runtime.installDir);
        }
        try {
            IoUtils.copy(runtime.stagedDir, runtime.installDir);
        } catch (IOException e) {
            throw new ProvisioningException(Errors.copyFile(runtime.stagedDir, runtime.installDir));
        }
    }

    public static void exportToFeaturePack(ProvisioningRuntime runtime, ArtifactCoords.Gav exportGav, Path location, Path installationHome) throws ProvisioningDescriptionException, ProvisioningException, IOException {
        diff(runtime, location, installationHome);
        FeaturePackRepositoryManager fpRepoManager = FeaturePackRepositoryManager.newInstance(location);
        FeaturePackBuilder fpBuilder = fpRepoManager.installer().newFeaturePack(exportGav);
        Map<String, FeaturePackConfig.Builder> builders = new HashMap<>();
        for (FeaturePackConfig fpConfig : runtime.getProvisioningConfig().getFeaturePackDeps()) {
            FeaturePackConfig.Builder builder = FeaturePackConfig.builder(fpConfig.getGav());
            for(ConfigModel configSpec : fpConfig.getDefinedConfigs()) {
                builder.addConfig(configSpec);
            }
            builder.excludeAllPackages(fpConfig.getExcludedPackages());
            builder.setInheritConfigs(fpConfig.isInheritConfigs());
            builder.setInheritModelOnlyConfigs(fpConfig.isInheritModelOnlyConfigs());
            builder.setInheritPackages(fpConfig.isInheritPackages());
            for(Entry<String, Boolean> excludedModel : fpConfig.getFullModelsExcluded().entrySet()) {
                builder.excludeConfigModel(excludedModel.getKey(), excludedModel.getValue());
            }
            for(ConfigId includedConfig : fpConfig.getIncludedConfigs()) {
                builder.includeDefaultConfig(includedConfig);
            }
            builders.put(FeaturePackConfig.getDefaultOriginName(fpConfig.getGav()), builder);
        }
        runtime.exportDiffResultToFeaturePack(fpBuilder, builders, installationHome);
        for(Entry<String,FeaturePackConfig.Builder> entry : builders.entrySet()) {
            fpBuilder.addDependency(entry.getKey(), entry.getValue().build());
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(runtime.pluginsDir)) {
            for(Path file : stream) {
                if((Files.isRegularFile(file))) {
                    fpBuilder.addPlugin(file);
                }
            }
        } catch(IOException ioex) {
            throw new ProvisioningException(ioex);
        }
        fpBuilder.getInstaller().install();
        runtime.artifactResolver.install(exportGav.toArtifactCoords(), fpRepoManager.resolve(exportGav.toArtifactCoords()));
    }

    public static void diff(ProvisioningRuntime runtime, Path target, Path customizedInstallation) throws ProvisioningException, IOException {
        // execute the plug-ins
        runtime.executeDiffPlugins(target, customizedInstallation);
    }

    public static void upgrade(ProvisioningRuntime runtime, Path customizedInstallation) throws ProvisioningException {
        // execute the plug-ins
        runtime.executeUpgradePlugins(customizedInstallation);
         if (Files.exists(customizedInstallation)) {
            IoUtils.recursiveDelete(customizedInstallation);
        }
        try {
            IoUtils.copy(runtime.installDir, customizedInstallation);
        } catch (IOException e) {
            throw new ProvisioningException(Errors.copyFile(runtime.installDir, customizedInstallation));
        }
    }

    private final long startTime;
    private final ArtifactRepositoryManager artifactResolver;
    private ProvisioningConfig config;
    private Path installDir;
    private final Path stagedDir;
    private final Path workDir;
    private final Path tmpDir;
    private final Path pluginsDir;
    private final Map<ArtifactCoords.Ga, FeaturePackRuntime> fpRuntimes;
    private final Map<String, String> pluginOptions;
    private final MessageWriter messageWriter;
    private List<ProvisionedConfig> configs = Collections.emptyList();
    private FileSystemDiffResult diff = FileSystemDiffResult.empty();
    private final String operation;
    private ClassLoader pluginsClassLoader;
    private boolean closePluginsCl;

    ProvisioningRuntime(ProvisioningRuntimeBuilder builder, final MessageWriter messageWriter) throws ProvisioningException {
        this.startTime = builder.startTime;
        this.artifactResolver = builder.artifactResolver;
        this.config = builder.config;
        this.fpRuntimes = builder.getFpRuntimes(this);
        this.pluginsDir = builder.pluginsDir; // the pluginsDir is initialized during the getFpRuntimes() invocation, atm
        this.configs = builder.getResolvedConfigs();
        pluginOptions = CollectionUtils.unmodifiable(builder.pluginOptions);
        this.operation = builder.operation;

        this.workDir = builder.workDir;
        this.installDir = builder.installDir;
        this.stagedDir = workDir.resolve("staged");
        try {
            Files.createDirectories(stagedDir);
        } catch(IOException e) {
            throw new ProvisioningException(Errors.mkdirs(stagedDir), e);
        }

        this.tmpDir = workDir.resolve("tmp");
        this.messageWriter = messageWriter;
    }

    private ClassLoader getPluginClassloader() throws ProvisioningException {
        if(pluginsClassLoader != null) {
            return pluginsClassLoader;
        }
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
                final Thread thread = Thread.currentThread();
                pluginsClassLoader = new java.net.URLClassLoader(urls.toArray(
                        new java.net.URL[urls.size()]), thread.getContextClassLoader());
            } else {
                pluginsClassLoader = Thread.currentThread().getContextClassLoader();
            }
        } else {
            pluginsClassLoader = Thread.currentThread().getContextClassLoader();
        }
        return pluginsClassLoader;
    }

    /**
     * The target staged location
     *
     * @return the staged location
     */
    public Path getStagedDir() {
        return stagedDir;
    }

    /**
     * The target installation location
     *
     * @return the installation location
     */
    public Path getInstallDir() {
        return installDir;
    }

    public void setInstallDir(Path installDir) {
        this.installDir = installDir;
    }

    /**
     * Configuration of the installation to be provisioned.
     *
     * @return  installation configuration
     */
    public ProvisioningConfig getProvisioningConfig() {
        return config;
    }

    @Override
    public boolean hasFeaturePacks() {
        return !fpRuntimes.isEmpty();
    }

    @Override
    public boolean hasFeaturePack(ArtifactCoords.Ga ga) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<FeaturePackRuntime> getFeaturePacks() {
        return fpRuntimes.values();
    }

    @Override
    public FeaturePackRuntime getFeaturePack(ArtifactCoords.Ga ga) {
        return fpRuntimes.get(ga);
    }

    /**
     * Returns a writer for messages to be reported.
     *
     * @return the message writer
     */
    public MessageWriter getMessageWriter() {
        return messageWriter;
    }

    public void setDiff(FileSystemDiffResult diff) {
        this.diff = diff;
    }

    /**
     * Returns the result of a diff if such an operation was called previously.
     *
     * @return the result of a diff
     */
    public FileSystemDiffResult getDiff() {
        return diff;
    }

    public void exportDiffResultToFeaturePack(FeaturePackBuilder fpBuilder, Map<String, FeaturePackConfig.Builder> builders, Path installationHome) throws ProvisioningException {
        ClassLoader pluginClassLoader = getPluginClassloader();
        if (pluginClassLoader != null) {
            final Thread thread = Thread.currentThread();
            final ClassLoader ocl = thread.getContextClassLoader();
            try {
                thread.setContextClassLoader(pluginClassLoader);
                diff.toFeaturePack(fpBuilder, builders, this, installationHome);
            } finally {
                thread.setContextClassLoader(ocl);
            }
        }
    }

    /**
     * Returns the current operation being executed.
     *
     * @return the current operation being executed.
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Returns a resource path for the provisioning setup.
     *
     * @param path  path to the resource relative to the global resources directory
     * @return  file-system path for the resource
     */
    public Path getResource(String... path) {
        if(path.length == 0) {
            throw new IllegalArgumentException("Resource path is null");
        }
        if(path.length == 1) {
            return workDir.resolve(Constants.RESOURCES).resolve(path[0]);
        }
        Path p = workDir.resolve(Constants.RESOURCES);
        for(String name : path) {
            p = p.resolve(name);
        }
        return p;
    }

    /**
     * Returns a path for a temporary file-system resource.
     *
     * @param path  path relative to the global tmp directory
     * @return  temporary file-system path
     */
    public Path getTmpPath(String... path) {
        if(path.length == 0) {
            return tmpDir;
        }
        if(path.length == 1) {
            return tmpDir.resolve(path[0]);
        }
        Path p = tmpDir;
        for(String name : path) {
            p = p.resolve(name);
        }
        return p;
    }

    public boolean isOptionSet(PluginOption option) throws ProvisioningException {
        if(!pluginOptions.containsKey(option.getName())) {
            return false;
        }
        if(option.isAcceptsValue() || pluginOptions.get(option.getName()) == null) {
            return true;
        }
        throw new ProvisioningException("Plugin option " + option.getName() + " does expect value but is set to " + pluginOptions.get(option.getName()));
    }

    public String getOptionValue(PluginOption option) throws ProvisioningException {
        return getOptionValue(option, null);
    }

    public String getOptionValue(PluginOption option, String defaultValue) throws ProvisioningException {
        final String value = pluginOptions.get(option.getName());
        if(value == null) {
            if(defaultValue != null) {
                return defaultValue;
            }
            defaultValue = option.getDefaultValue();
            if(defaultValue != null) {
                return defaultValue;
            }
            if(option.isRequired()) {
                throw new ProvisioningException("Required plugin option " + option.getName() + " has not been provided");
            }
            return null;
        }
        if(!option.isAcceptsValue()) {
            throw new ProvisioningException("Plugin option " + option.getName() + " is set to " + value + " but does not accept values");
        }
        if(!option.getValueSet().isEmpty() && !option.getValueSet().contains(value)) {
            final StringBuilder buf = new StringBuilder();
            buf.append("Plugin option ").append(option.getName()).append(" is set to ").append(value).append(" but expects one of ");
            StringUtils.append(buf, option.getValueSet());
            throw new ProvisioningException(buf.toString());
        }
        return value;
    }

    public Map<String, String> getPluginOptions() {
        return pluginOptions;
    }

    /**
     * Resolves the location of the artifact given its coordinates.
     *
     * @param coords  artifact coordinates
     * @return  location of the artifact
     * @throws ArtifactException  in case the artifact could not be
     * resolved for any reason
     */
    public Path resolveArtifact(ArtifactCoords coords) throws ArtifactException {
        return artifactResolver.resolve(coords);
    }

    @Override
    public boolean hasConfigs() {
        return !configs.isEmpty();
    }

    @Override
    public List<ProvisionedConfig> getConfigs() {
        return configs;
    }

    private void executePlugins() throws ProvisioningException {
        PluginVisitor<InstallPlugin> visitor = new PluginVisitor<InstallPlugin>() {
            @Override
            public void visitPlugin(InstallPlugin plugin) throws ProvisioningException {
                plugin.postInstall(ProvisioningRuntime.this);
            }
        };
        visitePlugins(visitor, InstallPlugin.class);
    }

    public <T extends ProvisioningPlugin> void visitePlugins(PluginVisitor<T> visitor, Class<T> clazz) throws ProvisioningException {
        ClassLoader pluginClassLoader = getPluginClassloader();
        if (pluginClassLoader != null) {
            final Thread thread = Thread.currentThread();
            final ServiceLoader<T> pluginLoader = ServiceLoader.load(clazz, pluginClassLoader);
            final Iterator<T> pluginIterator = pluginLoader.iterator();
            if (pluginIterator.hasNext()) {
                final ClassLoader ocl = thread.getContextClassLoader();
                try {
                    thread.setContextClassLoader(pluginClassLoader);
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
    }

    @Override
    public void close() {
        if(closePluginsCl) {
            try {
                ((java.net.URLClassLoader)pluginsClassLoader).close();
            } catch (IOException e) {
                if(messageWriter.isVerboseEnabled()) {
                    messageWriter.verbose("Failed to close plugins classloader");
                    e.printStackTrace();
                }
            }
        }
        IoUtils.recursiveDelete(workDir);
        //if (messageWriter.isVerboseEnabled()) {
            final long time = System.currentTimeMillis() - startTime;
            final long seconds = time / 1000;
            messageWriter.print("Done in %d.%d seconds", seconds, (time - seconds * 1000));
        //}
    }

    private void executeDiffPlugins(Path target, Path customizedInstallation) throws ProvisioningException, IOException {
        PluginVisitor<DiffPlugin> visitor = new PluginVisitor<DiffPlugin>() {
            @Override
            public void visitPlugin(DiffPlugin plugin) throws ProvisioningException {
                plugin.computeDiff(ProvisioningRuntime.this, customizedInstallation, target);
            }
        };
        visitePlugins(visitor, DiffPlugin.class);
    }

    private void executeUpgradePlugins(Path customizedInstallation) throws ProvisioningException {
        PluginVisitor<UpgradePlugin> visitor = new PluginVisitor<UpgradePlugin>() {
            @Override
            public void visitPlugin(UpgradePlugin plugin) throws ProvisioningException {
                plugin.upgrade(ProvisioningRuntime.this, customizedInstallation);
            }
        };
    }
}
