/*
 * Copyright ${license.git.copyrightYears} Red Hat, Inc. and/or its affiliates
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
import org.jboss.galleon.xml.ConfigXmlParser;

/**
 *
 * @author Emmanuel Hugonnet (c) 2018 Red Hat, inc.
 */
public class FeaturePackInstaller {

    private final Path repoHome;
    private final Path installationDir;
    private final Path configuration;
    private final List<ConfigurationId> configs;
    private final String featurePackGav;
    private final boolean inheritPackages;
    private final boolean inheritConfigs;
    private final List<String> includedPackages;
    private final List<String> excludedPackages;
    private final Map<String, String> options;

    public FeaturePackInstaller(Path repoHome, Path installationDir, Path configuration,
            List<ConfigurationId> configs, String featurePackGav, boolean inheritConfigs, boolean inheritPackages,
            List<String> includedPackages, List<String> excludedPackages, Map<String, String> options) {
        this.repoHome = repoHome;
        this.installationDir = installationDir;
        this.configuration = configuration;
        this.inheritConfigs = inheritConfigs;
        this.inheritPackages = inheritPackages;
        this.featurePackGav = featurePackGav;
        if (configs == null) {
            this.configs = Collections.emptyList();
        } else {
            this.configs = configs;
        }
        if (includedPackages == null) {
            this.includedPackages = Collections.emptyList();
        } else {
            this.includedPackages = includedPackages;
        }
        if (excludedPackages == null) {
            this.excludedPackages = Collections.emptyList();
        } else {
            this.excludedPackages = excludedPackages;
        }
        if (options == null) {
            this.options = Collections.emptyMap();
        } else {
            this.options = options;
        }
    }

    public void install() {
        try {
            ProvisioningManager manager = getManager();
            System.setProperty("org.wildfly.logging.skipLogManagerCheck", "true");
            ConfigModel config = null;
            if (configuration != null && Files.exists(configuration)) {
                try (BufferedReader reader = Files.newBufferedReader(configuration)) {
                    config = ConfigXmlParser.getInstance().parse(reader);
                } catch (XMLStreamException | IOException ex) {
                    throw new IllegalArgumentException("Couldn't load the customization configuration " + configuration, ex);
                }
            }
            FeaturePackConfig.Builder fpConfigBuilder = FeaturePackConfig.builder(ArtifactCoords.newGav(featurePackGav))
                    .setInheritPackages(inheritPackages)
                    .setInheritConfigs(inheritConfigs);
            if(configs != null && ! configs.isEmpty()) {
                for(ConfigurationId configId : configs) {
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
            manager.install(fpConfigBuilder.build(), options);
        } catch (ProvisioningException ex) {
            throw new IllegalArgumentException("Couldn't install the feature pack " + featurePackGav, ex);
        } finally {
            System.clearProperty("org.wildfly.logging.skipLogManagerCheck");
        }
    }

    private ProvisioningManager getManager() {
        return ProvisioningManager.builder()
                .setArtifactResolver(FeaturePackRepositoryManager.newInstance(repoHome))
                .setInstallationHome(installationDir)
                .build();
    }
}
