/*
 * Copyright 2016-2020 Red Hat, Inc. and/or its affiliates
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
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.artifact.ArtifactCoordinate;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.jboss.galleon.Constants;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.maven.plugin.util.Configuration;
import org.jboss.galleon.maven.plugin.util.ConfigurationId;
import org.jboss.galleon.maven.plugin.util.FeaturePack;
import org.jboss.galleon.maven.plugin.util.MavenArtifactRepositoryManager;
import org.jboss.galleon.maven.plugin.util.MvnMessageWriter;
import org.jboss.galleon.maven.plugin.util.ResolveLocalItem;
import org.jboss.galleon.repo.RepositoryArtifactResolver;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.maven.MavenArtifact;
import org.jboss.galleon.universe.maven.MavenUniverseException;
import org.jboss.galleon.universe.maven.repo.MavenRepoManager;
import org.jboss.galleon.util.IoUtils;
import org.jboss.galleon.xml.ConfigXmlParser;

/**
 * This maven plugin provisions an installation that consists of one or more feature-packs.
 * If the target installation directory already contains an installation, the existing
 * installation will be fully replaced with the newly provisioned one.<p>
 * In other words, the configuration provided for this goal fully describes the
 * state of the final installation.
 *
 * @author Emmanuel Hugonnet (c) 2017 Red Hat, inc.
 * @author Alexey Loubyansky (c) 2017 Red Hat, inc.
 */
@Mojo(name = "provision", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, defaultPhase = LifecyclePhase.PROCESS_TEST_RESOURCES)
public class ProvisionStateMojo extends AbstractMojo {

    // These WildFly specific props should be cleaned up
    private static final String MAVEN_REPO_LOCAL = "maven.repo.local";

    @Component
    protected RepositorySystem repoSystem;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    protected RepositorySystemSession repoSession;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    protected MavenSession session;

    @Parameter( defaultValue = "${project.remoteProjectRepositories}", readonly = true, required = true )
    private List<RemoteRepository> repositories;

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
    * Arbitrary plugin options.
    */
    @Parameter(alias = "plugin-options", required = false)
    private Map<String, String> pluginOptions = Collections.emptyMap();

    /**
    * A list of feature-pack configurations to install.
    */
    @Parameter(alias = "feature-packs", required = true)
    private List<FeaturePack> featurePacks = Collections.emptyList();

    /**
     * A list of custom configurations to install.
     */
    @Parameter(alias = "configurations", required = false)
    private List<Configuration> configs = Collections.emptyList();

    /**
     * Whether to use offline mode when the plugin resolves an artifact.
     * In offline mode the plugin will only use the local Maven repository for an artifact resolution.
     */
    @Parameter(alias = "offline", defaultValue = "false")
    private boolean offline;

    /**
     * Whether to log provisioning time at the end
     */
    @Parameter(alias = "log-time", defaultValue = "false")
    private boolean logTime;

    /**
     * Whether to record provisioned state in .galleon directory.
     */
    @Parameter(alias = "record-state", defaultValue = "true")
    private boolean recordState = true;

    /**
     * A list of artifacts and paths pointing to feature-pack archives that should be resolved locally without
     * involving the universe-based feature-pack resolver at provisioning time.
     */
    @Parameter(alias = "resolve-locals")
    private List<ResolveLocalItem> resolveLocals = Collections.emptyList();

