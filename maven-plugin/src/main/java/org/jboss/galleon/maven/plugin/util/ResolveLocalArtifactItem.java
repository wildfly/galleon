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

import org.apache.maven.shared.artifact.ArtifactCoordinate;
import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.universe.maven.MavenArtifact;

/**
 * Represents an artifact inside of resolve-local section in resolve-locals parameter
 * configuration of provisioning plugin.
 * <p>
 * Each artifact is a feature-pack based on maven coordinates that will be
 * added as local feature pack.
 */
public class ResolveLocalArtifactItem implements ArtifactCoordinate {
    private ArtifactCoords artifactCoords;

    /**
     * Default setter for maven parameter.
     * Artifact in string format
     *
     * @parameter
     */
    public void set(String artifact) {
        artifactCoords = ArtifactCoords.fromString(artifact, MavenArtifact.EXT_ZIP);
    }

    @Override
    public String getGroupId() {
        return artifactCoords != null ? artifactCoords.getGroupId() : null;
    }

    @Override
    public String getArtifactId() {
        return artifactCoords != null ? artifactCoords.getArtifactId() : null;
    }

    @Override
    public String getVersion() {
        return artifactCoords != null ? artifactCoords.getVersion() : null;
    }

    @Override
    public String getExtension() {
        return artifactCoords != null ? artifactCoords.getExtension() : null;
    }

    @Override
    public String getClassifier() {
        return artifactCoords != null ? artifactCoords.getClassifier() : null;
    }
}
