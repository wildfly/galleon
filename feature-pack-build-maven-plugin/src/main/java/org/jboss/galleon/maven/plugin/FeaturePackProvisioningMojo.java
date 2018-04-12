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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.inject.Inject;
import javax.xml.stream.XMLStreamException;

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
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.deployment.DeploymentException;
import org.eclipse.aether.installation.InstallRequest;
import org.eclipse.aether.installation.InstallationException;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.ArtifactException;
import org.jboss.galleon.ArtifactRepositoryManager;
import org.jboss.galleon.Constants;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.maven.plugin.util.LoggerMessageWriter;
import org.jboss.galleon.xml.ProvisioningXmlParser;

/**
 *
 * @author Alexey Loubyansky
 */
@Mojo(name = "build", requiresDependencyResolution = ResolutionScope.RUNTIME, defaultPhase = LifecyclePhase.COMPILE)
public class FeaturePackProvisioningMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Component
    private RepositorySystem repoSystem;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private RepositorySystemSession repoSession;

    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true)
    private List<RemoteRepository> remoteRepos;

    /** The encoding to use when reading descriptor files */
    @Parameter(defaultValue = "${project.build.sourceEncoding}", required = true, property = "pm.encoding")
    private String encoding;

    @Inject
    private LoggerMessageWriter messageWriter;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        final String provXmlArg = repoSession.getSystemProperties().get(Constants.PROVISIONING_XML);
        if(provXmlArg == null) {
            throw new MojoExecutionException(FpMavenErrors.propertyMissing(Constants.PROVISIONING_XML));
        }
        final Path provXml = Paths.get(provXmlArg);
        if(!Files.exists(provXml)) {
            throw new MojoExecutionException(Errors.pathDoesNotExist(provXml));
        }

        final String installDirArg = repoSession.getSystemProperties().get(Constants.PM_INSTALL_DIR);
        if(installDirArg == null) {
            throw new MojoExecutionException(FpMavenErrors.propertyMissing(Constants.PM_INSTALL_DIR));
        }
        final Path installDir = Paths.get(installDirArg);

        ProvisioningConfig provisioningConfig;
        try(Reader r = Files.newBufferedReader(provXml, Charset.forName(encoding))) {
            provisioningConfig = ProvisioningXmlParser.getInstance().parse(r);
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException(Errors.pathDoesNotExist(provXml), e);
        } catch (XMLStreamException e) {
            throw new MojoExecutionException(Errors.parseXml(provXml), e);
        } catch (IOException e) {
            throw new MojoExecutionException(Errors.openFile(provXml), e);
        }

        try {
            ProvisioningManager.builder().setEncoding(encoding).setInstallationHome(installDir)
                    .setArtifactResolver(new ArtifactRepositoryManager() {
                        @Override
                        public Path resolve(ArtifactCoords coords) throws org.jboss.galleon.ArtifactException {
                            final ArtifactResult result;
                            try {
                                result = repoSystem.resolveArtifact(repoSession, getArtifactRequest(coords));
                            } catch (ArtifactResolutionException e) {
                                throw new org.jboss.galleon.ArtifactException(FpMavenErrors.artifactResolution(coords), e);
                            }
                            if(!result.isResolved()) {
                                throw new org.jboss.galleon.ArtifactException(FpMavenErrors.artifactResolution(coords));
                            }
                            if(result.isMissing()) {
                                throw new org.jboss.galleon.ArtifactException(FpMavenErrors.artifactMissing(coords));
                            }
                            return Paths.get(result.getArtifact().getFile().toURI());
                        }

                        @Override
                        public void install(ArtifactCoords coords, Path file) throws org.jboss.galleon.ArtifactException {
                            final InstallRequest request = new InstallRequest();
                            Artifact artifact = new DefaultArtifact(coords.getGroupId(), coords.getArtifactId(), coords.getClassifier(),
                                    coords.getExtension(), coords.getVersion());
                            artifact.setFile(file.toFile());
                            try {
                                repoSystem.install(repoSession, request);
                            } catch (InstallationException ex) {
                                throw new org.jboss.galleon.ArtifactException(ex.getMessage(), ex);
                            }
                        }

                        @Override
                        public void deploy(ArtifactCoords coords, Path file) throws org.jboss.galleon.ArtifactException {
                            final DeployRequest request = new DeployRequest();
                            Artifact artifact = new DefaultArtifact(coords.getGroupId(), coords.getArtifactId(), coords.getClassifier(),
                                    coords.getExtension(), coords.getVersion());
                            artifact.setFile(file.toFile());
                            try {
                                repoSystem.deploy(repoSession, request);
                            } catch (DeploymentException ex) {
                                throw new org.jboss.galleon.ArtifactException(ex.getMessage(), ex);
                            }
                        }

                        @Override
                        public String getHighestVersion(ArtifactCoords coords, String range) throws ArtifactException {
                            throw new UnsupportedOperationException("Not supported operation.");
                        }
                    })
                    .setMessageWriter(messageWriter)
                    .build().provision(provisioningConfig);
        } catch (ProvisioningException e) {
            throw new MojoExecutionException("Failed to provision the installation", e);
        }

        //collectDependencies(artifact);
        //resolveDependencies(artifact);
        //versionRequest(artifact);
        //artifactRequest(new DefaultArtifact("org.wildfly.core", "wildfly-cli", "jar", "3.0.0.Alpha3-SNAPSHOT"));
        //artifactRequest(new DefaultArtifact("org.wildfly.core", "wildfly-cli", "jar", "LATEST"));
        //artifactRequest(new DefaultArtifact("org.wildfly.feature-pack", "wildfly", "zip", "10.1.0.Final-SNAPSHOT"));
    }
