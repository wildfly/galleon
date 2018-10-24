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
package org.jboss.galleon;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.diff.FsDiff;
import org.jboss.galleon.diff.FsEntry;
import org.jboss.galleon.diff.FsEntryFactory;
import org.jboss.galleon.layout.ProvisioningLayout;
import org.jboss.galleon.layout.ProvisioningLayoutFactory;
import org.jboss.galleon.layout.ProvisioningPlan;
import org.jboss.galleon.runtime.FeaturePackRuntimeBuilder;
import org.jboss.galleon.runtime.ProvisioningRuntime;
import org.jboss.galleon.runtime.ProvisioningRuntimeBuilder;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;
import org.jboss.galleon.universe.UniverseFactoryLoader;
import org.jboss.galleon.universe.UniverseResolver;
import org.jboss.galleon.universe.UniverseResolverBuilder;
import org.jboss.galleon.universe.UniverseSpec;
import org.jboss.galleon.util.StateHistoryUtils;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.HashUtils;
import org.jboss.galleon.util.IoUtils;
import org.jboss.galleon.util.LayoutUtils;
import org.jboss.galleon.util.PathsUtils;
import org.jboss.galleon.xml.ProvisionedStateXmlParser;
import org.jboss.galleon.xml.ProvisioningXmlParser;
import org.jboss.galleon.xml.ProvisioningXmlWriter;

/**
 *
 * @author Alexey Loubyansky
 */
public class ProvisioningManager implements AutoCloseable {

    public static class Builder extends UniverseResolverBuilder<Builder> {

        private String encoding = "UTF-8";
        private Path installationHome;
        private ProvisioningLayoutFactory layoutFactory;
        private MessageWriter messageWriter;
        private UniverseResolver resolver;

        private Builder() {
        }

        public Builder setEncoding(String encoding) {
            this.encoding = encoding;
            return this;
        }

        public Builder setInstallationHome(Path installationHome) {
            this.installationHome = installationHome;
            return this;
        }

        public Builder setLayoutFactory(ProvisioningLayoutFactory layoutFactory) throws ProvisioningException {
            if(ufl != null) {
                throw new ProvisioningException("Universe factory loader has already been initialized which conflicts with the initialization of provisioning layout factory");
            }
            if(resolver != null) {
                throw new ProvisioningException("Universe resolver has already been initialized which conflicts with the initialization of provisioning layout factory");
            }
            this.layoutFactory = layoutFactory;
            return this;
        }

        @Override
        protected UniverseFactoryLoader getUfl() throws ProvisioningException {
            if(layoutFactory != null) {
                throw new ProvisioningException("Provisioning layout factory has already been initialized which conflicts with the initialization of universe factory loader");
            }
            if(resolver != null) {
                throw new ProvisioningException("Universe resolver has already been initialized which conflicts with the initialization of universe factory loader");
            }
            return super.getUfl();

        }
        public Builder setMessageWriter(MessageWriter messageWriter) {
            this.messageWriter = messageWriter;
            return this;
        }

        public Builder setUniverseResolver(UniverseResolver resolver) throws ProvisioningException {
            if(ufl != null) {
                throw new ProvisioningException("Universe factory loader has already been initialized which conflicts with the initialization of universe resolver");
            }
            if(layoutFactory != null) {
                throw new ProvisioningException("Provisioning layout factory has already been initialized which conflicts with the initialization of universe resolver");
            }
            this.resolver = resolver;
            return this;
        }

        public ProvisioningManager build() throws ProvisioningException {
            return new ProvisioningManager(this);
        }

