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
package org.jboss.galleon.maven.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.maven.plugin.util.ConfigurationId;
import org.jboss.galleon.maven.plugin.util.FeaturePack;
import org.jboss.galleon.repomanager.FeaturePackRepositoryManager;
import org.jboss.galleon.xml.ConfigXmlParser;

/**
 * Maven plugin to provision a state.
 *
 * @author Emmanuel Hugonnet (c) 2017 Red Hat, inc.
 * @author Alexey Loubyansky (c) 2017 Red Hat, inc.
 */
@Mojo(name = "provision", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, defaultPhase = LifecyclePhase.PROCESS_TEST_RESOURCES)
public class ProvisionStateMojo extends AbstractMojo {

    // These WildFly specific props should be cleaned up
    private static final String MAVEN_REPO_LOCAL = "maven.repo.local";

    private static final String SYSPROP_KEY_JBOSS_SERVER_BASE_DIR = "jboss.server.base.dir";
    private static final String SYSPROP_KEY_JBOSS_SERVER_CONFIG_DIR = "jboss.server.config.dir";
    private static final String SYSPROP_KEY_JBOSS_SERVER_DEPLOY_DIR = "jboss.server.deploy.dir";
    private static final String SYSPROP_KEY_JBOSS_SERVER_TEMP_DIR = "jboss.server.temp.dir";
    private static final String SYSPROP_KEY_JBOSS_SERVER_LOG_DIR = "jboss.server.log.dir";
    private static final String SYSPROP_KEY_JBOSS_SERVER_DATA_DIR = "jboss.server.data.dir";

    private static final String SYSPROP_KEY_JBOSS_DOMAIN_BASE_DIR = "jboss.domain.base.dir";
    private static final String SYSPROP_KEY_JBOSS_DOMAIN_CONFIG_DIR = "jboss.domain.config.dir";
    private static final String SYSPROP_KEY_JBOSS_DOMAIN_DEPLOYMENT_DIR = "jboss.domain.deployment.dir";
    private static final String SYSPROP_KEY_JBOSS_DOMAIN_TEMP_DIR = "jboss.domain.temp.dir";
    private static final String SYSPROP_KEY_JBOSS_DOMAIN_LOG_DIR = "jboss.domain.log.dir";
    private static final String SYSPROP_KEY_JBOSS_DOMAIN_DATA_DIR = "jboss.domain.data.dir";

    @Component
    protected RepositorySystem repoSystem;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    protected RepositorySystemSession repoSession;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    protected MavenSession session;

    @Parameter(alias = "install-dir", required = true)
    private File installDir;

    @Parameter(alias = "custom-config", required = false)
    private File customConfig;

    @Parameter(alias = "plugin-options", required = false)
    private Map<String, String> pluginOptions = Collections.emptyMap();

    @Parameter(alias = "feature-packs", required = true)
    private List<FeaturePack> featurePacks = Collections.emptyList();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if(featurePacks.isEmpty()) {
            throw new MojoExecutionException("No feature-packs to install.");
        }

        final String originalMavenRepoLocal = System.getProperty(MAVEN_REPO_LOCAL);
        System.setProperty(MAVEN_REPO_LOCAL, session.getSettings().getLocalRepository());
        try {
            System.setProperty("org.wildfly.logging.skipLogManagerCheck", "true");
            doProvision();
        } catch (ProvisioningException e) {
            throw new MojoExecutionException("Failed to provision the state", e);
        } finally {
            System.clearProperty("org.wildfly.logging.skipLogManagerCheck");
            resetProperties();
            if(originalMavenRepoLocal == null) {
                System.clearProperty(MAVEN_REPO_LOCAL);
            } else {
                System.setProperty(MAVEN_REPO_LOCAL, originalMavenRepoLocal);
            }
        }
    }

    private void doProvision() throws MojoExecutionException, ProvisioningException {
        final ProvisioningConfig.Builder state = ProvisioningConfig.builder();
        for(FeaturePack fp : featurePacks) {
            if(fp.getGroupId() == null) {
                throw new MojoExecutionException("Feature-pack groupId is missing");
            }
            if(fp.getArtifactId() == null) {
                throw new MojoExecutionException("Feature-pack artifactId is missing");
            }
            final FeaturePackConfig.Builder fpConfig = FeaturePackConfig.builder(ArtifactCoords.newGav(fp.getGroupId(),
                    fp.getArtifactId(), fp.getVersion()))
                    .setInheritConfigs(fp.isInheritConfigs())
                    .setInheritPackages(fp.isInheritPackages());

            if(!fp.getExcludedConfigs().isEmpty()) {
                for(ConfigurationId configId : fp.getExcludedConfigs()) {
                    if(configId.isModelOnly()) {
                        fpConfig.excludeConfigModel(configId.getId().getModel());
                    } else {
                        fpConfig.excludeDefaultConfig(configId.getId());
                    }
                }
            }
            if(!fp.getIncludedConfigs().isEmpty()) {
                for(ConfigurationId configId : fp.getIncludedConfigs()) {
                    if(configId.isModelOnly()) {
                        fpConfig.includeConfigModel(configId.getId().getModel());
                    } else {
                        fpConfig.includeDefaultConfig(configId.getId());
                    }
                }
            }

            if (!fp.getIncludedPackages().isEmpty()) {
                for (String includedPackage : fp.getIncludedPackages()) {
                    fpConfig.includePackage(includedPackage);
                }
            }
            if (!fp.getExcludedPackages().isEmpty()) {
                for (String excludedPackage : fp.getExcludedPackages()) {
                    fpConfig.excludePackage(excludedPackage);
                }
            }

            state.addFeaturePackDep(fpConfig.build());
        }

        if (customConfig != null && customConfig.exists()) {
            try (BufferedReader reader = Files.newBufferedReader(customConfig.toPath())) {
                state.addConfig(ConfigXmlParser.getInstance().parse(reader));
            } catch (XMLStreamException | IOException ex) {
                throw new IllegalArgumentException("Couldn't load the customization configuration " + customConfig, ex);
            }
        }

        final ProvisioningManager pm = ProvisioningManager.builder()
                .setArtifactResolver(
                        FeaturePackRepositoryManager.newInstance(repoSession.getLocalRepository().getBasedir().toPath()))
                .setInstallationHome(installDir.toPath()).build();

        pm.provision(state.build(), pluginOptions);
    }

    private static void resetProperties() {
        System.clearProperty(SYSPROP_KEY_JBOSS_SERVER_BASE_DIR);
        System.clearProperty(SYSPROP_KEY_JBOSS_SERVER_CONFIG_DIR);
        System.clearProperty(SYSPROP_KEY_JBOSS_SERVER_DATA_DIR);
        System.clearProperty(SYSPROP_KEY_JBOSS_SERVER_DEPLOY_DIR);
        System.clearProperty(SYSPROP_KEY_JBOSS_SERVER_TEMP_DIR);
        System.clearProperty(SYSPROP_KEY_JBOSS_SERVER_LOG_DIR);
        System.clearProperty(SYSPROP_KEY_JBOSS_DOMAIN_BASE_DIR);
        System.clearProperty(SYSPROP_KEY_JBOSS_DOMAIN_CONFIG_DIR);
        System.clearProperty(SYSPROP_KEY_JBOSS_DOMAIN_DATA_DIR);
        System.clearProperty(SYSPROP_KEY_JBOSS_DOMAIN_DEPLOYMENT_DIR);
        System.clearProperty(SYSPROP_KEY_JBOSS_DOMAIN_TEMP_DIR);
        System.clearProperty(SYSPROP_KEY_JBOSS_DOMAIN_LOG_DIR);
    }
}