/*
    private static void printDeps(DependencyNode dep) {
        printDeps(dep, 0);
    }

    private static void printDeps(DependencyNode dep, int level) {
        final StringBuilder buf = new StringBuilder();
        for(int i = 0; i < level; ++i) {
            buf.append("  ");
        }
        buf.append(dep.getArtifact().getGroupId())
            .append(':')
            .append(dep.getArtifact().getArtifactId())
            .append(':')
            .append(dep.getArtifact().getVersion());
        System.out.println(buf.toString());
        for(DependencyNode child : dep.getChildren()) {
            printDeps(child, level + 1);
        }
    }

    private void collectDependencies(final Artifact artifact) throws MojoExecutionException {
        CollectResult cRes = null;
        try {
            cRes = repoSystem.collectDependencies(repoSession, new CollectRequest(new Dependency(artifact, null), remoteRepos));
        } catch (DependencyCollectionException e) {
            throw new MojoExecutionException("Failed to collect", e);
        }
        printDeps(cRes.getRoot());
    }

    private void resolveDependencies(final Artifact artifact) throws MojoExecutionException {
        DependencyRequest dReq = new DependencyRequest().setCollectRequest(new CollectRequest(new Dependency(artifact, null), remoteRepos));
        DependencyResult dRes;
        try {
            dRes = repoSystem.resolveDependencies(repoSession, dReq);
        } catch (DependencyResolutionException e) {
            throw new MojoExecutionException("Failed to resolve dependency", e);
        }

        System.out.println("   root " + dRes.getRoot());
        System.out.println("deps " + dRes.getArtifactResults());
        for(ArtifactResult aRes : dRes.getArtifactResults()) {
            System.out.println("  - " + aRes.getArtifact());
        }
    }

    private void versionRequest(final Artifact artifact) throws MojoExecutionException {
        VersionRequest vReq = new VersionRequest()
            .setArtifact(artifact)
            .setRepositories(remoteRepos);

        VersionResult vRes;
        try {
            vRes = repoSystem.resolveVersion(repoSession, vReq);
        } catch (VersionResolutionException e) {
            throw new MojoExecutionException("Failed to resolve version", e);
        }

        System.out.println("  version=" + vRes.getVersion());
    }

    private void artifactRequest(final Artifact artifact) {
        ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(artifact);
        request.setRepositories(remoteRepos);
        final ArtifactResult result;
        try {
            result = repoSystem.resolveArtifact(repoSession, request);
        } catch ( ArtifactException e ) {
            throw new RuntimeException("failed to resolve artifact "+artifact, e);
        }
        System.out.println(artifact.toString() + " " + result.getArtifact().getFile().getAbsolutePath());

        final File targetFile = new File(project.getBasedir(), result.getArtifact().getFile().getName());
        if(targetFile.exists()) {
            targetFile.delete();
        }
        FileOutputStream fos = null;
        FileInputStream fis = null;
        try {
            fos = new FileOutputStream(targetFile);
            fis = new FileInputStream(result.getArtifact().getFile());
            IOUtil.copy(fis, fos);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            IOUtil.close(fos);
            IOUtil.close(fis);
        }
    }
*/
    private ArtifactRequest getArtifactRequest(ArtifactCoords coords) {
        final ArtifactRequest req = new ArtifactRequest();
        req.setArtifact(new DefaultArtifact(coords.getGroupId(), coords.getArtifactId(), coords.getClassifier(), coords.getExtension(), coords.getVersion()));
        req.setRepositories(remoteRepos);
        return req;
    }
}