        protected UniverseResolver getUniverseResolver() throws ProvisioningException {
            return resolver == null ? buildUniverseResolver() : resolver;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final String encoding;
    private final Path home;
    private final MessageWriter log;

    private final UniverseResolver universeResolver;
    private ProvisioningLayoutFactory layoutFactory;
    private boolean closeLayoutFactory;
    private ProvisioningConfig provisioningConfig;

    private ProvisioningManager(Builder builder) throws ProvisioningException {
        PathsUtils.assertInstallationDir(builder.installationHome);
        this.home = builder.installationHome;
        this.encoding = builder.encoding;
        this.log = builder.messageWriter == null ? DefaultMessageWriter.getDefaultInstance() : builder.messageWriter;
        if(builder.layoutFactory != null) {
            layoutFactory = builder.layoutFactory;
            closeLayoutFactory = false;
            universeResolver = layoutFactory.getUniverseResolver();
        } else {
            universeResolver = builder.getUniverseResolver();
        }
    }

    /**
     * Provisioning layout factory
     *
     * @return  provisioning layout factory
     */
    public ProvisioningLayoutFactory getLayoutFactory() {
        if(layoutFactory == null) {
            closeLayoutFactory = true;
            layoutFactory = ProvisioningLayoutFactory.getInstance(universeResolver);
        }
        return layoutFactory;
    }

    /**
     * Location of the installation.
     *
     * @return  location of the installation
     */
    public Path getInstallationHome() {
        return home;
    }

    /**
     * Add named universe spec to the provisioning configuration
     *
     * @param name  universe name
     * @param universeSpec  universe spec
     * @throws ProvisioningException  in case of an error
     */
    public void addUniverse(String name, UniverseSpec universeSpec) throws ProvisioningException {
        final ProvisioningConfig config = ProvisioningConfig.builder(getProvisioningConfig()).addUniverse(name, universeSpec).build();
        try {
            ProvisioningXmlWriter.getInstance().write(config, PathsUtils.getProvisioningXml(home));
        } catch (Exception e) {
            throw new ProvisioningException(Errors.writeFile(PathsUtils.getProvisioningXml(home)), e);
        }
        this.provisioningConfig = config;
    }

    /**
     * Removes universe spec associated with the name from the provisioning configuration
     * @param name  name of the universe spec or null for the default universe spec
     * @throws ProvisioningException  in case of an error
     */
    public void removeUniverse(String name) throws ProvisioningException {
        ProvisioningConfig config = getProvisioningConfig();
        if(config == null || !config.hasUniverse(name)) {
            return;
        }
        config = ProvisioningConfig.builder(config).removeUniverse(name).build();
        try {
            ProvisioningXmlWriter.getInstance().write(config, PathsUtils.getProvisioningXml(home));
        } catch (Exception e) {
            throw new ProvisioningException(Errors.writeFile(PathsUtils.getProvisioningXml(home)), e);
        }
        this.provisioningConfig = config;
    }

    /**
     * Set the default universe spec for the installation
     *
     * @param universeSpec  universe spec
     * @throws ProvisioningException  in case of an error
     */
    public void setDefaultUniverse(UniverseSpec universeSpec) throws ProvisioningException {
        addUniverse(null, universeSpec);
    }

    /**
     * Last recorded installation provisioning configuration or null in case
     * the installation is not found at the specified location.
     *
     * @return  the last recorded provisioning installation configuration
     * @throws ProvisioningException  in case any error occurs
     */
    public ProvisioningConfig getProvisioningConfig() throws ProvisioningException {
        if (provisioningConfig == null) {
            provisioningConfig = ProvisioningXmlParser.parse(PathsUtils.getProvisioningXml(home));
        }
        return provisioningConfig;
    }

    /**
     * Returns the detailed description of the provisioned installation.
     *
     * @return  detailed description of the provisioned installation
     * @throws ProvisioningException  in case there was an error reading the description from the disk
     */
    public ProvisionedState getProvisionedState() throws ProvisioningException {
        return ProvisionedStateXmlParser.parse(PathsUtils.getProvisionedStateXml(home));
    }

    /**
     * Installs a feature-pack provided as a local archive.
     * This method calls install(featurePack, true).
     *
     * @param featurePack  path to feature-pack archive
     * @throws ProvisioningException  in case installation fails
     */
    public void install(Path featurePack) throws ProvisioningException {
        install(featurePack, true);
    }

    /**
     * Installs a feature-pack provided as a local archive.
     *
     * @param featurePack  path to feature-pack archive
     * @param installInUniverse  whether to install the feature-pack artifact into the universe repository
     * @throws ProvisioningException  in case installation fails
     */
    public void install(Path featurePack, boolean installInUniverse) throws ProvisioningException {
        install(getLayoutFactory().addLocal(featurePack, installInUniverse));
    }

    /**
     * Installs the specified feature-pack.
     *
     * @param fpl  feature-pack location
     * @throws ProvisioningException  in case the installation fails
     */
    public void install(FeaturePackLocation fpl) throws ProvisioningException {
        install(FeaturePackConfig.forLocation(fpl));
    }

    /**
     * Installs the specified feature-pack taking into account provided plug-in options.
     *
     * @param  fpl feature-pack location
     * @param options  plug-in options
     * @throws ProvisioningException  in case the installation fails
     */
    public void install(FeaturePackLocation fpl, Map<String, String> options) throws ProvisioningException {
        install(FeaturePackConfig.forLocation(fpl), options);
    }

    /**
     * Installs the desired feature-pack configuration.
     *
     * @param fpConfig  the desired feature-pack configuration
     * @throws ProvisioningException  in case the installation fails
     */
    public void install(FeaturePackConfig fpConfig) throws ProvisioningException {
        install(fpConfig, Collections.emptyMap());
    }

    public void install(FeaturePackConfig fpConfig, Map<String, String> options) throws ProvisioningException {
        ProvisioningConfig config = getProvisioningConfig();
        if(config == null) {
            config = ProvisioningConfig.builder().build();
        }
        try(ProvisioningLayout<FeaturePackRuntimeBuilder> layout = getLayoutFactory().newConfigLayout(config, ProvisioningRuntimeBuilder.FP_RT_FACTORY, false)) {
            final UniverseSpec configuredUniverse = getConfiguredUniverse(fpConfig.getLocation());
            layout.install(configuredUniverse == null ? fpConfig : FeaturePackConfig.builder(fpConfig.getLocation().replaceUniverse(configuredUniverse)).init(fpConfig).build(), options);
            try (ProvisioningRuntime runtime = getRuntime(layout)) {
                doProvision(runtime, false);
            }
        }
    }

    /**
     * Uninstalls the specified feature-pack.
     *
     * @param fpid  feature-pack ID
     * @throws ProvisioningException  in case the uninstallation fails
     */
    public void uninstall(FeaturePackLocation.FPID fpid) throws ProvisioningException {
        uninstall(fpid, Collections.emptyMap());
    }

    /**
     * Uninstalls the specified feature-pack.
     *
     * @param fpid  feature-pack ID
     * @param pluginOptions  provisioning plugin options
     * @throws ProvisioningException  in case of a failure
     */
    public void uninstall(FeaturePackLocation.FPID fpid, Map<String, String> pluginOptions) throws ProvisioningException {
        final ProvisioningConfig config = getProvisioningConfig();
        if(config == null || !config.hasFeaturePackDeps()) {
            throw new ProvisioningException(Errors.unknownFeaturePack(fpid));
        }
        try(ProvisioningLayout<FeaturePackRuntimeBuilder> layout = getLayoutFactory().newConfigLayout(config, ProvisioningRuntimeBuilder.FP_RT_FACTORY, false)) {
            layout.uninstall(resolveUniverseSpec(fpid.getLocation()).getFPID(), pluginOptions);
            try (ProvisioningRuntime runtime = getRuntime(layout)) {
                doProvision(runtime, false);
            }
        }
    }

    /**
     * (Re-)provisions the current installation to the desired specification.
     *
     * @param provisioningConfig  the desired installation specification
     * @throws ProvisioningException  in case provisioning fails
     */
    public void provision(ProvisioningConfig provisioningConfig) throws ProvisioningException {
        provision(provisioningConfig, Collections.emptyMap());
    }

    /**
     * (Re-)provisions the current installation to the desired specification.
     *
     * @param provisioningConfig  the desired installation specification
     * @param options  feature-pack plug-ins options
     * @throws ProvisioningException  in case provisioning fails
     */
    public void provision(ProvisioningConfig provisioningConfig, Map<String, String> options) throws ProvisioningException {
        try(ProvisioningRuntime runtime = getRuntime(provisioningConfig, options)) {
            doProvision(runtime, false);
        }
    }

    /**
     * (Re-)provisions the current installation to the desired specification.
     *
     * @param provisioningLayout  pre-built provisioning layout
     * @throws ProvisioningException  in case provisioning fails
     */
    public void provision(ProvisioningLayout<?> provisioningLayout) throws ProvisioningException {
        try(ProvisioningRuntime runtime = getRuntime(provisioningLayout)) {
            doProvision(runtime, false);
        }
    }

    /**
     * Provision the state described in the specified XML file.
     *
     * @param provisioningXml  file describing the desired provisioned state
     * @throws ProvisioningException  in case provisioning fails
     */
    public void provision(Path provisioningXml) throws ProvisioningException {
        provision(provisioningXml, Collections.emptyMap());
    }

    /**
     * Provision the state described in the specified XML file.
     *
     * @param provisioningXml file describing the desired provisioned state
     * @param options feature-pack plug-ins options
     * @throws ProvisioningException in case provisioning fails
     */
    public void provision(Path provisioningXml, Map<String, String> options) throws ProvisioningException {
        try(ProvisioningRuntime runtime = getRuntime(ProvisioningXmlParser.parse(provisioningXml), options)) {
            doProvision(runtime, false);
        }
    }

    /**
     * Query for available updates and patches for feature-packs in this layout.
     *
     * @param includeTransitive  whether to include transitive dependencies into the result
     * @return  available updates
     * @throws ProvisioningException in case of a failure
     */
    public ProvisioningPlan getUpdates(boolean includeTransitive) throws ProvisioningException {
        final ProvisioningConfig config = getProvisioningConfig();
        ProvisioningPlan plan;
        if (config == null) {
            plan = ProvisioningPlan.builder();
        } else {
            try (ProvisioningLayout<?> layout = getLayoutFactory().newConfigLayout(config)) {
                plan = layout.getUpdates(includeTransitive);
            }
        }
        return plan;
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
        final ProvisioningConfig config = getProvisioningConfig();
        ProvisioningPlan plan;
        if (config == null) {
            plan = ProvisioningPlan.builder();
        } else {
            try (ProvisioningLayout<?> layout = getLayoutFactory().newConfigLayout(config)) {
                plan = layout.getUpdates(producers);
            }
        }
        return plan;
    }

    /**
     * Apply provisioning plan to the currently provisioned installation
     *
     * @param plan  provisioning plan
     * @throws ProvisioningException  in case of a failure
     */
    public void apply(ProvisioningPlan plan) throws ProvisioningException {
        apply(plan, Collections.emptyMap());
    }

    /**
     * Apply provisioning plan to the currently provisioned installation
     *
     * @param plan  provisioning plan
     * @param options  provisioning plugin options
     * @throws ProvisioningException  in case of a failure
     */
    public void apply(ProvisioningPlan plan, Map<String, String> options) throws ProvisioningException {
        ProvisioningConfig config = getProvisioningConfig();
        if(config == null) {
            config = ProvisioningConfig.builder().build();
        }
        try (ProvisioningLayout<FeaturePackRuntimeBuilder> layout = getLayoutFactory().newConfigLayout(config, ProvisioningRuntimeBuilder.FP_RT_FACTORY, false)) {
            layout.apply(plan, options);
            try (ProvisioningRuntime runtime = getRuntime(layout)) {
                doProvision(runtime, false);
            }
        }
    }

    /**
     * Checks whether the provisioning state history is not empty and can be used to undo
     * the last provisioning operation.
     *
     * @return  true if the provisioning state history is not empty, otherwise false
     * @throws ProvisioningException  in case of an error checking the history state
     */
    public boolean isUndoAvailable() throws ProvisioningException {
        return StateHistoryUtils.isUndoAvailable(home, log);
    }

    /**
     * Returns the state history limit for the installation.
     *
     * @return  state history limit
     * @throws ProvisioningException  in case of a failure to read the value
     */
    public int getStateHistoryLimit() throws ProvisioningException {
        return StateHistoryUtils.readStateHistoryLimit(home, log);
    }

    /**
     * Sets the new state history limit to the specified value.
     * The value cannot be negative.
     *
     * @param limit  new state history limit
     * @throws ProvisioningException  in case of a failure
     */
    public void setStateHistoryLimit(int limit) throws ProvisioningException {
        StateHistoryUtils.writeStateHistoryLimit(home, limit, log);
    }

    /**
     * Clears the state history.
     *
     * @throws ProvisioningException in case of a failure
     */
    public void clearStateHistory() throws ProvisioningException {
        StateHistoryUtils.clearStateHistory(home, log);
    }

    /**
     * Goes back to the previous provisioning state recorded in the provisioning state history.
     * If the history is empty, the method throws an exception.
     *
     * @throws ProvisioningException  in case of a failure
     */
    public void undo() throws ProvisioningException {
        try(ProvisioningRuntime runtime = getRuntime(StateHistoryUtils.readUndoConfig(home, log))) {
            doProvision(runtime, true);
        }
    }

    /**
     * Exports the current provisioning configuration of the installation to
     * the specified file.
     *
     * @param location  file to which the current installation configuration should be exported
     * @throws ProvisioningException  in case the provisioning configuration record is missing
     * @throws IOException  in case writing to the specified file fails
     */
    public void exportProvisioningConfig(Path location) throws ProvisioningException, IOException {
        Path exportPath = location;
        final Path userProvisionedXml = PathsUtils.getProvisioningXml(home);
        if(!Files.exists(userProvisionedXml)) {
            throw new ProvisioningException("Provisioned state record is missing for " + home);
        }
        if(Files.isDirectory(exportPath)) {
            exportPath = exportPath.resolve(userProvisionedXml.getFileName());
        }
        IoUtils.copy(userProvisionedXml, exportPath);
    }
/*
    public void exportConfigurationChanges(Path location, FPID fpid, Map<String, String> options) throws ProvisioningException, IOException {
        final ProvisioningConfig configuration = getProvisioningConfig();
        if (configuration == null) {
            throw new ProvisioningException("Provisioned state record is missing for " + home);
        }

        try (ProvisioningLayout<FeaturePackLayout> layout = getLayoutFactory().newConfigLayout(configuration)) {
            final FeaturePackPluginVisitor<UserConfigDiffPlugin> visitor = new FeaturePackPluginVisitor<UserConfigDiffPlugin>() {
                @Override
                public void visitPlugin(UserConfigDiffPlugin plugin) throws ProvisioningException {
                    plugin.userConfigDiff(getProvisionedState(), layout, getInstallationHome(), log);
                }
            };
            layout.visitPlugins(visitor, UserConfigDiffPlugin.class);
        }

        / *
        try (ProvisioningRuntime runtime = ProvisioningRuntimeBuilder.newInstance(log)
                .initLayout(getLayoutFactory(), configuration)
                .setEncoding(encoding)
                .addOptions(options)
                // .setOperation(fpid != null ? "diff-to-feature-pack" : "diff")
                .build()) {
            runtime.provision();
            if (fpid != null) {
                ProvisioningRuntime.exportToFeaturePack(runtime, fpid, location, home);
            } else {
                // execute the plug-ins
                runtime.diff(location, home);
            }
        }
        * /
    }
*/
    /*
    public void upgrade(ArtifactCoords.Gav fpGav, Map<String, String> options) throws ProvisioningException, IOException {
        ProvisioningConfig configuration = this.getProvisioningConfig();
        Path tempInstallationDir = IoUtils.createRandomTmpDir();
        Path stagedDir = IoUtils.createRandomTmpDir();
        try (ProvisioningManager reference = new ProvisioningManager(ProvisioningManager.builder()
                    .setLayoutFactory(getLayoutFactory())
                    .setEncoding(encoding)
                    .setInstallationHome(tempInstallationDir)
                    .setMessageWriter(new MessageWriter() {
                        @Override
                        public void verbose(Throwable cause, CharSequence message) {
                            return;
                        }

                        @Override
                        public void print(Throwable cause, CharSequence message) {
                            log.print(cause, message);
                        }

                        @Override
                        public void error(Throwable cause, CharSequence message) {
                            log.error(cause, message);
                        }

                        @Override
                        public boolean isVerboseEnabled() {
                            return false;
                        }

                        @Override
                        public void close() throws Exception {
                            return;
                        }
                    }))) {
            reference.provision(configuration);
        }
        Files.createDirectories(stagedDir);
        try (ProvisioningManager reference = new ProvisioningManager(ProvisioningManager.builder()
                    .setLayoutFactory(getLayoutFactory())
                    .setEncoding(encoding)
                    .setInstallationHome(stagedDir)
                    .setMessageWriter(new MessageWriter() {
                        @Override
                        public void verbose(Throwable cause, CharSequence message) {
                            return;
                        }

                        @Override
                        public void print(Throwable cause, CharSequence message) {
                            log.print(cause, message);
                        }

                        @Override
                        public void error(Throwable cause, CharSequence message) {
                            log.error(cause, message);
                        }

                        @Override
                        public boolean isVerboseEnabled() {
                            return false;
                        }

                        @Override
                        public void close() throws Exception {
                            return;
                        }
                    }))) {
            reference.provision(ProvisioningConfig.builder().addFeaturePackDep(FeaturePackConfig.forLocation(LegacyGalleon1Universe.toFpl(fpGav))).build());
        }
        try (ProvisioningRuntime runtime = ProvisioningRuntimeBuilder.newInstance(log)
                .initLayout(getLayoutFactory(), configuration).setEncoding(encoding)
                // .setInstallDir(tempInstallationDir)
                .addOptions(options)
                // .setOperation("upgrade")
                .build()) {
            // install the software
            Files.createDirectories(tempInstallationDir.resolve("model_diff"));
            // execute the plug-ins
            final ProvisioningDiffResult diff = runtime.diff(tempInstallationDir.resolve("model_diff"), home);
            // execute the plug-ins
            runtime.executeUpgradePlugins(diff, home);
            if (Files.exists(home)) {
                IoUtils.recursiveDelete(home);
            }
            try {
                IoUtils.copy(stagedDir, home);
            } catch (IOException e) {
                throw new ProvisioningException(Errors.copyFile(stagedDir, home));
            }

        } finally {
            IoUtils.recursiveDelete(tempInstallationDir);
            IoUtils.recursiveDelete(stagedDir);
        }
    }
    */

    public ProvisioningRuntime getRuntime(ProvisioningConfig provisioningConfig)
            throws ProvisioningException {
        return getRuntime(provisioningConfig, Collections.emptyMap());
    }

    private ProvisioningRuntime getRuntime(ProvisioningConfig provisioningConfig, Map<String, String> pluginOptions)
            throws ProvisioningException {
        return getRuntimeInternal(getLayoutFactory().newConfigLayout(provisioningConfig, ProvisioningRuntimeBuilder.FP_RT_FACTORY, pluginOptions));
    }

    public ProvisioningRuntime getRuntime(ProvisioningLayout<?> provisioningLayout)
            throws ProvisioningException {
        return getRuntimeInternal(provisioningLayout.transform(ProvisioningRuntimeBuilder.FP_RT_FACTORY));
    }

    private ProvisioningRuntime getRuntimeInternal(ProvisioningLayout<FeaturePackRuntimeBuilder> layout)
            throws ProvisioningException {
        return ProvisioningRuntimeBuilder.newInstance(log)
                .initRtLayout(layout)
                .setEncoding(encoding)
                .build();
    }

    private void doProvision(ProvisioningRuntime runtime, boolean undo) throws ProvisioningException {

        FsDiff diff = null;
        final ProvisioningConfig config = getProvisioningConfig();
        if(config != null && config.hasFeaturePackDeps()) {
            diff = detectUserChanges(config);
        }

        try {
            runtime.provision();
            if(runtime.getProvisioningConfig().hasFeaturePackDeps()) {
                persistHashes(runtime);
            }
            if(undo) {
                final Map<String, Boolean> undoTasks = StateHistoryUtils.readUndoTasks(home, log);
                if(!undoTasks.isEmpty()) {
                    final Path staged = runtime.getStagedDir();
                    for(Map.Entry<String, Boolean> entry : undoTasks.entrySet()) {
                        final Path stagedPath = staged.resolve(entry.getKey());
                        if(entry.getValue()) {
                            final Path homePath = home.resolve(entry.getKey());
                            if(Files.exists(homePath)) {
                                try {
                                    IoUtils.copy(homePath, stagedPath);
                                } catch (IOException e) {
                                    throw new ProvisioningException(Errors.copyFile(homePath, stagedPath), e);
                                }
                            }
                        } else {
                            IoUtils.recursiveDelete(stagedPath);
                        }
                    }
                }
            }
            Map<String, Boolean> undoTasks = Collections.emptyMap();
            if(diff != null && !diff.isEmpty()) {
                undoTasks = applyUserChanges(diff, runtime.getStagedDir());
            }
            PathsUtils.replaceDist(runtime.getStagedDir(), home, undo, undoTasks, log);
        } finally {
            this.provisioningConfig = null;
        }
    }

    private FeaturePackLocation resolveUniverseSpec(FeaturePackLocation fpl) throws ProvisioningException {
        final UniverseSpec universeSpec = getConfiguredUniverse(fpl);
        return universeSpec == null ? fpl : fpl.replaceUniverse(universeSpec);
    }

    private UniverseSpec getConfiguredUniverse(FeaturePackLocation fpl)
            throws ProvisioningException, ProvisioningDescriptionException {
        final ProvisioningConfig config = getProvisioningConfig();
        if(config == null) {
            return null;
        }
        if(fpl.hasUniverse()) {
            final String name = fpl.getUniverse().toString();
            if(config.hasUniverse(name)) {
                return config.getUniverseSpec(name);
            }
            return null;
        }
        return config.getDefaultUniverse();
    }

    @Override
    public void close() {
        if(closeLayoutFactory) {
            layoutFactory.close();
        }
    }

    private FsDiff detectUserChanges(ProvisioningConfig config) throws ProvisioningException {
        log.verbose("Detecting user changes");
        final Path hashesDir = LayoutUtils.getHashesDir(getInstallationHome());
        if(Files.exists(hashesDir)) {
            final FsEntry originalState = new FsEntry(null, hashesDir);
            readHashes(originalState, new ArrayList<>());
            final FsEntry currentState = getFsEntryFactory().forPath(getInstallationHome());
            return FsDiff.diff(originalState, currentState);
        }
        try(ProvisioningRuntime rt = getRuntime(config)) {
            rt.provision();
            final FsEntryFactory fsFactory = getFsEntryFactory();
            final FsEntry originalState = fsFactory.forPath(rt.getStagedDir());
            final FsEntry currentState = fsFactory.forPath(getInstallationHome());
            final long startTime = System.nanoTime();
            final FsDiff fsDiff = FsDiff.diff(originalState, currentState);
            if (log.isVerboseEnabled()) {
                final long timeMs = (System.nanoTime() - startTime) / 1000000;
                final long timeSec = timeMs / 1000;
                log.verbose("  fs diff took %d.%d seconds", timeSec, (timeMs - timeSec * 1000));
            }
            return fsDiff;
        }
    }

    private static void readHashes(FsEntry parent, List<FsEntry> dirs) throws ProvisioningException {
        int dirsTotal = 0;
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(parent.getPath())) {
            for(Path child : stream) {
                if(child.getFileName().toString().equals(Constants.HASHES)) {
                    try(BufferedReader reader = Files.newBufferedReader(child)) {
                        String line = reader.readLine();
                        while(line != null) {
                            new FsEntry(parent, line, HashUtils.hexStringToByteArray(reader.readLine()));
                            line = reader.readLine();
                        }
                    } catch (IOException e) {
                        throw new ProvisioningException("Failed to read hashes", e);
                    }
                } else {
                    dirs.add(new FsEntry(parent, child));
                    ++dirsTotal;
                }
            }
        } catch (IOException e) {
            throw new ProvisioningException("Failed to read hashes", e);
        }
        while(dirsTotal > 0) {
            readHashes(dirs.remove(dirs.size() - 1), dirs);
            --dirsTotal;
        }
    }

