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
import org.jboss.galleon.util.IoUtils;
import org.jboss.galleon.util.PathsUtils;
import org.jboss.galleon.xml.XmlParsers;

/**
 *
 * @author Alexey Loubyansky
 */
public class ProvisioningManager {

    public static class Builder {

        private String encoding = "UTF-8";
        private Path installationHome;
        private ArtifactRepositoryManager artifactResolver;
        private MessageWriter messageWriter;

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

        public Builder setArtifactResolver(ArtifactRepositoryManager artifactResolver) {
            this.artifactResolver = artifactResolver;
            return this;
        }

        public Builder setMessageWriter(MessageWriter messageWriter) {
            this.messageWriter = messageWriter;
            return this;
        }

        public ProvisioningManager build() {
            return new ProvisioningManager(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final String encoding;
    private final Path installationHome;
    private final ArtifactRepositoryManager artifactResolver;
    private final MessageWriter messageWriter;

    private ProvisioningConfig provisioningConfig;

    private ProvisioningManager(Builder builder) {
        this.encoding = builder.encoding;
        this.installationHome = builder.installationHome;
        this.artifactResolver = builder.artifactResolver;
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
            provisioningConfig = readProvisioningConfig(PathsUtils.getProvisioningXml(installationHome));
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
        final Path xml = PathsUtils.getProvisionedStateXml(installationHome);
        if (!Files.exists(xml)) {
            return null;
        }
        try (BufferedReader reader = Files.newBufferedReader(xml)) {
            final ProvisionedState.Builder builder = ProvisionedState.builder();
            XmlParsers.parse(reader, builder);
            return builder.build();
        } catch (IOException | XMLStreamException e) {
            throw new ProvisioningException(Errors.parseXml(xml), e);
        }
    }

    /**
     * Installs the specified feature-pack.
     *
     * @param fpGav  feature-pack GAV
     * @throws ProvisioningException  in case the installation fails
     */
    public void install(ArtifactCoords.Gav fpGav) throws ProvisioningException {
        install(FeaturePackConfig.forGav(fpGav));
    }

    public void install(ArtifactCoords.Gav fpGav, Map<String, String> options) throws ProvisioningException {
        install(FeaturePackConfig.forGav(fpGav), options);
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

    public void install(ArtifactCoords.Gav fpGav, boolean replaceInstalledVersion) throws ProvisioningException {
        install(FeaturePackConfig.forGav(fpGav), replaceInstalledVersion);
    }

    public void install(ArtifactCoords.Gav fpGav, boolean replaceInstalledVersion, Map<String, String> options) throws ProvisioningException {
        install(FeaturePackConfig.forGav(fpGav), replaceInstalledVersion, options);
    }

    public void install(FeaturePackConfig fpConfig, boolean replaceInstalledVersion) throws ProvisioningException {
        install(fpConfig, replaceInstalledVersion, Collections.emptyMap());
    }

    public void install(FeaturePackConfig fpConfig, boolean replaceInstalledVersion, Map<String, String> options)
            throws ProvisioningException {
        final ProvisioningConfig provisionedConfig = this.getProvisioningConfig();
        if(provisionedConfig == null) {
            provision(ProvisioningConfig.builder().addFeaturePackDep(fpConfig).build(), options);
            return;
        }

        final ProvisionedFeaturePack installedFp = getProvisionedState().getFeaturePack(fpConfig.getGav().toGa());
        // if it's installed neither explicitly nor implicitly as a dependency
        if(installedFp == null) {
            provision(ProvisioningConfig.builder(provisionedConfig).addFeaturePackDep(fpConfig).build(), options);
            return;
        }

        if(!replaceInstalledVersion) {
            if (installedFp.getGav().equals(fpConfig.getGav())) {
                throw new ProvisioningException(Errors.featurePackAlreadyInstalled(fpConfig.getGav()));
            } else {
                throw new ProvisioningException(Errors.featurePackVersionConflict(fpConfig.getGav(), installedFp.getGav()));
            }
        }

        // if it's installed explicitly, replace the explicit config
        if(provisionedConfig.hasFeaturePackDep(fpConfig.getGav().toGa())) {
            final FeaturePackConfig installedFpConfig = provisionedConfig.getFeaturePackDep(fpConfig.getGav().toGa());
            final String origin = provisionedConfig.originOf(fpConfig.getGav().toGa());
            provision(ProvisioningConfig.builder(provisionedConfig)
                    .removeFeaturePackDep(installedFpConfig.getGav())
                    .addFeaturePackDep(origin, fpConfig)
                    .build(), options);
            return;
        }

        provision(ProvisioningConfig.builder(provisionedConfig).addFeaturePackDep(fpConfig).build(), options);
    }

    /**
     * Uninstalls the specified feature-pack.
     *
     * @param gav  feature-pack GAV
     * @throws ProvisioningException  in case the uninstallation fails
     */
    public void uninstall(ArtifactCoords.Gav gav) throws ProvisioningException {
        final ProvisioningConfig provisionedConfig = getProvisioningConfig();
        if(provisionedConfig == null) {
            throw new ProvisioningException(Errors.unknownFeaturePack(gav));
        }
        if(!provisioningConfig.hasFeaturePackDep(gav.toGa())) {
            if(getProvisionedState().hasFeaturePack(gav.toGa())) {
                throw new ProvisioningException(Errors.unsatisfiedFeaturePackDep(gav));
            }
            throw new ProvisioningException(Errors.unknownFeaturePack(gav));
        }
        doProvision(provisionedConfig, gav.toGa(), Collections.emptyMap());
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

    private void doProvision(ProvisioningConfig provisioningConfig, ArtifactCoords.Ga uninstallGa, Map<String, String> options) throws ProvisioningException {
        checkInstallationDir(installationHome);

        if(!provisioningConfig.hasFeaturePackDeps()) {
            emptyHomeDir();
            this.provisioningConfig = null;
            return;
        }

        if(artifactResolver == null) {
            throw new ProvisioningException("Artifact resolver has not been provided.");
        }

        try(ProvisioningRuntime runtime = getRuntime(provisioningConfig, uninstallGa, options)) {
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

    public ProvisioningRuntime getRuntime(ProvisioningConfig provisioningConfig, ArtifactCoords.Ga uninstallGa, Map<String, String> options)
            throws ProvisioningException {
        final ProvisioningRuntimeBuilder builder = ProvisioningRuntimeBuilder.newInstance(messageWriter)
                .setArtifactResolver(artifactResolver)
                .setConfig(provisioningConfig)
                .setEncoding(encoding)
                .setInstallDir(installationHome)
                .addOptions(options);
        if(uninstallGa != null) {
            builder.uninstall(uninstallGa);
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
        provision(readProvisioningConfig(provisioningXml), options);
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

    public void exportConfigurationChanges(Path location, ArtifactCoords.Gav diffGav, Map<String, String> options) throws ProvisioningException, IOException {
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
                    .setArtifactResolver(artifactResolver)
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
                    .setArtifactResolver(artifactResolver)
                    .setConfig(configuration)
                    .setEncoding(encoding)
                    .setInstallDir(tempInstallationDir)
                    .addOptions(options)
                    .setOperation(diffGav != null ? "diff-to-feature-pack" : "diff")
                    .build()) {
                if(diffGav != null) {
                    ProvisioningRuntime.exportToFeaturePack(runtime, diffGav, location, installationHome);
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

    public void upgrade(ArtifactCoords.Gav fpGav, Map<String, String> options) throws ProvisioningException, IOException {
        ProvisioningConfig configuration = this.getProvisioningConfig();
        Path tempInstallationDir = IoUtils.createRandomTmpDir();
        Path stagedDir = IoUtils.createRandomTmpDir();
        try {
            ProvisioningManager reference = new ProvisioningManager(ProvisioningManager.builder()
                    .setArtifactResolver(artifactResolver)
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
                    .setArtifactResolver(artifactResolver)
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
            reference.provision(ProvisioningConfig.builder().addFeaturePackDep(FeaturePackConfig.forGav(fpGav)).build());
            try (ProvisioningRuntime runtime = ProvisioningRuntimeBuilder.newInstance(messageWriter)
                    .setArtifactResolver(artifactResolver)
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

    public ProvisioningConfig readProvisioningConfig(Path path) throws ProvisioningException {
        if (!Files.exists(path)) {
            return null;
        }
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            final ProvisioningConfig.Builder builder = ProvisioningConfig.builder();
            XmlParsers.parse(reader, builder);
            return builder.build();
        } catch (IOException | XMLStreamException e) {
            throw new ProvisioningException(Errors.parseXml(path), e);
        }
    }
}