    /**
     * Specifies whether the provisioning should be skipped.
     *
     * @since 4.2.6
     */
    @Parameter(defaultValue = "false", property = PropertyNames.SKIP)
    private boolean skip;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Skipping the provision goal.");
            return;
        }
        if(featurePacks.isEmpty()) {
            throw new MojoExecutionException("No feature-packs to install.");
        }

        final String originalMavenRepoLocal = System.getProperty(MAVEN_REPO_LOCAL);
        System.setProperty(MAVEN_REPO_LOCAL, session.getSettings().getLocalRepository());
        try {
            doProvision();
        } catch (ProvisioningException e) {
            throw new MojoExecutionException("Provisioning failed", e);
        } finally {
            if(originalMavenRepoLocal == null) {
                System.clearProperty(MAVEN_REPO_LOCAL);
            } else {
                System.setProperty(MAVEN_REPO_LOCAL, originalMavenRepoLocal);
            }
        }
    }

    private void doProvision() throws MojoExecutionException, ProvisioningException {
        final ProvisioningConfig.Builder state = ProvisioningConfig.builder();

        final RepositoryArtifactResolver artifactResolver = offline ? new MavenArtifactRepositoryManager(repoSystem, repoSession)
                : new MavenArtifactRepositoryManager(repoSystem, repoSession, repositories);

        final Path home = installDir.toPath();
        if(!recordState) {
            IoUtils.recursiveDelete(home);
        }
        try (ProvisioningManager pm = ProvisioningManager.builder().addArtifactResolver(artifactResolver)
                .setInstallationHome(home)
                .setMessageWriter(new MvnMessageWriter(getLog()))
                .setLogTime(logTime)
                .setRecordState(recordState)
                .build()) {

            for (FeaturePack fp : featurePacks) {

                if (fp.getLocation() == null && (fp.getGroupId() == null || fp.getArtifactId() == null)
                        && fp.getNormalizedPath() == null) {
                    throw new MojoExecutionException("Feature-pack location, Maven GAV or feature pack path is missing");
                }

                final FeaturePackLocation fpl;
                if (fp.getNormalizedPath() != null) {
                    fpl = pm.getLayoutFactory().addLocal(fp.getNormalizedPath(), false);
                } else if (fp.getGroupId() != null && fp.getArtifactId() != null) {
                    Path path = resolveMaven(fp, (MavenRepoManager) artifactResolver);
                    fpl = pm.getLayoutFactory().addLocal(path, false);
                } else {
                    fpl = FeaturePackLocation.fromString(fp.getLocation());
                }

                final FeaturePackConfig.Builder fpConfig = fp.isTransitive() ? FeaturePackConfig.transitiveBuilder(fpl)
                        : FeaturePackConfig.builder(fpl);
                if(fp.isInheritConfigs() != null) {
                    fpConfig.setInheritConfigs(fp.isInheritConfigs());
                }
                if(fp.isInheritPackages() != null) {
                    fpConfig.setInheritPackages(fp.isInheritPackages());
                }

                if (!fp.getExcludedConfigs().isEmpty()) {
                    for (ConfigurationId configId : fp.getExcludedConfigs()) {
                        if (configId.isModelOnly()) {
                            fpConfig.excludeConfigModel(configId.getId().getModel());
                        } else {
                            fpConfig.excludeDefaultConfig(configId.getId());
                        }
                    }
                }
                if (!fp.getIncludedConfigs().isEmpty()) {
                    for (ConfigurationId configId : fp.getIncludedConfigs()) {
                        if (configId.isModelOnly()) {
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

            boolean hasLayers = false;
            for (Configuration config : configs) {
                ConfigModel.Builder configBuilder = ConfigModel.
                        builder(config.getModel(), config.getName());
                for (String layer : config.getLayers()) {
                    hasLayers = true;
                    configBuilder.includeLayer(layer);
                }
                if (config.getExcludedLayers() != null) {
                    for (String layer : config.getExcludedLayers()) {
                        configBuilder.excludeLayer(layer);
                    }
                }
                state.addConfig(configBuilder.build());
            }

            if (hasLayers) {
                if (pluginOptions.isEmpty()) {
                    pluginOptions = Collections.
                            singletonMap(Constants.OPTIONAL_PACKAGES, Constants.PASSIVE_PLUS);
                } else if (!pluginOptions.containsKey(Constants.OPTIONAL_PACKAGES)) {
                    pluginOptions.put(Constants.OPTIONAL_PACKAGES, Constants.PASSIVE_PLUS);
                }
            }

            if (customConfig != null && customConfig.exists()) {
                try (BufferedReader reader = Files.newBufferedReader(customConfig.toPath())) {
                    state.addConfig(ConfigXmlParser.getInstance().parse(reader));
                } catch (XMLStreamException | IOException ex) {
                    throw new IllegalArgumentException("Couldn't load the customization configuration " + customConfig, ex);
                }
            }

            for (ResolveLocalItem localResolverItem : resolveLocals) {
                if (localResolverItem.getError() != null) {
                    throw new MojoExecutionException(localResolverItem.getError());
                }
            }

            for (ResolveLocalItem localResolverItem : resolveLocals) {
                if (localResolverItem.getNormalizedPath() != null) {
                    pm.getLayoutFactory().addLocal(localResolverItem.getNormalizedPath(),
                            localResolverItem.getInstallInUniverse());
                } else if (localResolverItem.hasArtifactCoords()) {
                    Path path = resolveMaven(localResolverItem, (MavenRepoManager) artifactResolver);
                    pm.getLayoutFactory().addLocal(path, false);
                } else {
                    throw new MojoExecutionException("resolve-local element appears to be neither path not maven artifact");
                }
            }

            pm.provision(state.build(), pluginOptions);
        }
    }

    private Path resolveMaven(ArtifactCoordinate coordinate, MavenRepoManager resolver) throws MavenUniverseException, MojoExecutionException {
        final MavenArtifact artifact = new MavenArtifact();
        artifact.setGroupId(coordinate.getGroupId());
        artifact.setArtifactId(coordinate.getArtifactId());
        String version = coordinate.getVersion();
        if(isEmptyOrNull(version)) {
            // first, we are looking for the artifact among the project deps
            // direct dependencies may override the managed versions
            for(Artifact a : project.getArtifacts()) {
                if(coordinate.getArtifactId().equals(a.getArtifactId())
                        && coordinate.getGroupId().equals(a.getGroupId())
                        && coordinate.getExtension().equals(a.getType())
                        && (coordinate.getClassifier() == null ? "" : coordinate.getClassifier())
                                .equals(a.getClassifier() == null ? "" : a.getClassifier())) {
                    version = a.getVersion();
                    break;
                }
            }
            if(isEmptyOrNull(version)) {
                // Now we are going to look for for among the managed dependencies
                for (Dependency d : project.getDependencyManagement().getDependencies()) {
                    if (coordinate.getArtifactId().equals(d.getArtifactId())
                            && coordinate.getGroupId().equals(d.getGroupId())
                            && coordinate.getExtension().equals(d.getType())
                            && (coordinate.getClassifier() == null ? "" : coordinate.getClassifier())
                                    .equals(d.getClassifier() == null ? "" : d.getClassifier())) {
                        version = d.getVersion();
                        break;
                    }
                }
                if (isEmptyOrNull(version)) {
                    throw new MojoExecutionException(coordinate.getGroupId() + ":" + coordinate.getArtifactId() + ":"
                            + (coordinate.getClassifier() == null ? "" : coordinate.getClassifier()) + ":"
                            + coordinate.getExtension()
                            + " was found among neither the project's dependencies nor the managed dependencies."
                            + " To proceed, please, add the desired version of the feature-pack to the provisioning configuration"
                            + " or the project dependencies, or the dependency management section of the Maven project");
                }
            }
        }
        artifact.setVersion(version);
        artifact.setExtension(coordinate.getExtension());
        artifact.setClassifier(coordinate.getClassifier());

        resolver.resolve(artifact);
        return artifact.getPath();
    }

    private boolean isEmptyOrNull(String version) {
        return version == null || version.isEmpty();
    }
}
