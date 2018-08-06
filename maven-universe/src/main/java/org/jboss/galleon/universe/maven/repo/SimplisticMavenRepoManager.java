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

package org.jboss.galleon.universe.maven.repo;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.ArtifactException;
import org.jboss.galleon.ArtifactRepositoryManager;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.model.Gaec;
import org.jboss.galleon.model.GaecRange;
import org.jboss.galleon.model.Gaecv;
import org.jboss.galleon.model.Gaecvp;
import org.jboss.galleon.universe.maven.MavenUniverseException;
import org.jboss.galleon.universe.maven.MavenErrors;
import org.jboss.galleon.universe.maven.MavenLatestVersionNotAvailableException;
import org.jboss.galleon.util.IoUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class SimplisticMavenRepoManager implements ArtifactRepositoryManager, MavenRepoManager {

    public static final String REPOSITORY_ID = MavenRepoManager.REPOSITORY_ID;
    public static final String SIMPLISTIC_MAVEN_REPO_HOME = "simplistic.maven.repo.home";

    public static SimplisticMavenRepoManager getInstance() {
        final String prop = System.getProperty(SIMPLISTIC_MAVEN_REPO_HOME);
        return getInstance(prop == null ? Paths.get(System.getProperty("user.home")).resolve(".m2") : Paths.get(prop));
    }

    public static SimplisticMavenRepoManager getInstance(Path repoHome) {
        return new SimplisticMavenRepoManager(repoHome, null);
    }

    public static SimplisticMavenRepoManager getInstance(Path repoHome, MavenRepoManager fallback) {
        return new SimplisticMavenRepoManager(repoHome, fallback);
    }

    private static final MavenArtifactVersionRangeParser versionRangeParser = new MavenArtifactVersionRangeParser();

    private final Path repoHome;
    private final MavenRepoManager fallback;

    private SimplisticMavenRepoManager(Path repoHome, MavenRepoManager fallback) {
        this.repoHome = repoHome;
        this.fallback = fallback;
    }

    @Override
    public String getRepositoryId() {
        return REPOSITORY_ID;
    }

//    @Override
//    public Path resolve(ArtifactCoords coords) throws ArtifactException {
//        final MavenArtifact artifact = toMavenArtifact(coords);
//        try {
//            resolve(artifact);
//        } catch (MavenUniverseException e) {
//            throw new ArtifactException("Failed to resolve " + coords, e);
//        }
//        return artifact.getPath();
//    }

    @Override
    public void deploy(ArtifactCoords coords, Path artifact) throws ArtifactException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getHighestVersion(ArtifactCoords coords, String range) throws ArtifactException {
        final GaecRange r = new GaecRange(new Gaec(coords.getGroupId(), coords.getArtifactId(), coords.getExtension(),coords.getClassifier()), range);
        try {
            return getLatestFinalVersion(r);
        } catch (MavenUniverseException e) {
            throw new ArtifactException("Failed to resolve the latest version for " + coords, e);
        }
    }

    @Override
    public Gaecvp resolve(Gaecv artifact) throws ProvisioningException {
        try {
            final Path path = getArtifactPath(artifact);
            if (!Files.exists(path)) {
                throw new MavenUniverseException(pathDoesNotExist(artifact, path));
            }
            return new Gaecvp(artifact, path);
        } catch (MavenUniverseException e) {
            if (fallback == null) {
                throw e;
            }
        }
        return fallback.resolve(artifact);
    }

    @Override
    public Gaecvp resolveLatestVersion(GaecRange artifact, String lowestQualifier) throws MavenUniverseException {
        Path path = null;
        try {
            path = resolveLatestVersionDir(artifact, lowestQualifier);
            final Gaecv gaecv = new Gaecv(artifact.getGaec(), path.getFileName().toString());
            path = path.resolve(gaecv.getArtifactFileName());
            if (!Files.exists(path)) {
                throw new MavenUniverseException(pathDoesNotExist(artifact, path));
            }
            return new Gaecvp(gaecv, path);
        } catch (MavenUniverseException e) {
            if (fallback == null) {
                throw e;
            }
        }
        return fallback.resolveLatestVersion(artifact, lowestQualifier);
    }

    private String pathDoesNotExist(Object artifact, Path path) throws MavenUniverseException {
        return "Failed to resolve " + artifact + ": " + path + " does not exist";
    }

    @Override
    public String getLatestVersion(GaecRange artifact, String lowestQualifier) throws MavenUniverseException {
        return resolveLatestVersionDir(artifact, lowestQualifier).getFileName().toString();
    }

    @Override
    public Gaecvp install(Gaecv artifact, Path path) throws MavenUniverseException {
        final Path targetPath = getArtifactPath(artifact);
        try {
            IoUtils.copy(path, targetPath);
        } catch (IOException e) {
            throw new MavenUniverseException("Failed to install " + artifact, e);
        }
        return new Gaecvp(artifact, targetPath);
    }

    private Path resolveLatestVersionDir(GaecRange artifact, String lowestQualifier) throws MavenUniverseException {
        final Gaec gaec = artifact.getGaec();
        Path artifactDir = repoHome;
        final String[] groupParts = gaec.getGroupId().split("\\.");
        for (String part : groupParts) {
            artifactDir = artifactDir.resolve(part);
        }
        artifactDir = artifactDir.resolve(gaec.getArtifactId());
        if(!Files.exists(artifactDir)) {
            throw MavenErrors.artifactNotFound(artifact, repoHome);
        }
        final MavenArtifactVersionRange range = versionRangeParser.parseRange(artifact.getVersionRange());
        if(lowestQualifier == null) {
            lowestQualifier = "";
        }
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(artifactDir)) {

            final Iterable<String> versions = new Iterable<String>() {
                @Override
                public Iterator<String> iterator() {
                    return new Iterator<String>() {
                        final Iterator<Path> i = stream.iterator();
                        Path nextPath = toNext(range);

                        @Override
                        public boolean hasNext() {
                            return nextPath != null;
                        }

                        @Override
                        public String next() {
                            if(nextPath != null) {
                                final String s = nextPath.getFileName().toString();
                                nextPath = toNext(range);
                                return s;
                            }
                            throw new NoSuchElementException();
                        }

                        private Path toNext(final MavenArtifactVersionRange range) {
                            while(i.hasNext()) {
                                final Path path = i.next();
                                final MavenArtifactVersion next = new MavenArtifactVersion(path.getFileName().toString());
                                if(range.includesVersion(next)) {
                                    return path;
                                }
                            }
                            return null;
                        }
                    };
                }
            };

            final MavenArtifactVersion latest = MavenArtifactVersion.getLatest(versions, lowestQualifier);
            if(latest == null) {
                throw new MavenLatestVersionNotAvailableException(MavenErrors.failedToResolveLatestVersion(artifact.toString()));
            }
            return artifactDir.resolve(latest.toString());
        } catch(MavenUniverseException e) {
            throw e;
        } catch (Exception e) {
            throw new MavenUniverseException(MavenErrors.failedToResolveLatestVersion(artifact.toString()), e);
        }
    }

    private Path getArtifactPath(Gaecv artifact) throws MavenUniverseException {
        final Gaec gaec = artifact.getGaec();
        Path p = repoHome;
        final String[] groupParts = gaec.getGroupId().split("\\.");
        for (String part : groupParts) {
            p = p.resolve(part);
        }
        final String artifactFileName = artifact.getArtifactFileName();
        return p.resolve(gaec.getArtifactId()).resolve(artifact.getVersion()).resolve(artifactFileName);
    }

//    @Override
//    public boolean isResolved(MavenArtifact artifact) throws MavenUniverseException {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }

//    @Override
//    public boolean isLatestVersionResolved(MavenArtifact artifact, String lowestQualifier) throws MavenUniverseException {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }

    @Override
    public String getLatestVersion(GaecRange artifact) throws MavenUniverseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
