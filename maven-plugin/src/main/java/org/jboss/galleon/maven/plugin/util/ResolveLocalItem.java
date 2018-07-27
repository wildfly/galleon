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
import java.nio.file.Path;

import org.apache.maven.shared.artifact.ArtifactCoordinate;
import org.jboss.galleon.model.Gaec;
import org.jboss.galleon.model.Gaecv;

/**
 * Represents the information of each item inside of resolve-locals configuration param for
 * provisioning plugin.
 */
public class ResolveLocalItem implements ArtifactCoordinate {
    private Path path;

    private String groupId;
    private String artifactId;
    private String version;
    private String extension = "zip";
    private String classifier = "";

    private Boolean installInUniverse;
    /**
     * Even throwing an exception if there is a wrong configuration for this element,
     * the error is silently ignored and the build continues. We use this text field here
     * to flag that the configuration has an error.
     */
    private String error;

    /**
     * File pointing to a feature-pack
     *
     * @param file  feature-pack file
     *
     * @throws IllegalStateException if artifact has been already initialized
     */
    public void setPath(File file) {
        assertPath();
        this.path = file.toPath().normalize();
        this.installInUniverse = installInUniverse == null ? Boolean.TRUE : installInUniverse;
    }

    public Path getNormalizedPath(){
        return path;
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        assertNotPath();
        this.groupId = groupId;
    }

    @Override
    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        assertNotPath();
        this.artifactId = artifactId;
    }

    @Override
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        assertNotPath();
        this.version = version;
    }

    @Override
    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        assertNotPath();
        this.extension = extension;
    }

    @Override
    public String getClassifier() {
        return classifier;
    }

    public void setClassifier(String classifier) {
        assertNotPath();
        this.classifier = classifier;
    }

    /**
     * Boolean value for install-in-universe parameter configuration
     *
     * @param installInUniverse  whether to install the feature-pack in universe repository
     */
    public void setInstallInUniverse(Boolean installInUniverse) {
        assertInstallInUniverse();
        this.installInUniverse = installInUniverse;
    }

    public Boolean getInstallInUniverse() {
        return installInUniverse;
    }

    private void assertNotPath() {
        if (this.path != null || installInUniverse != null) {
            error = "feature-pack artifact cannot be used: feature-pack Path or install-in-universe have already been initialized";
            throw new IllegalStateException(error);
        }
    }

    public boolean hasArtifactCoords() {
        return groupId != null || artifactId != null || version != null;
    }

    private void assertPath() {
        if (hasArtifactCoords()) {
            error = "feature-pack Path cannot be used: feature-pack artifact has already been initialized";
            throw new IllegalStateException(error);
        }
    }

    private void assertInstallInUniverse() {
        if (hasArtifactCoords()) {
            error = "feature-pack install-in-universe cannot be used: feature-pack artifact has already been initialized";
            throw new IllegalStateException(error);
        }
    }

    public String getError() {
        return error;
    }

    public Gaec toGaec() {
        return new Gaec(groupId, artifactId, extension, classifier);
    }

    public Gaecv toGaecv() {
        return new Gaecv(toGaec(), version);
    }
}
