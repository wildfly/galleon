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


import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.layout.ProvisioningLayout;
import org.jboss.galleon.layout.ProvisioningLayoutFactory;
import org.jboss.galleon.runtime.FeaturePackRuntimeBuilder;
import org.jboss.galleon.runtime.ProvisioningRuntime;
import org.jboss.galleon.runtime.ProvisioningRuntimeBuilder;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.UniverseFactoryLoader;
import org.jboss.galleon.universe.UniverseResolver;
import org.jboss.galleon.universe.UniverseResolverBuilder;
import org.jboss.galleon.universe.UniverseSpec;
import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.util.IoUtils;
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

        /**
         * @deprecated
         *
         * @param arm  artifact repository manager
         * @return  this builder instance
         */
        public Builder setArtifactResolver(ArtifactRepositoryManager arm) {
            try {
                return addArtifactResolver(arm);
            } catch (ProvisioningException e) {
                throw new IllegalStateException("Failed to set artifact resolver", e);
            }
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

    public static void checkInstallationDir(Path installationHome) throws ProvisioningException {
        if (!Files.exists(installationHome)) {
            return;
        }
        if (!Files.isDirectory(installationHome)) {
            throw new ProvisioningException(Errors.notADir(installationHome));
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(installationHome)) {
            boolean usableDir = true;
            final Iterator<Path> i = stream.iterator();
            while (i.hasNext()) {
                if (i.next().getFileName().toString().equals(Constants.PROVISIONED_STATE_DIR)) {
                    usableDir = true;
                    break;
                } else {
                    usableDir = false;
                }
            }
            if (!usableDir) {
                throw new ProvisioningException(Errors.homeDirNotUsable(installationHome));
            }
        } catch (IOException e) {
            throw new ProvisioningException(Errors.readDirectory(installationHome));
        }
    }

    private final String encoding;
    private final Path installationHome;
    private final MessageWriter messageWriter;

    private final UniverseResolver universeResolver;
    private ProvisioningLayoutFactory layoutFactory;
    private boolean closeLayoutFactory;
    private ProvisioningConfig provisioningConfig;

    private ProvisioningManager(Builder builder) throws ProvisioningException {
        this.encoding = builder.encoding;
        this.installationHome = builder.installationHome;
        this.messageWriter = builder.messageWriter == null ? DefaultMessageWriter.getDefaultInstance() : builder.messageWriter;
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
        return installationHome;
    }

    /**
     * Add named universe spec to the provisioning configuration
     *
     * @param name  universe name
     * @param universeSpec  universe spec
     * @throws ProvisioningException  in case of an error
     */
    public void addUniverse(String name, UniverseSpec universeSpec) throws ProvisioningException {
        final ProvisioningConfig config = getInstallationBuilder().addUniverse(name, universeSpec).build();
        try {
            ProvisioningXmlWriter.getInstance().write(config, PathsUtils.getProvisioningXml(installationHome));
        } catch (Exception e) {
            throw new ProvisioningException(Errors.writeFile(PathsUtils.getProvisioningXml(installationHome)), e);
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
            ProvisioningXmlWriter.getInstance().write(config, PathsUtils.getProvisioningXml(installationHome));
        } catch (Exception e) {
            throw new ProvisioningException(Errors.writeFile(PathsUtils.getProvisioningXml(installationHome)), e);
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
            provisioningConfig = ProvisioningXmlParser.parse(PathsUtils.getProvisioningXml(installationHome));
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
        return ProvisionedStateXmlParser.parse(PathsUtils.getProvisionedStateXml(installationHome));
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
        install(fpConfig, false);
    }

    public void install(FeaturePackConfig fpConfig, Map<String, String> options) throws ProvisioningException {
        install(fpConfig, false, options);
    }

    public void install(FeaturePackConfig fpConfig, boolean replaceInstalledVersion) throws ProvisioningException {
        install(fpConfig, replaceInstalledVersion, Collections.emptyMap());
    }

    public void install(FeaturePackConfig fpConfig, boolean replaceInstalledVersion, Map<String, String> options)
            throws ProvisioningException {
        ProvisioningConfig config = getProvisioningConfig();
        final boolean empty = config == null || !config.hasFeaturePackDeps();
        if(empty) {
            config = getInstallationBuilder().addFeaturePackDep(fpConfig).build();
        }
        try(ProvisioningLayout<FeaturePackRuntimeBuilder> layout = getLayoutFactory().newConfigLayout(config, ProvisioningRuntimeBuilder.FP_RT_FACTORY)) {
            if(!empty) {
                final UniverseSpec configuredUniverse = getConfiguredUniverse(fpConfig.getLocation());
                layout.install(configuredUniverse == null ? fpConfig : FeaturePackConfig.builder(fpConfig.getLocation().replaceUniverse(configuredUniverse)).init(fpConfig).build());
            }
            try (ProvisioningRuntime runtime = getRuntime(layout, options)) {
                doProvision(runtime);
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
        ProvisioningConfig config = getProvisioningConfig();
        final boolean empty = config == null || !config.hasFeaturePackDeps();
        if(empty) {
            throw new ProvisioningException(Errors.unknownFeaturePack(fpid));
        }
        try(ProvisioningLayout<FeaturePackRuntimeBuilder> layout = getLayoutFactory().newConfigLayout(config, ProvisioningRuntimeBuilder.FP_RT_FACTORY)) {
            layout.uninstall(resolveUniverseSpec(fpid.getLocation()).getFPID());
            try (ProvisioningRuntime runtime = getRuntime(layout, Collections.emptyMap())) {
                doProvision(runtime);
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
            doProvision(runtime);
        }
    }

    /**
     * (Re-)provisions the current installation to the desired specification.
     *
     * @param provisioningLayout  pre-built provisioning layout
     * @param options  feature-pack plug-ins options
     * @throws ProvisioningException  in case provisioning fails
     */
    public void provision(ProvisioningLayout<?> provisioningLayout, Map<String, String> options) throws ProvisioningException {
        try(ProvisioningRuntime runtime = getRuntime(provisioningLayout, options)) {
            doProvision(runtime);
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
            doProvision(runtime);
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
        final Path userProvisionedXml = PathsUtils.getProvisioningXml(installationHome);
        if(!Files.exists(userProvisionedXml)) {
            throw new ProvisioningException("Provisioned state record is missing for " + installationHome);
        }
        if(Files.isDirectory(exportPath)) {
            exportPath = exportPath.resolve(userProvisionedXml.getFileName());
        }
        IoUtils.copy(userProvisionedXml, exportPath);
    }

    public void exportConfigurationChanges(Path location,  FPID fpid, Map<String, String> options) throws ProvisioningException, IOException {
        ProvisioningConfig configuration = this.getProvisioningConfig();
        if (configuration == null) {
            final Path userProvisionedXml = PathsUtils.getProvisioningXml(installationHome);
            if (!Files.exists(userProvisionedXml)) {
                throw new ProvisioningException("Provisioned state record is missing for " + installationHome);
            }
            Path xmlTarget = location;
            if (Files.isDirectory(xmlTarget)) {
                xmlTarget = xmlTarget.resolve(userProvisionedXml.getFileName());
            }
            Files.copy(userProvisionedXml, xmlTarget, StandardCopyOption.REPLACE_EXISTING);
        }
        Path tempInstallationDir = IoUtils.createRandomTmpDir();
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
                            messageWriter.print(cause, message);
                        }

                        @Override
                        public void error(Throwable cause, CharSequence message) {
                            messageWriter.error(cause, message);
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
            try (ProvisioningRuntime runtime = ProvisioningRuntimeBuilder.newInstance(messageWriter)
                    .initLayout(getLayoutFactory(), configuration)
                    .setEncoding(encoding)
                    .setInstallDir(tempInstallationDir)
                    .addOptions(options)
                    .setOperation(fpid != null ? "diff-to-feature-pack" : "diff")
                    .build()) {
                if(fpid != null) {
                    ProvisioningRuntime.exportToFeaturePack(runtime, fpid, location, installationHome);
                } else {
                    ProvisioningRuntime.diff(runtime, location, installationHome);
                    runtime.getDiff().toXML(location, installationHome);
                }
            } catch (XMLStreamException | IOException e) {
                messageWriter.error(e, e.getMessage());
            }
        } finally {
            IoUtils.recursiveDelete(tempInstallationDir);
        }
    }

    /**
     * @deprecated
     *
     * @param fpGav  Feature-pack Maven artifact GAV
     * @param options  plugin options
     * @throws ProvisioningException  in case upgrade fails
     * @throws IOException  in case upgrade fails
     */
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
                            messageWriter.print(cause, message);
                        }

                        @Override
                        public void error(Throwable cause, CharSequence message) {
                            messageWriter.error(cause, message);
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
                            messageWriter.print(cause, message);
                        }

                        @Override
                        public void error(Throwable cause, CharSequence message) {
                            messageWriter.error(cause, message);
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
        try (ProvisioningRuntime runtime = ProvisioningRuntimeBuilder.newInstance(messageWriter)
                    .initLayout(getLayoutFactory(), configuration)
                    .setEncoding(encoding)
                    .setInstallDir(tempInstallationDir)
                    .addOptions(options)
                    .setOperation("upgrade")
                    .build()) {
                // install the software
                Files.createDirectories(tempInstallationDir.resolve("model_diff"));
                ProvisioningRuntime.diff(runtime, tempInstallationDir.resolve("model_diff"), installationHome);
                runtime.setInstallDir(stagedDir);
                ProvisioningRuntime.upgrade(runtime, installationHome);
            }
        finally {
            IoUtils.recursiveDelete(tempInstallationDir);
            IoUtils.recursiveDelete(stagedDir);
        }
    }

    public ProvisioningRuntime getRuntime(ProvisioningConfig provisioningConfig, Map<String, String> options)
            throws ProvisioningException {
        final ProvisioningRuntimeBuilder builder = ProvisioningRuntimeBuilder.newInstance(messageWriter)
                .initLayout(getLayoutFactory(), provisioningConfig)
                .setEncoding(encoding)
                .setInstallDir(installationHome)
                .addOptions(options);
        return builder.build();
    }

    public ProvisioningRuntime getRuntime(ProvisioningLayout<?> provisioningLayout, Map<String, String> options)
            throws ProvisioningException {
        return doGetRuntime(provisioningLayout.transform(ProvisioningRuntimeBuilder.FP_RT_FACTORY), options);
    }

    private ProvisioningRuntime doGetRuntime(ProvisioningLayout<FeaturePackRuntimeBuilder> rtLayout, Map<String, String> options) throws ProvisioningException {
        final ProvisioningRuntimeBuilder builder = ProvisioningRuntimeBuilder.newInstance(messageWriter)
                .initRtLayout(rtLayout)
                .setEncoding(encoding)
                .setInstallDir(installationHome)
                .addOptions(options);
        return builder.build();
    }

    private ProvisioningConfig.Builder getInstallationBuilder() throws ProvisioningException {
        return ProvisioningConfig.builder(getProvisioningConfig());
    }

    private void doProvision(ProvisioningRuntime runtime) throws ProvisioningException {
        checkInstallationDir(installationHome);

        if(runtime == null || !runtime.getProvisioningConfig().hasFeaturePackDeps()) {
            emptyHomeDir();
            this.provisioningConfig = null;
            return;
        }
        // install the software
        try {
        ProvisioningRuntime.install(runtime);
        } finally {
            this.provisioningConfig = null;
        }
    }

    private void emptyHomeDir() throws ProvisioningException {
        if(!Files.exists(installationHome)) {
            return;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(installationHome)) {
            for (Path p : stream) {
                IoUtils.recursiveDelete(p);
            }
        } catch (IOException e) {
            throw new ProvisioningException(Errors.readDirectory(installationHome));
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
}
