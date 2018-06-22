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
package org.jboss.galleon.maven.plugin.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.repomanager.FeaturePackRepositoryManager;
import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.xml.ConfigXmlParser;

/**
 *
 * @author Emmanuel Hugonnet (c) 2018 Red Hat, inc.
 * @author Alexey Loubyansky (c) 2018 Red Hat, inc.
 */
public class FeaturePackInstaller {

    public static FeaturePackInstaller newInstance(Path repoHome, Path installationDir, ArtifactCoords.Gav fpGav) {
        return new FeaturePackInstaller(repoHome, installationDir, fpGav);
    }

    private final Path repoHome;
    private final Path installationDir;
    private final ArtifactCoords.Gav fpGav;
    private boolean inheritConfigs = true;
    private List<ConfigurationId> includedConfigs = Collections.emptyList();
    private Path customConfig;
    private boolean inheritPackages = true;
    private List<String> includedPackages = Collections.emptyList();
    private List<String> excludedPackages = Collections.emptyList();
    private Map<String, String> pluginOptions = Collections.emptyMap();

    private FeaturePackInstaller(Path repoHome, Path installationDir, ArtifactCoords.Gav fpGav) {
        this.repoHome = repoHome;
        this.installationDir = installationDir;
        this.fpGav = fpGav;
    }

    public FeaturePackInstaller setInheritConfigs(boolean inheritConfigs) {
        this.inheritConfigs = inheritConfigs;
        return this;
    }

    public FeaturePackInstaller includeConfig(ConfigurationId configId) {
        includedConfigs = CollectionUtils.add(includedConfigs, configId);
        return this;
    }

    public FeaturePackInstaller includeConfigs(List<ConfigurationId> configIds) {
        includedConfigs = CollectionUtils.addAll(includedConfigs, configIds);
        return this;
    }

    public FeaturePackInstaller setCustomConfig(Path customConfig) {
        this.customConfig = customConfig;
        return this;
    }

    public FeaturePackInstaller setInheritPackages(boolean inheritPackages) {
        this.inheritPackages = inheritPackages;
        return this;
    }

    public FeaturePackInstaller includePackage(String packageName) {
        includedPackages = CollectionUtils.add(includedPackages, packageName);
        return this;
    }

    public FeaturePackInstaller includePackages(List<String> packageNames) {
        includedPackages = CollectionUtils.addAll(includedPackages, packageNames);
        return this;
    }

    public FeaturePackInstaller excludePackage(String packageName) {
        excludedPackages = CollectionUtils.add(excludedPackages, packageName);
        return this;
    }

    public FeaturePackInstaller excludePackages(List<String> packageNames) {
        excludedPackages = CollectionUtils.addAll(excludedPackages, packageNames);
        return this;
    }

    public FeaturePackInstaller setPluginOption(String option) {
        return setPluginOption(option, null);
    }

    public FeaturePackInstaller setPluginOption(String option, String value) {
        pluginOptions = CollectionUtils.put(pluginOptions, option, value);
        return this;
    }

    public FeaturePackInstaller setPluginOptions(Map<String, String> options) {
        this.pluginOptions = CollectionUtils.putAll(pluginOptions, options);
        return this;
    }

    public void install() {
        try {
            ProvisioningManager manager = getManager();
            System.setProperty("org.wildfly.logging.skipLogManagerCheck", "true");
            ConfigModel config = null;
            if (customConfig != null && Files.exists(customConfig)) {
                try (BufferedReader reader = Files.newBufferedReader(customConfig)) {
                    config = ConfigXmlParser.getInstance().parse(reader);
                } catch (XMLStreamException | IOException ex) {
                    throw new IllegalArgumentException("Couldn't load the customization configuration " + customConfig, ex);
                }
            }
            FeaturePackConfig.Builder fpConfigBuilder = FeaturePackConfig.builder(LegacyGalleon1Universe.newFPID(fpGav.getGroupId(), fpGav.getArtifactId(), fpGav.getVersion()).getLocation())
                    .setInheritPackages(inheritPackages)
                    .setInheritConfigs(inheritConfigs);
            if(includedConfigs != null && ! includedConfigs.isEmpty()) {
                for(ConfigurationId configId : includedConfigs) {
                    if(configId.isModelOnly()) {
                        fpConfigBuilder.includeConfigModel(configId.getId().getModel());
                    } else {
                        fpConfigBuilder.includeDefaultConfig(configId.getId());
                    }
                }
            }
            if (config != null) {
                fpConfigBuilder.addConfig(config);
            }
            if (includedPackages != null && !includedPackages.isEmpty()) {
                for (String includedPackage : includedPackages) {
                    fpConfigBuilder.includePackage(includedPackage);
                }
            }
            if (excludedPackages != null && !excludedPackages.isEmpty()) {
                for (String excludedPackage : excludedPackages) {
                    fpConfigBuilder.excludePackage(excludedPackage);
                }
            }
            manager.install(fpConfigBuilder.build(), pluginOptions);
        } catch (ProvisioningException ex) {
            throw new IllegalArgumentException("Couldn't install the feature pack " + fpGav, ex);
        } finally {
            System.clearProperty("org.wildfly.logging.skipLogManagerCheck");
        }
    }

    private ProvisioningManager getManager() throws ProvisioningException {
        return ProvisioningManager.builder()
                .addArtifactResolver(FeaturePackRepositoryManager.newInstance(repoHome))
                .setInstallationHome(installationDir)
                .build();
    }
}
