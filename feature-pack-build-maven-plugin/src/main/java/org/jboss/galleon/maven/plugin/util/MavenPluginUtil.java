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

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Singleton;
import org.apache.maven.project.MavenProject;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.installation.InstallRequest;
import org.eclipse.aether.util.artifact.SubArtifact;
import org.jboss.galleon.util.IoUtils;
import org.jboss.galleon.util.ZipUtils;

/**
 *
 * @author Alexey Loubyansky
 */
@Singleton
public class MavenPluginUtil extends AbstractLogEnabled {

    public InstallRequest getInstallLayoutRequest(final Path layoutDir, final MavenProject project) throws IOException {
        final File pomFile = project.getFile();
        final Logger logger = getLogger();
        final InstallRequest installReq = new InstallRequest();
        try (DirectoryStream<Path> wdStream = Files.newDirectoryStream(layoutDir, entry -> Files.isDirectory(entry))) {
            for (Path groupDir : wdStream) {
                final String groupId = groupDir.getFileName().toString();
                try (DirectoryStream<Path> groupStream = Files.newDirectoryStream(groupDir)) {
                    for (Path artifactDir : groupStream) {
                        final String artifactId = artifactDir.getFileName().toString();
                        try (DirectoryStream<Path> artifactStream = Files.newDirectoryStream(artifactDir)) {
                            for (Path versionDir : artifactStream) {
                                if(logger.isDebugEnabled()) {
                                    logger.debug("Preparing feature-pack " + versionDir.toAbsolutePath());
                                }
                                final Path zippedFP = layoutDir.resolve(
                                        groupId + '_' + artifactId + '_' + versionDir.getFileName().toString() + ".zip");
                                if(Files.exists(zippedFP)) {
                                    IoUtils.recursiveDelete(zippedFP);
                                }
                                ZipUtils.zip(versionDir, zippedFP);
                                final Artifact artifact = new DefaultArtifact(
                                        groupDir.getFileName().toString(),
                                        artifactDir.getFileName().toString(), null,
                                        "zip", versionDir.getFileName().toString(), null, zippedFP.toFile());
                                Path target = Paths.get(project.getBuild().getDirectory()).resolve(project.getBuild().getFinalName() + ".zip");
                                IoUtils.copy(zippedFP, target);
                                installReq.addArtifact(artifact);
                                if (pomFile != null && Files.exists(pomFile.toPath())) {
                                    Artifact pomArtifact = new SubArtifact(artifact, "", "pom");
                                    pomArtifact = pomArtifact.setFile(pomFile);
                                    installReq.addArtifact(pomArtifact);
                                }
                            }
                        }
                    }
                }
            }
        }
        return installReq;
    }
}