    private FsEntryFactory fsEntryFactory;

    private FsEntryFactory getFsEntryFactory() {
        if(fsEntryFactory == null) {
            fsEntryFactory = FsEntryFactory.getInstance()
                .filter("/.galleon")
                .filter("*.glnew")
                .filter("/domain/configuration/domain_xml_history")
                .filter("/domain/configuration/host_xml_history")
                .filter("/domain/data")
                .filter("/domain/log")
                .filter("/domain/servers")
                .filter("/standalone/configuration/standalone_xml_history")
                .filter("/standalone/data")
                .filter("/standalone/log")
                .filter("/standalone/tmp");
        }
        return fsEntryFactory;
    }

    private static final char REPLAY_SKIP = 'S';
    private static final char REPLAY_ADDED = '+';
    private static final char REPLAY_MODIFIED = 'M';
    private static final char REPLAY_REMOVED = '-';

    private Map<String, Boolean> applyUserChanges(FsDiff diff, Path home) throws ProvisioningException {
        log.print("Replaying your changes on top");
        Map<String, Boolean> undoTasks = Collections.emptyMap();
        if(diff.hasRemovedEntries()) {
            for(FsEntry removed : diff.getRemovedEntries()) {
                final Path target = home.resolve(removed.getRelativePath());
                if(Files.exists(target)) {
                    logReplay(REPLAY_REMOVED, removed.getRelativePath(), null);
                    IoUtils.recursiveDelete(target);
                } else {
                    removedPathNotPresent(removed);
                    undoTasks = CollectionUtils.putLinked(undoTasks, removed.getRelativePath(), false);
                }
            }
        }
        if(diff.hasAddedEntries()) {
            for(FsEntry added : diff.getAddedEntries()) {
                undoTasks = addFsEntry(home, added, undoTasks);
            }
        }
        if(diff.hasModifiedEntries()) {
            for(FsEntry[] modified : diff.getModifiedEntries()) {
                final FsEntry update = modified[1];
                final Path target = home.resolve(update.getRelativePath());
                char action = REPLAY_MODIFIED;
                String warning = null;
                if(Files.exists(target)) {
                    final byte[] targetHash;
                    try {
                        targetHash = HashUtils.hashPath(target);
                    } catch (IOException e) {
                        throw new ProvisioningException(Errors.hashCalculation(target), e);
                    }
                    if(Arrays.equals(update.getHash(), targetHash)) {
                        if(!modifiedPathMatchesExisting(update)) {
                            action = REPLAY_SKIP;
                        }
                        undoTasks = CollectionUtils.putLinked(undoTasks, update.getRelativePath(), true);
                    } else if (!Arrays.equals(modified[0].getHash(), targetHash)) {
                        if (originalOfModifiedPathUpdated(update)) {
                            warning = "the original has changed in the updated version";
                            glnew(target);
                        } else {
                            action = REPLAY_SKIP;
                        }
                    } else if (modifiedPathConflict(update)) {
                        glnew(target);
                    } else {
                        action = REPLAY_SKIP;
                    }
                } else if(modifiedPathNotPresent(update)) {
                    warning = "the original has been removed from the updated version";
                    //action = REPLAY_ADDED;
                } else {
                    action = REPLAY_SKIP;
                }
                if (action != REPLAY_SKIP) {
                    logReplay(action, update.getRelativePath(), warning);
                    try {
                        IoUtils.copy(update.getPath(), target);
                    } catch (IOException e) {
                        throw new ProvisioningException(Errors.copyFile(update.getPath(), target), e);
                    }
                }
            }
        }
        return undoTasks;
    }

