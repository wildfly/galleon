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

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
import org.jboss.galleon.maven.plugin.util.ArtifactItem;
import org.jboss.galleon.maven.plugin.util.ConfigurationId;
import org.jboss.galleon.maven.plugin.util.FeaturePackInstaller;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;

/**
 * This maven plugin  installs a feature-pack into an empty directory or a
 * directory that already contains an installation, in which case the product
 * the feature-pack represents will be integrated into an existing installation.
 *
 * @author Emmanuel Hugonnet (c) 2017 Red Hat, inc.
 * @author Alexey Loubyansky (c) 2017 Red Hat, inc.
 */
@Mojo(name = "install-feature-pack", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, defaultPhase = LifecyclePhase.PROCESS_TEST_RESOURCES)
public class FeaturePackInstallMojo extends AbstractMojo {

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

    /**
    * The target installation directory.
    */
    @Parameter(alias = "install-dir", required = true)
    private File installDir;

    /**
     * Path to a file containing `config` that should be installed.
    */
    @Parameter(alias = "custom-config", required = false)
    private File customConfig;

    /**
     * Whether to install the default package set.
     */
    @Parameter(alias = "inherit-packages", required = false, defaultValue = "true")
    private boolean inheritPackages;

    /**
     * Whether to inherit the default feature-pack configs.
     */
    @Parameter(alias = "inherit-configs", required = false, defaultValue = "true")
    private boolean inheritConfigs;

    /**
     * Legacy Galleon 1.x feature-pack artifact coordinates as (groupId - artifactId - version)
     * if not specified the feature-pack must be a transitive dependency of a feature-pack with
     * the version specified.
     *
     * NOTE: either this parameter or 'location' must be configured.
     */
    @Parameter(alias = "feature-pack", required = false)
    private ArtifactItem featurePack;

    /**
     * Galleon2 feature-pack location
     *
     * NOTE: either this parameter or 'feature-pack' must be configured.
     */
    @Parameter(required = false)
    private String location;

    /**
     * Default feature-pack configs that should be included.
     */
    @Parameter(alias = "included-configs", required = false)
    private List<ConfigurationId> includedConfigs = Collections.emptyList();;

    /**
     * Explicitly excluded packages from the installation.
    */
    @Parameter(alias = "excluded-packages", required = false)
    private List<String> excludedPackages = Collections.emptyList();;

    /**
     * Explicitly included packages to install.
    */
    @Parameter(alias = "included-packages", required = false)
    private List<String> includedPackages = Collections.emptyList();

    /**
     * Arbitrary plugin options recognized by the plugins attached to the feature-pack being installed.
    */
    @Parameter(alias = "plugin-options", required = false)
    private Map<String, String> pluginOptions = Collections.emptyMap();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        resetProperties();
        final FeaturePackLocation fpl;
        if(featurePack != null) {
            fpl = LegacyGalleon1Universe.toFpl(featurePack.getArtifactCoords().toGav());
        } else if(location != null) {
            fpl = FeaturePackLocation.fromString(location);
        } else {
            throw new MojoExecutionException("Either 'location' or 'feature-pack' must be configured");
        }

        final FeaturePackInstaller fpInstaller = FeaturePackInstaller.newInstance(
                repoSession.getLocalRepository().getBasedir().toPath(),
                installDir.toPath(),
                fpl)
                .setInheritConfigs(inheritConfigs)
                .includeConfigs(includedConfigs)
                .setInheritPackages(inheritPackages)
                .includePackages(includedPackages)
                .excludePackages(excludedPackages)
                .setPluginOptions(pluginOptions);
        if(customConfig != null) {
            fpInstaller.setCustomConfig(customConfig.toPath().toAbsolutePath());
        }

        final String originalMavenRepoLocal = System.getProperty(MAVEN_REPO_LOCAL);
        System.setProperty(MAVEN_REPO_LOCAL, session.getSettings().getLocalRepository());
        try {
            fpInstaller.install();
        } finally {
            if(originalMavenRepoLocal == null) {
                System.clearProperty(MAVEN_REPO_LOCAL);
            } else {
                System.setProperty(MAVEN_REPO_LOCAL, originalMavenRepoLocal);
            }
        }
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
