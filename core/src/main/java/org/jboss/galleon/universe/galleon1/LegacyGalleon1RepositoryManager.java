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
package org.jboss.galleon.universe.galleon1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.repo.RepositoryArtifactResolver;
import org.jboss.galleon.util.ZipUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class LegacyGalleon1RepositoryManager implements RepositoryArtifactResolver {

    private static final String REPOSITORY_ID = RepositoryArtifactResolver.ID_PREFIX + "maven";

    private static final Pattern COORDS_PATTERN = Pattern.compile("([^: ]+):([^: ]+)(:([^: ]*)(:([^: ]+))?)?:([^: ]+)");

    public static LegacyGalleon1RepositoryManager newInstance(Path repoHome) {
        return new LegacyGalleon1RepositoryManager(repoHome);
    }

    final Path repoHome;

    private LegacyGalleon1RepositoryManager(Path repoHome) {
        this.repoHome = repoHome;
    }

    public void install(String coords, Path artifact) throws ProvisioningException {
        try {
            final Path path = getArtifactPath(coords);
            Files.createDirectories(path.getParent());
            if(Files.isDirectory(artifact)) {
                 ZipUtils.zip(artifact, path);
            }else {
                Files.copy(artifact, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new ProvisioningException("Failed to install artifact " + coords + " to " + artifact, ex);
        }
    }

    @Override
    public String getRepositoryId() {
        return REPOSITORY_ID;
    }

    @Override
    public Path resolve(String location) throws ProvisioningException {
        final Path path = getArtifactPath(location);
        if(!Files.exists(path)) {
            throw new ProvisioningException("Artifact " + location + " not found in the repository at " + path);
        }
        return path;
    }

    private Path getArtifactPath(String location) {
        final Matcher m = COORDS_PATTERN.matcher(location);
        if (!m.matches()) {
            throw new IllegalArgumentException("Bad artifact coordinates " + location
                    + ", expected format is <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>");
        }
        final String groupId = m.group(1);
        final String artifactId = m.group(2);
        final String extension = get(m.group(4), "zip");
        final String classifier = get(m.group(6), "");
        final String version = m.group(7);

        Path p = repoHome;
        final String[] groupParts = groupId.split("\\.");
        for (String part : groupParts) {
            p = p.resolve(part);
        }
        p = p.resolve(artifactId);
        p = p.resolve(version);
        final StringBuilder fileName = new StringBuilder();
        fileName.append(artifactId).append('-').append(version);
        if(!classifier.isEmpty()) {
            fileName.append('-').append(classifier);
        }
        fileName.append('.').append(extension);
        return p.resolve(fileName.toString());
    }

    private static String get(String value, String defaultValue) {
        return (value == null || value.length() <= 0) ? defaultValue : value;
    }
}