    private Map<String, Boolean> addFsEntry(Path home, FsEntry added, Map<String, Boolean> undoTasks) throws ProvisioningException {
        final Path target = home.resolve(added.getRelativePath());
        char action = REPLAY_ADDED;
        String warning = null;
        if(Files.exists(target)) {
            if(added.isDir()) {
                for (FsEntry child : added.getChildren()) {
                    undoTasks = addFsEntry(home, child, undoTasks);
                }
                return undoTasks;
            }
            final byte[] targetHash;
            try {
                targetHash = HashUtils.hashPath(target);
            } catch (IOException e) {
                throw new ProvisioningException(Errors.hashCalculation(target), e);
            }
            if(Arrays.equals(added.getHash(), targetHash)) {
                if(!addedPathMatchesExisting(added)) {
                    action = REPLAY_SKIP;
                } else {
                    warning = "matches the updated version";
                    //action = REPLAY_MODIFIED;
                }
                undoTasks = CollectionUtils.putLinked(undoTasks, added.getRelativePath(), true);
            } else if(addedPathConflict(added) && !added.isDir()) {
                warning = "conflicts with the updated version";
                glnew(target);
                //action = REPLAY_MODIFIED;
            }
        }
        if (action != REPLAY_SKIP) {
            logReplay(action, added.getRelativePath(), warning);
            try {
                IoUtils.copy(added.getPath(), target);
            } catch (IOException e) {
                throw new ProvisioningException(Errors.copyFile(added.getPath(), target), e);
            }
        }
        return undoTasks;
    }

