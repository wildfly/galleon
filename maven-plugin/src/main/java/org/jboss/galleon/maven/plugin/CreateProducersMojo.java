/*
 * Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
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

import java.nio.file.Paths;
import java.util.List;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.jboss.galleon.maven.plugin.util.MavenArtifactRepositoryManager;
import org.jboss.galleon.universe.maven.MavenArtifact;
import org.jboss.galleon.universe.maven.MavenProducers;
import org.jboss.galleon.universe.maven.MavenUniverseException;
import org.jboss.galleon.universe.maven.repo.SimplisticMavenRepoManager;

/**
 * Creates a new Maven artifact which combines multiple producers.
 *
 * @author Alexey Loubyansky
 */
@Mojo(name = "create-producers")
public class CreateProducersMojo extends AbstractMojo {

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

    @Component
    private MavenProjectHelper projectHelper;

    /**
     * Producer groupId
     */
    @Parameter(required = true, defaultValue="${project.groupId}")
    private String groupId;

    /**
     * Producer artifactId
     */
    @Parameter(required = true, defaultValue="${project.artifactId}")
    private String artifactId;

    /**
     * Producer version
     */
    @Parameter(required = true, defaultValue="${project.version}")
    private String version;

    /**
     * Producers
     */
    @Parameter(required = true)
    private List<ProducerDescription> producers;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final MavenArtifact artifact = new MavenArtifact().setGroupId(groupId).setArtifactId(artifactId).setVersion(version);
        final MavenProducers installer = MavenProducers.getInstance(
                SimplisticMavenRepoManager.getInstance(Paths.get(project.getBuild().getDirectory()).resolve("local-repo"),
                        new MavenArtifactRepositoryManager(repoSystem, repoSession, repositories)),
                artifact);
        for(ProducerDescription producer : producers) {
            installer.addProducer(producer);
        }
        try {
            installer.install();
        } catch (MavenUniverseException e) {
            throw new MojoExecutionException("Failed to create producers artifact", e);
        }
        projectHelper.attachArtifact(project, "jar", artifact.getPath().toFile());
    }
}
