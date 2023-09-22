/*
 * Copyright 2016-2023 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.api;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

import org.jboss.galleon.util.StringUtils;

/**
 *
 * @author Alexey Loubyanssky
 */
public class GalleonFeaturePack implements GalleonArtifactCoordinate {

    private String groupId;
    private String artifactId;
    private String version;
    private String type = "zip";
    private String classifier;
    private String extension = "zip";

    private boolean transitiveDep;
    private String location;

    private Boolean inheritConfigs;
    private Set<ConfigurationId> includedConfigs = Collections.emptySet();
    private Set<ConfigurationId> excludedConfigs = Collections.emptySet();

    private Boolean inheritPackages;
    private Set<String> excludedPackages = Collections.emptySet();
    private Set<String> includedPackages = Collections.emptySet();

    private Path path;

    @Override
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        assertGalleon1Location();
        this.groupId = groupId;
    }

    @Override
    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        assertGalleon1Location();
        this.artifactId = artifactId;
    }

    @Override
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        assertGalleon1Location();
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        assertGalleon1Location();
        this.type = type;
    }

    @Override
    public String getClassifier() {
        return classifier;
    }

    public void setClassifier(String classifier) {
        assertGalleon1Location();
        this.classifier = classifier;
    }

    @Override
    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        assertGalleon1Location();
        this.extension = extension;
    }

    public boolean isTransitive() {
        return transitiveDep;
    }

    public void setTransitive(boolean transitiveDep) {
        this.transitiveDep = transitiveDep;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        assertGalleon2Location();
        this.location = location;
    }

    public Boolean isInheritPackages() {
        return inheritPackages;
    }

    public void setInheritPackages(Boolean inheritPackages) {
        this.inheritPackages = inheritPackages;
    }

    public Boolean isInheritConfigs() {
        return inheritConfigs;
    }

    public void setInheritConfigs(Boolean inheritConfigs) {
        this.inheritConfigs = inheritConfigs;
    }

    public Set<ConfigurationId> getIncludedConfigs() {
        return includedConfigs;
    }

    public void setIncludedConfigs(Set<ConfigurationId> includedConfigs) {
        this.includedConfigs = includedConfigs;
    }

    public Set<ConfigurationId> getExcludedConfigs() {
        return excludedConfigs;
    }

    public void setExcludedConfigs(Set<ConfigurationId> excludedConfigs) {
        this.excludedConfigs = excludedConfigs;
    }

    public Set<String> getExcludedPackages() {
        return excludedPackages;
    }

    public void setExcludedPackages(Set<String> excludedPackages) {
        this.excludedPackages = excludedPackages;
    }

    public Set<String> getIncludedPackages() {
        return includedPackages;
    }

    public void setIncludedPackages(Set<String> includedPackages) {
        this.includedPackages = includedPackages;
    }

    public void setPath(File path) {
        assertPathLocation();
        this.path = path.toPath().normalize();
    }

    public Path getNormalizedPath() {
        return path;
    }

    public String getMavenCoords() {
        StringBuilder builder = new StringBuilder();
        builder.append(getGroupId()).append(":").append(getArtifactId());
        String type = getExtension() == null ? getType() : getExtension();
        if (getClassifier() != null || type != null) {
            builder.append(":").append(getClassifier() == null ? "" : getClassifier()).append(":").append(type == null ? "" : type);
        }
        if (getVersion() != null) {
            builder.append(":").append(getVersion());
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append('{');
        if (transitiveDep) {
            buf.append("transitive ");
        }
        if (location != null) {
            buf.append(location);
        } else {
            buf.append(groupId).append(':').append(artifactId).append(':').append(version);
        }
        buf.append(" inherit-packages=").append(inheritPackages);
        if (!includedPackages.isEmpty()) {
            buf.append(" included-packages=");
            StringUtils.append(buf, includedPackages);
        }
        if (!excludedPackages.isEmpty()) {
            buf.append(" excluded-packages=");
            StringUtils.append(buf, excludedPackages);
        }
        buf.append(" inherit-configs=").append(inheritConfigs);
        if (!includedConfigs.isEmpty()) {
            buf.append(" included-configs=");
            StringUtils.append(buf, includedConfigs);
        }
        if (!excludedConfigs.isEmpty()) {
            buf.append(" excluded-configs=");
            StringUtils.append(buf, excludedConfigs);
        }
        return buf.append('}').toString();
    }

    private void assertPathLocation() {
        if (groupId != null || artifactId != null || version != null) {
            throw new IllegalStateException("feature-pack Path cannot be used: Galleon 1.x feature-pack Maven coordinates have already been initialized");
        }
        if (location != null) {
            throw new IllegalStateException("feature-pack Path cannot be used: Galleon 2.x location has already been initialized");
        }
    }

    private void assertGalleon2Location() {
        if (groupId != null || artifactId != null || version != null) {
            throw new IllegalStateException("Galleon 2.x location cannot be used: feature-pack Maven coordinates have already been initialized");
        }
        if (path != null) {
            throw new IllegalStateException("Galleon 2.x location cannot be used: feature-pack Path has already been initialized");
        }
    }

    private void assertGalleon1Location() {
        if (location != null) {
            throw new IllegalStateException("Galleon 1.x feature-pack Maven coordinates cannot be used: Galleon 2.x feature-pack location has already been initialized");
        }
        if (path != null) {
            throw new IllegalStateException("Galleon 1.x feature-pack Maven coordinates cannot be used: feature-pack Path has already been initialized");
        }
    }
}
