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
import org.jboss.galleon.runtime.ProvisioningRuntime;
import org.jboss.galleon.runtime.ProvisioningRuntimeBuilder;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.UniverseResolver;
import org.jboss.galleon.universe.UniverseResolverBuilder;
import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.util.IoUtils;
import org.jboss.galleon.util.PathsUtils;
import org.jboss.galleon.xml.ProvisionedStateXmlParser;
import org.jboss.galleon.xml.ProvisioningXmlParser;

/**
 *
 * @author Alexey Loubyansky
 */
public class ProvisioningManager {

    public static class Builder extends UniverseResolverBuilder<Builder> {

        private String encoding = "UTF-8";
        private Path installationHome;
        private MessageWriter messageWriter;

        private Builder() {
        }

        /**
         * @deprecated
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

        public Builder setMessageWriter(MessageWriter messageWriter) {
            this.messageWriter = messageWriter;
            return this;
        }

        public ProvisioningManager build() throws ProvisioningException {
            return new ProvisioningManager(this);
        }

        protected UniverseResolver getUniverseResolver() throws ProvisioningException {
            return buildUniverseResolver();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final String encoding;
    private final Path installationHome;
    private final UniverseResolver universeResolver;
    private final MessageWriter messageWriter;

    private ProvisioningConfig provisioningConfig;

    private ProvisioningManager(Builder builder) throws ProvisioningException {
        this.encoding = builder.encoding;
        this.installationHome = builder.installationHome;
        this.universeResolver = builder.getUniverseResolver();
        this.messageWriter = builder.messageWriter == null ? DefaultMessageWriter.getDefaultInstance() : builder.messageWriter;
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
     * @param fps  feature-pack source
     * @throws ProvisioningException  in case the installation fails
     */
    public void install(FeaturePackLocation fps) throws ProvisioningException {
        install(FeaturePackConfig.forLocation(fps));
    }

    /**
     * Installs the specified feature-pack taking into account provided plug-in options.
     *
     * @param  fps feature-pack source
     * @param options  plug-in options
     * @throws ProvisioningException  in case the installation fails
     */
    public void install(FeaturePackLocation fps, Map<String, String> options) throws ProvisioningException {
        install(FeaturePackConfig.forLocation(fps), options);
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
        final ProvisioningConfig provisionedConfig = this.getProvisioningConfig();
        if(provisionedConfig == null) {
            provision(ProvisioningConfig.builder()
                    .addFeaturePackDep(fpConfig)
                    .build(), options);
            return;
        }

        final ProvisionedFeaturePack installedFp = getProvisionedState().getFeaturePack(fpConfig.getLocation().getChannel());
        // if it's installed neither explicitly nor implicitly as a dependency
        if(installedFp == null) {
            provision(ProvisioningConfig.builder(provisionedConfig).addFeaturePackDep(fpConfig).build(), options);
            return;
        }

        if(!replaceInstalledVersion) {
            if (installedFp.getFPID().equals(fpConfig.getLocation().getFPID())) {
                throw new ProvisioningException(Errors.featurePackAlreadyInstalled(fpConfig.getLocation().getFPID()));
            } else {
                throw new ProvisioningException(Errors.featurePackVersionConflict(fpConfig.getLocation().getFPID(), installedFp.getFPID()));
            }
        }

        // if it's installed explicitly, replace the explicit config
        if(provisionedConfig.hasFeaturePackDep(fpConfig.getLocation().getChannel())) {
            final FeaturePackConfig installedFpConfig = provisionedConfig.getFeaturePackDep(fpConfig.getLocation().getChannel());
            final String origin = provisionedConfig.originOf(fpConfig.getLocation().getChannel());
            provision(ProvisioningConfig.builder(provisionedConfig)
                    .removeFeaturePackDep(installedFpConfig.getLocation())
                    .addFeaturePackDep(origin, fpConfig)
                    .build(), options);
            return;
        }

        provision(ProvisioningConfig.builder(provisionedConfig).addFeaturePackDep(fpConfig).build(), options);
    }

    /**
     * Uninstalls the specified feature-pack.
     *
     * @param fpid  feature-pack ID
     * @throws ProvisioningException  in case the uninstallation fails
     */
    public void uninstall(FeaturePackLocation.FPID fpid) throws ProvisioningException {
        final ProvisioningConfig provisionedConfig = getProvisioningConfig();
        if(provisionedConfig == null) {
            throw new ProvisioningException(Errors.unknownFeaturePack(fpid));
        }
        if(!provisioningConfig.hasFeaturePackDep(fpid.getChannel())) {
            if(getProvisionedState().hasFeaturePack(fpid.getChannel())) {
                throw new ProvisioningException(Errors.unsatisfiedFeaturePackDep(fpid.getChannel()));
            }
            throw new ProvisioningException(Errors.unknownFeaturePack(fpid));
        }
        doProvision(provisionedConfig, fpid, Collections.emptyMap());
    }

