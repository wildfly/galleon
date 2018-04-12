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
package org.jboss.galleon.maven.plugin;

import java.io.File;
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

/**
 * Maven plugin to install a feature pack.
 * @author Emmanuel Hugonnet (c) 2017 Red Hat, inc.
 */
@Mojo(name = "provision", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, defaultPhase = LifecyclePhase.PROCESS_TEST_RESOURCES)
public class FeaturePackInstallMojo extends AbstractMojo {

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

    @Parameter(required = true)
    private File outputDirectory;

    @Parameter(required = false)
    private File modelConfiguration;

    @Parameter(required = false, defaultValue = "false")
    private Boolean inheritPackages;

    @Parameter(required = false, defaultValue = "false")
    private Boolean inheritConfigs;

    @Parameter(required = true)
    private ArtifactItem featurePack;

    @Parameter(required = false)
    private List<ConfigurationId> configs;

    @Parameter(required = false)
    private List<String> excludedPackages;

    @Parameter(required = false)
    private List<String> includedPackages;

    @Parameter(required = false)
    private Map<String, String> options;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        resetProperties();
        FeaturePackInstaller installer = new FeaturePackInstaller(
                repoSession.getLocalRepository().getBasedir().toPath(),
                outputDirectory.toPath(),
                modelConfiguration == null ? null : modelConfiguration.toPath().toAbsolutePath(),
                configs,
                featurePack.getArtifactCoords().toGav().toString(),
                inheritConfigs,
                inheritPackages,
                includedPackages,
                excludedPackages,
                options);
        installer.install();
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