    private void logReplay(char action, String path, String warning) {
        final StringBuilder buf = new StringBuilder();
        buf.append(' ').append(action).append(' ').append(path);
        if(warning != null) {
            buf.setCharAt(0, '!');
            buf.append(" (").append(warning).append(')');
        }
        log.print(buf.toString());
    }

    private static void glnew(final Path target) throws ProvisioningException {
        try {
            IoUtils.copy(target, target.getParent().resolve(target.getFileName() + Constants.DOT_GLNEW));
        } catch (IOException e) {
            throw new ProvisioningException("Failed to persist " + target.getParent().resolve(target.getFileName() + Constants.DOT_GLNEW), e);
        }
    }

    private boolean addedPathMatchesExisting(FsEntry userEntry) throws ProvisioningException {
        //log.verbose("%s added by the user matches the file from the updated version", userEntry.getRelativePath());
        return false;
    }

    private boolean addedPathConflict(FsEntry userEntry) throws ProvisioningException {
        //log.verbose("%s added by the user conflicts with the updated version", userEntry.getRelativePath());
        return true;
    }

    private boolean modifiedPathMatchesExisting(FsEntry userEntry) throws ProvisioningException {
        //log.verbose("%s modified by the user matches its updated version", userEntry.getRelativePath());
        return false;
    }

