/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.universe.maven.repo;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import org.jboss.galleon.universe.maven.MavenArtifact;
import org.jboss.galleon.universe.maven.MavenErrors;
import org.jboss.galleon.universe.maven.MavenLatestVersionNotAvailableException;
import org.jboss.galleon.universe.maven.MavenUniverseException;

/**
 *
 * @author Alexey Loubyansky
 */
public class LocalArtifactVersionRangeResolver {

    private static final MavenArtifactVersionRangeParser versionRangeParser = new MavenArtifactVersionRangeParser();

    protected final Path repoHome;

    public LocalArtifactVersionRangeResolver(Path localRepo) {
        this.repoHome = localRepo;
    }

    public void resolveLatestVersion(MavenArtifact artifact, String lowestQualifier) throws MavenUniverseException {
        resolveLatestVersion(artifact, lowestQualifier, null, null);
    }

    public void resolveLatestVersion(MavenArtifact artifact, String lowestQualifier, Pattern includeVersion, Pattern excludeVersion) throws MavenUniverseException {
        if(artifact.isResolved()) {
            throw new MavenUniverseException("Artifact is already resolved");
        }
        Path path = resolveLatestVersionDir(artifact, lowestQualifier, includeVersion, excludeVersion);
        artifact.setVersion(path.getFileName().toString());
        path = path.resolve(artifact.getArtifactFileName());
        if (!Files.exists(path)) {
            throw new MavenUniverseException(pathDoesNotExist(artifact, path));
        }
        artifact.setPath(path);
    }

    public String getLatestVersion(MavenArtifact artifact, String lowestQualifier) throws MavenUniverseException {
        return resolveLatestVersionDir(artifact, lowestQualifier, null, null).getFileName().toString();
    }

    public String getLatestVersion(MavenArtifact artifact, String lowestQualifier, Pattern includeVersion, Pattern excludeVersion) throws MavenUniverseException {
        return resolveLatestVersionDir(artifact, lowestQualifier, includeVersion, excludeVersion).getFileName().toString();
    }

    protected String pathDoesNotExist(MavenArtifact artifact, Path path) throws MavenUniverseException {
        return "Failed to resolve " + artifact.getCoordsAsString() + ": " + path + " does not exist";
    }

    private Path resolveLatestVersionDir(MavenArtifact artifact, String lowestQualifier, Pattern includeVersion, Pattern excludeVersion) throws MavenUniverseException {
        if(artifact.getGroupId() == null) {
            MavenErrors.missingGroupId();
        }
        if(artifact.getArtifactId() == null) {
            MavenErrors.missingArtifactId();
        }
        if(artifact.getVersionRange() == null) {
            throw new MavenUniverseException("Version range is missing for " + artifact.getCoordsAsString());
        }
        Path artifactDir = repoHome;
        final String[] groupParts = artifact.getGroupId().split("\\.");
        for (String part : groupParts) {
            artifactDir = artifactDir.resolve(part);
        }
        artifactDir = artifactDir.resolve(artifact.getArtifactId());
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
                                if(!Files.isDirectory(path)) {
                                    continue;
                                }
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

            final MavenArtifactVersion latest = MavenArtifactVersion.getLatest(versions, lowestQualifier, includeVersion, excludeVersion);
            if(latest == null) {
                throw new MavenLatestVersionNotAvailableException(MavenErrors.failedToResolveLatestVersion(artifact.getCoordsAsString()));
            }
            return artifactDir.resolve(latest.toString());
        } catch(MavenUniverseException e) {
            throw e;
        } catch (Exception e) {
            throw new MavenUniverseException(MavenErrors.failedToResolveLatestVersion(artifact.getCoordsAsString()), e);
        }
    }
}
