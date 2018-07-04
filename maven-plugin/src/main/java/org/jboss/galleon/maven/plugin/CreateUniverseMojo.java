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

import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.jboss.galleon.universe.maven.MavenArtifact;
import org.jboss.galleon.universe.maven.MavenUniverseException;
import org.jboss.galleon.universe.maven.MavenUniverseInstaller;
import org.jboss.galleon.universe.maven.repo.SimplisticMavenRepoManager;

/**
 * Creates a new Maven universe artifact.
 *
 * @author Alexey Loubyansky
 */
@Mojo(name = "create-universe")
public class CreateUniverseMojo extends AbstractMojo {

    public static class ProducerSpec {

        /**
         * Producer name
         */
        @Parameter(required = true)
        String name;

        /**
         * Producer artifact groupId
         */
        @Parameter(required = true)
        String groupId;

        /**
         * Producer artifact artifactId
         */
        @Parameter(required = true)
        String artifactId;

        /**
         * Producer artifact version range
         */
        @Parameter(required = true)
        String versionRange;
    }

    @Component
    protected RepositorySystem repoSystem;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    protected RepositorySystemSession repoSession;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    protected MavenSession session;

    @Component
    private MavenProjectHelper projectHelper;

    /**
     * Universe groupId
     */
    @Parameter(required = true, defaultValue="${project.groupId}")
    private String groupId;

    /**
     * Universe artifactId
     */
    @Parameter(required = true, defaultValue="${project.artifactId}")
    private String artifactId;

    /**
     * Universe version
     */
    @Parameter(required = true, defaultValue="${project.version}")
    private String version;

    /**
     * Feature-pack producers that are members of the universe
     */
    @Parameter(required = true)
    private List<ProducerSpec> producers = Collections.emptyList();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        final MavenArtifact universeArtifact = new MavenArtifact()
                .setGroupId(groupId)
                .setArtifactId(artifactId)
                .setVersion(version);
        final MavenUniverseInstaller installer = new MavenUniverseInstaller(
                SimplisticMavenRepoManager.getInstance(
                        Paths.get(project.getBuild().getDirectory()).resolve("local-repo"),
                        SimplisticMavenRepoManager.getInstance(repoSession.getLocalRepository().getBasedir().toPath())),
                universeArtifact);

        final Set<String> names = new HashSet<>(producers.size());
        for(ProducerSpec producer : producers) {
            if(!names.add(producer.name)) {
                throw new MojoExecutionException("Duplicate producer " + producer.name);
            }
            try {
                installer.addProducer(producer.name, producer.groupId, producer.artifactId, producer.versionRange);
            } catch (MavenUniverseException e) {
                throw new MojoExecutionException("Failed to add producer " + producer.name, e);
            }
        }
        try {
            installer.install();
        } catch (MavenUniverseException e) {
            throw new MojoExecutionException("Failed to create universe", e);
        }
        projectHelper.attachArtifact(project, "jar", universeArtifact.getPath().toFile());
    }
}
