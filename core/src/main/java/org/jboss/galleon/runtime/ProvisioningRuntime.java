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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.Errors;
import org.jboss.galleon.MessageWriter;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningOption;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.diff.FsDiff;
import org.jboss.galleon.layout.FeaturePackLayoutTransformer;
import org.jboss.galleon.layout.FeaturePackPluginVisitor;
import org.jboss.galleon.layout.ProvisioningLayout;
import org.jboss.galleon.layout.ProvisioningLayoutFactory;
import org.jboss.galleon.plugin.InstallPlugin;
import org.jboss.galleon.plugin.PluginOption;
import org.jboss.galleon.repo.RepositoryArtifactResolver;
import org.jboss.galleon.state.FeaturePackSet;
import org.jboss.galleon.state.ProvisionedConfig;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;
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

    private final long startTime;
    private ProvisioningConfig config;
    private FsDiff fsDiff;
    private final Path stagedDir;
    private final ProvisioningLayout<FeaturePackRuntime> layout;
    private final MessageWriter messageWriter;
    private List<ProvisionedConfig> configs = Collections.emptyList();

    ProvisioningRuntime(final ProvisioningRuntimeBuilder builder, final MessageWriter messageWriter) throws ProvisioningException {
        this.startTime = builder.startTime;
        this.config = builder.config;
        this.layout = builder.layout.transform(new FeaturePackLayoutTransformer<FeaturePackRuntime, FeaturePackRuntimeBuilder>() {
            @Override
            public FeaturePackRuntime transform(FeaturePackRuntimeBuilder other) throws ProvisioningException {
                return other.build(builder);
            }
        });
        this.fsDiff = builder.fsDiff;

        try {
            this.configs = builder.getResolvedConfigs();
            this.stagedDir = layout.newStagedDir();
        } catch (ProvisioningException | RuntimeException | Error e) {
            layout.close();
            throw e;
        }

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

    public FsDiff getFsDiff() {
        return fsDiff;
    }

    /**
     * Deprecated in 3.0.0.Final
     * @return  provisioning layout factory
     */
    @Deprecated
    public ProvisioningLayoutFactory getLayoutFactory() {
        return layout.getFactory();
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

    /**
     * Deprecated in 3.0.0 in favor of the equivalent with ProvisioningOption
     * @param option  plugin option
     * @return  whether the value is set
     * @throws ProvisioningException  in case of an error
     */
    @Deprecated
    public boolean isOptionSet(PluginOption option) throws ProvisioningException {
        return isOptionSet((ProvisioningOption)option);
    }

    @Deprecated
    public String getOptionValue(PluginOption option) throws ProvisioningException {
        return getOptionValue((ProvisioningOption)option);
    }

    @Deprecated
    public String getOptionValue(PluginOption option, String defaultValue) throws ProvisioningException {
        return getOptionValue((ProvisioningOption)option, defaultValue);
    }

    public boolean isOptionSet(ProvisioningOption option) throws ProvisioningException {
        return layout.isOptionSet(option.getName());
    }

    public String getOptionValue(ProvisioningOption option) throws ProvisioningException {
        return getOptionValue(option, null);
    }

    public String getOptionValue(ProvisioningOption option, String defaultValue) throws ProvisioningException {
        final String value = layout.getOptionValue(option.getName());
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
        if(!option.getValueSet().isEmpty() && !option.getValueSet().contains(value)) {
            final StringBuilder buf = new StringBuilder();
            buf.append("Plugin option ").append(option.getName()).append(" is set to ").append(value).append(" but expects one of ");
            StringUtils.append(buf, option.getValueSet());
            throw new ProvisioningException(buf.toString());
        }
        return value;
    }

    /**
     * Deprecated in 3.0.0
     * @return  configured provisioning options
     */
    @Deprecated
    public Map<String, String> getPluginOptions() {
        return layout.getOptions();
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

        layout.visitPlugins(new FeaturePackPluginVisitor<InstallPlugin>() {
            @Override
            public void visitPlugin(InstallPlugin plugin) throws ProvisioningException {
                plugin.preInstall(ProvisioningRuntime.this);
            }
        }, InstallPlugin.class);

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

        layout.visitPlugins(new FeaturePackPluginVisitor<InstallPlugin>() {
            @Override
            public void visitPlugin(InstallPlugin plugin) throws ProvisioningException {
                plugin.postInstall(ProvisioningRuntime.this);
            }
        }, InstallPlugin.class);

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

    @Override
    public void close() {
        layout.close();
        if (messageWriter.isVerboseEnabled()) {
            final long time = System.currentTimeMillis() - startTime;
            final long seconds = time / 1000;
            messageWriter.verbose("Done in %d.%d seconds", seconds, (time - seconds * 1000));
        }
    }
}
