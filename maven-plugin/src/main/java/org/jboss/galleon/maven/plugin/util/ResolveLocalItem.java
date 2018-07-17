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

/**
 * Represents the information of each item inside of resolve-locals configuration param for
 * provisioning plugin.
 */
public class ResolveLocalItem {
    private Path path;
    private ResolveLocalArtifactItem artifact;
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
     * @parameter
     * @param file
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

    /**
     * Artifact string using groupId:artifactId:version format
     *
     * @parameter
     * @param artifact
     *
     * @throws IllegalStateException if Path or install-in-universe have been already initialized
     */
    public void setArtifact(ResolveLocalArtifactItem artifact) {
        assertArtifact();
        this.artifact = artifact;
        this.installInUniverse = Boolean.FALSE;
    }

    public ResolveLocalArtifactItem getArtifact() {
        return artifact;
    }

    /**
     * Boolean value for install-in-universe parameter configuration
     *
     * @parameter
     * @param installInUniverse
     */
    public void setInstallInUniverse(Boolean installInUniverse) {
        assertInstallInUniverse();
        this.installInUniverse = installInUniverse;
    }

    public Boolean getInstallInUniverse() {
        return installInUniverse;
    }

    private void assertArtifact() {
        if (this.path != null || installInUniverse != null) {
            error = "feature-pack artifact cannot be used: feature-pack Path or install-in-universe have already been initialized";
            throw new IllegalStateException(error);
        }
    }

    private void assertPath() {
        if (this.artifact != null) {
            error = "feature-pack Path cannot be used: feature-pack artifact has already been initialized";
            throw new IllegalStateException(error);
        }
    }

    private void assertInstallInUniverse() {
        if (this.artifact != null) {
            error = "feature-pack install-in-universe cannot be used: feature-pack artifact has already been initialized";
            throw new IllegalStateException(error);
        }
    }

    public String getError() {
        return error;
    }
}