    private boolean modifiedPathConflict(FsEntry userEntry) throws ProvisioningException {
        //log.verbose("%s modified by the user conflicts with its updated version", userEntry.getRelativePath());
        return true;
    }

    private boolean originalOfModifiedPathUpdated(FsEntry userEntry) throws ProvisioningException {
        //log.print("WARN: %s original modified by the user has changed in the new version", userEntry.getRelativePath());
        return true;
    }

    private boolean modifiedPathNotPresent(FsEntry userEntry) throws ProvisioningException {
        //log.print("WARN: " + userEntry.getRelativePath() + " modified by the user is not present in the updated version");
        return true;
    }

    private void removedPathNotPresent(FsEntry userEntry) throws ProvisioningException {
        log.verbose("%s removed by the user is not present in the updated version", userEntry.getRelativePath());
    }

    private void persistHashes(ProvisioningRuntime runtime) throws ProvisioningException {
        final long startTime = System.nanoTime();
        final FsEntry root = getFsEntryFactory().forPath(runtime.getStagedDir());
        if (root.hasChildren()) {
            final Path hashes = LayoutUtils.getHashesDir(runtime.getStagedDir());
            try {
                Files.createDirectories(hashes);
            } catch (IOException e) {
                throw new ProvisioningException("Failed to persist hashes", e);
            }
            final List<FsEntry> dirs = new ArrayList<>();
            persistChildHashes(hashes, root, dirs, hashes);
            if(!dirs.isEmpty()) {
                for(int i = dirs.size() - 1; i >= 0; --i) {
                    persistDirHashes(hashes, dirs.get(i), dirs);
                }
            }
        }
        if(log.isVerboseEnabled()) {
            final long timeMs = (System.nanoTime() - startTime) / 1000000;
            final long timeSec = timeMs / 1000;
            log.print("Hashing took %d.%d seconds", timeSec, (timeMs - timeSec * 1000));
        }
    }