    /**
     * (Re-)provisions the current installation to the desired specification.
     *
     * @param provisioningConfig  the desired installation specification
     * @throws ProvisioningException  in case the re-provisioning fails
     */
    public void provision(ProvisioningConfig provisioningConfig) throws ProvisioningException {
        doProvision(provisioningConfig, null, Collections.emptyMap());
    }

    /**
     * (Re-)provisions the current installation to the desired specification.
     *
     * @param provisioningConfig  the desired installation specification
     * @param options  feature-pack plug-ins options
     * @throws ProvisioningException  in case the re-provisioning fails
     */
    public void provision(ProvisioningConfig provisioningConfig, Map<String, String> options) throws ProvisioningException {
        doProvision(provisioningConfig, null, options);
    }

    private void doProvision(ProvisioningConfig provisioningConfig, FeaturePackLocation.FPID uninstallFpid, Map<String, String> options) throws ProvisioningException {
        checkInstallationDir(installationHome);

        if(!provisioningConfig.hasFeaturePackDeps()) {
            emptyHomeDir();
            this.provisioningConfig = null;
            return;
        }

        try(ProvisioningRuntime runtime = getRuntime(provisioningConfig, uninstallFpid, options)) {
            if(runtime == null) {
                return;
            }
            // install the software
            ProvisioningRuntime.install(runtime);
        } finally {
            this.provisioningConfig = null;
        }
    }

    private void emptyHomeDir() throws ProvisioningException {
        if(Files.exists(installationHome)) {
            try(DirectoryStream<Path> stream = Files.newDirectoryStream(installationHome)) {
                for(Path p : stream) {
                    IoUtils.recursiveDelete(p);
                }
            } catch (IOException e) {
                throw new ProvisioningException(Errors.readDirectory(installationHome));
            }
        }
    }

    public ProvisioningRuntime getRuntime(ProvisioningConfig provisioningConfig, FeaturePackLocation.FPID uninstallFpid, Map<String, String> options)
            throws ProvisioningException {
        final ProvisioningRuntimeBuilder builder = ProvisioningRuntimeBuilder.newInstance(messageWriter)
                .setUniverseResolver(universeResolver)
                .setConfig(provisioningConfig)
                .setEncoding(encoding)
                .setInstallDir(installationHome)
                .addOptions(options);
        if(uninstallFpid != null) {
            builder.uninstall(uninstallFpid);
        }
        return builder.build();
    }

    public static void checkInstallationDir(Path installationHome) throws ProvisioningException {
        if (Files.exists(installationHome)) {
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
        provision(ProvisioningXmlParser.parse(provisioningXml), options);
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
        try {
            ProvisioningManager reference = new ProvisioningManager(ProvisioningManager.builder()
                    .setUniverseFactoryLoader(universeResolver.getFactoryLoader())
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
                    }));
            reference.provision(configuration);
            try (ProvisioningRuntime runtime = ProvisioningRuntimeBuilder.newInstance(messageWriter)
                    .setUniverseResolver(universeResolver)
                    .setConfig(configuration)
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
     */
    public void upgrade(ArtifactCoords.Gav fpGav, Map<String, String> options) throws ProvisioningException, IOException {
        ProvisioningConfig configuration = this.getProvisioningConfig();
        Path tempInstallationDir = IoUtils.createRandomTmpDir();
        Path stagedDir = IoUtils.createRandomTmpDir();
        try {
            ProvisioningManager reference = new ProvisioningManager(ProvisioningManager.builder()
                    .setUniverseFactoryLoader(universeResolver.getFactoryLoader())
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
                    }));
            reference.provision(configuration);
            Files.createDirectories(stagedDir);
            reference = new ProvisioningManager(ProvisioningManager.builder()
                    .setUniverseFactoryLoader(universeResolver.getFactoryLoader())
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
                    }));
            reference.provision(ProvisioningConfig.builder().addFeaturePackDep(FeaturePackConfig.forLocation(LegacyGalleon1Universe.toFpl(fpGav))).build());
            try (ProvisioningRuntime runtime = ProvisioningRuntimeBuilder.newInstance(messageWriter)
                    .setUniverseResolver(universeResolver)
                    .setConfig(configuration)
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
        } finally {
            IoUtils.recursiveDelete(tempInstallationDir);
        }
    }
}
