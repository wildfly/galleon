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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.Errors;
import org.jboss.galleon.MessageWriter;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackBuilder;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.diff.ProvisioningDiffResult;
import org.jboss.galleon.layout.FeaturePackLayoutTransformer;
import org.jboss.galleon.layout.FeaturePackPluginVisitor;
import org.jboss.galleon.layout.ProvisioningLayout;
import org.jboss.galleon.layout.ProvisioningLayoutFactory;
import org.jboss.galleon.plugin.DiffPlugin;
import org.jboss.galleon.plugin.InstallPlugin;
import org.jboss.galleon.plugin.PluginOption;
import org.jboss.galleon.plugin.ProvisioningPlugin;
import org.jboss.galleon.plugin.UpgradePlugin;
import org.jboss.galleon.repo.RepositoryArtifactResolver;
import org.jboss.galleon.state.FeaturePackSet;
import org.jboss.galleon.state.ProvisionedConfig;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;
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

    public static void exportToFeaturePack(ProvisioningRuntime runtime, FPID fpid, Path location, Path installationHome) throws ProvisioningDescriptionException, ProvisioningException, IOException {
        // execute the plug-ins
        final ProvisioningDiffResult diff = runtime.diff(location, installationHome);

        final FeaturePackCreator creator = FeaturePackCreator.getInstance();
        FeaturePackBuilder fpBuilder = creator.newFeaturePack(fpid);
        Map<String, FeaturePackConfig.Builder> builders = new HashMap<>();
        for (FeaturePackConfig fpConfig : runtime.getProvisioningConfig().getFeaturePackDeps()) {
            FeaturePackConfig.Builder builder = FeaturePackConfig.builder(fpConfig.getLocation());
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
            builders.put(FeaturePackConfig.getDefaultOriginName(fpConfig.getLocation()), builder);
        }
        runtime.exportDiffResultToFeaturePack(diff, fpBuilder, builders, installationHome);
        for(Entry<String,FeaturePackConfig.Builder> entry : builders.entrySet()) {
            fpBuilder.addDependency(entry.getKey(), entry.getValue().build());
        }
        if(runtime.layout.hasPlugins()) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(runtime.layout.getPluginsDir())) {
                for (Path file : stream) {
                    if ((Files.isRegularFile(file))) {
                        fpBuilder.addPlugin(file);
                    }
                }
            } catch (IOException ioex) {
                throw new ProvisioningException(ioex);
            }
        }
        fpBuilder.getCreator().install();
    }

    private final long startTime;
    private ProvisioningConfig config;
    private final Path stagedDir;
    private final ProvisioningLayout<FeaturePackRuntime> layout;
    private final Map<String, String> pluginOptions;
    private final MessageWriter messageWriter;
    private List<ProvisionedConfig> configs = Collections.emptyList();

    ProvisioningRuntime(ProvisioningRuntimeBuilder builder, final MessageWriter messageWriter) throws ProvisioningException {
        this.startTime = builder.startTime;
        this.config = builder.config;
        this.layout = builder.layout.transform(new FeaturePackLayoutTransformer<FeaturePackRuntime, FeaturePackRuntimeBuilder>() {
            @Override
            public FeaturePackRuntime transform(FeaturePackRuntimeBuilder other) throws ProvisioningException {
                return other.build();
            }
        });

        try {
            this.configs = builder.getResolvedConfigs();
            this.stagedDir = layout.newStagedDir();
        } catch (ProvisioningException | RuntimeException | Error e) {
            layout.close();
            throw e;
        }

        pluginOptions = CollectionUtils.unmodifiable(builder.pluginOptions);

        this.messageWriter = messageWriter;
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
     * Configuration of the installation to be provisioned.
     *
     * @return  installation configuration
     */
    public ProvisioningConfig getProvisioningConfig() {
        return config;
    }

    public ProvisioningLayout<FeaturePackRuntime> getLayout() {
        return layout;
    }

    @Override
    public boolean hasFeaturePacks() {
        return layout.hasFeaturePacks();
    }

    @Override
    public boolean hasFeaturePack(ProducerSpec producer) {
        return layout.hasFeaturePack(producer);
    }

    @Override
    public Collection<FeaturePackRuntime> getFeaturePacks() {
        return layout.getOrderedFeaturePacks();
    }

    @Override
    public FeaturePackRuntime getFeaturePack(ProducerSpec producer) throws ProvisioningException {
        return layout.getFeaturePack(producer);
    }

    /**
     * Returns a writer for messages to be reported.
     *
     * @return the message writer
     */
    public MessageWriter getMessageWriter() {
        return messageWriter;
    }

    public void exportDiffResultToFeaturePack(ProvisioningDiffResult diff, FeaturePackBuilder fpBuilder, Map<String, FeaturePackConfig.Builder> builders, Path installationHome) throws ProvisioningException {
        ClassLoader pluginClassLoader = layout.getPluginsClassLoader();
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

    public ProvisioningLayoutFactory getLayoutFactory() {
        return layout.getFactory();
    }

    /**
     * Returns a resource path for the provisioning setup.
     *
     * @param path  path to the resource relative to the global resources directory
     * @return  file-system path for the resource
     * @throws ProvisioningException  in case of a failure
     */
    public Path getResource(String... path) throws ProvisioningException {
        return layout.getResource(path);
    }

    /**
     * Returns a path for a temporary file-system resource.
     *
     * @param path  path relative to the global tmp directory
     * @return  temporary file-system path
     */
    public Path getTmpPath(String... path) {
        return layout.getTmpPath(path);
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
     * Returns repository artifact resolver for specific repository type.
     *
     * @param repositoryId  repository id
     * @return  artifact resolver
     * @throws ProvisioningException  in case artifact resolver was not configured for the repository type
     */
    public RepositoryArtifactResolver getArtifactResolver(String repositoryId) throws ProvisioningException {
        return layout.getFactory().getUniverseResolver().getArtifactResolver(repositoryId);
    }

    @Override
    public boolean hasConfigs() {
        return !configs.isEmpty();
    }

    @Override
    public List<ProvisionedConfig> getConfigs() {
        return configs;
    }

    public void provision() throws ProvisioningException {
        // copy package content
        for(FeaturePackRuntime fp : layout.getOrderedFeaturePacks()) {
            messageWriter.verbose("Installing %s", fp.getFPID());
            for(PackageRuntime pkg : fp.getPackages()) {
                final Path pkgSrcDir = pkg.getContentDir();
                if (Files.exists(pkgSrcDir)) {
                    try {
                        IoUtils.copy(pkgSrcDir, stagedDir);
                    } catch (IOException e) {
                        throw new FeaturePackInstallException(Errors.packageContentCopyFailed(pkg.getName()), e);
                    }
                }
            }
        }

        // execute the plug-ins
        executeInstallPlugins();

        // save the config
        try {
            ProvisioningXmlWriter.getInstance().write(config, PathsUtils.getProvisioningXml(stagedDir));
        } catch (XMLStreamException | IOException e) {
            throw new FeaturePackInstallException(Errors.writeFile(PathsUtils.getProvisioningXml(stagedDir)), e);
        }

        // save the provisioned state
        try {
            ProvisionedStateXmlWriter.getInstance().write(this, PathsUtils.getProvisionedStateXml(stagedDir));
        } catch (XMLStreamException | IOException e) {
            throw new FeaturePackInstallException(Errors.writeFile(PathsUtils.getProvisionedStateXml(stagedDir)), e);
        }
    }

    private void executeInstallPlugins() throws ProvisioningException {
        FeaturePackPluginVisitor<InstallPlugin> visitor = new FeaturePackPluginVisitor<InstallPlugin>() {
            @Override
            public void visitPlugin(InstallPlugin plugin) throws ProvisioningException {
                plugin.postInstall(ProvisioningRuntime.this);
            }
        };
        visitCheckOptionsPlugins(visitor, InstallPlugin.class);
    }

    public ProvisioningDiffResult diff(Path target, Path customizedInstallation) throws ProvisioningException {
        final ProvisioningDiffResult result = ProvisioningDiffResult.empty();
        FeaturePackPluginVisitor<DiffPlugin> visitor = new FeaturePackPluginVisitor<DiffPlugin>() {
            @Override
            public void visitPlugin(DiffPlugin plugin) throws ProvisioningException {
                final ProvisioningDiffResult diff = plugin.computeDiff(ProvisioningRuntime.this, customizedInstallation, target);
                if(diff != null) {
                    // here we loose plugin specific diff
                    result.merge(diff);
                }
            }
        };
        visitCheckOptionsPlugins(visitor, DiffPlugin.class);
        try {
            result.toXML(target, customizedInstallation);
        } catch (Exception e) {
            throw new ProvisioningException("Failed to persist the diff result", e);
        }
        return result;
    }

    private <T extends ProvisioningPlugin> void visitCheckOptionsPlugins(FeaturePackPluginVisitor<T> visitor,
            Class<T> clazz) throws ProvisioningException {
        final Set<String> options = new HashSet<>();
        final FeaturePackPluginVisitor<T> v = new FeaturePackPluginVisitor<T>() {
            @Override
            public void visitPlugin(T plugin) throws ProvisioningException {
                //check for missing required options.
                for (PluginOption opt : plugin.getOptions().values()) {
                    if (opt.isRequired()) {
                        if (!pluginOptions.keySet().contains(opt.getName())) {
                            throw new ProvisioningException("Option: " + opt.getName()
                                    + " is required for this plugin.");
                        }
                    }
                    options.add(opt.getName());
                }
            }
        };
        layout.visitPlugins(v, clazz);
        // check if provided options exist
        for (String userOption : pluginOptions.keySet()) {
            if (!options.contains(userOption)) {
                throw new ProvisioningException("Option " + userOption + " is not supported");
            }
        }
        layout.visitPlugins(visitor, clazz);
    }

    public <T extends ProvisioningPlugin> void visitPlugins(FeaturePackPluginVisitor<T> visitor, Class<T> clazz) throws ProvisioningException {
        layout.visitPlugins(visitor, clazz);
    }

    @Override
    public void close() {
        layout.close();
        if (messageWriter.isVerboseEnabled()) {
            final long time = System.currentTimeMillis() - startTime;
            final long seconds = time / 1000;
            messageWriter.verbose("Done in %d.%d seconds", seconds, (time - seconds * 1000));
        }
    }

    public void executeUpgradePlugins(ProvisioningDiffResult diff, Path customizedInstallation) throws ProvisioningException {
        FeaturePackPluginVisitor<UpgradePlugin> visitor = new FeaturePackPluginVisitor<UpgradePlugin>() {
            @Override
            public void visitPlugin(UpgradePlugin plugin) throws ProvisioningException {
                plugin.upgrade(ProvisioningRuntime.this, diff, customizedInstallation);
            }
        };
        visitCheckOptionsPlugins(visitor, UpgradePlugin.class);
    }
}