    private static void persistDirHashes(Path hashes, FsEntry entry, List<FsEntry> dirs) throws ProvisioningException {
        final Path target = hashes.resolve(entry.getRelativePath());
        try {
            Files.createDirectory(target);
        } catch (IOException e) {
            throw new ProvisioningException("Failed to persist hashes", e);
        }
        if (entry.hasChildren()) {
            persistChildHashes(hashes, entry, dirs, target);
        }
    }

    private static void persistChildHashes(Path hashes, FsEntry entry, List<FsEntry> dirs, final Path target)
            throws ProvisioningException {
        int dirsTotal = 0;
        BufferedWriter writer = null;
        try {
            for (FsEntry child : entry.getChildren()) {
                if (!child.isDir()) {
                    if (writer == null) {
                        writer = Files.newBufferedWriter(target.resolve(Constants.HASHES));
                    }
                    writer.write(child.getName());
                    writer.newLine();
                    writer.write(HashUtils.bytesToHexString(child.getHash()));
                    writer.newLine();
                } else {
                    dirs.add(child);
                    ++dirsTotal;
                }
            }
        } catch (IOException e) {
            throw new ProvisioningException("Failed to persist hashes", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        while (dirsTotal > 0) {
            persistDirHashes(hashes, dirs.remove(dirs.size() - 1), dirs);
            --dirsTotal;
        }
    }
}
