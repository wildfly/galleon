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


import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.shared.artifact.ArtifactCoordinate;
import org.apache.maven.shared.dependencies.DependableCoordinate;
import org.codehaus.plexus.util.StringUtils;
import org.jboss.galleon.ArtifactCoords;

/**
 * ArtifactItem represents information specified for each artifact.
 */
public class ArtifactItem
        implements DependableCoordinate, ArtifactCoordinate {

    /**
     * Group Id of Artifact
     *
     * @parameter
     * @required
     */
    private String groupId;

    /**
     * Name of Artifact
     *
     * @parameter
     * @required
     */
    private String artifactId;

    /**
     * Version of Artifact
     *
     * @parameter
     */
    private String version = null;

    /**
     * Type of Artifact (War,Jar,etc)
     *
     * @parameter
     * @required
     */
    private String type = "jar";

    /**
     * Classifier for Artifact (tests,sources,etc)
     *
     * @parameter
     */
    private String classifier;

    /**
     * Extension of artifact
     *
     * @parameter
     */
    private String extension;

    /**
     * Artifact Item
     */
    private Artifact artifact;

    public ArtifactItem() {
        // default constructor
    }

    public ArtifactItem(Artifact artifact) {
        this.setArtifact(artifact);
        this.setArtifactId(artifact.getArtifactId());
        this.setClassifier(artifact.getClassifier());
        this.setGroupId(artifact.getGroupId());
        this.setType(artifact.getType());
        this.setVersion(artifact.getVersion());
    }

    private String filterEmptyString(String in) {
        if ("".equals(in)) {
            return null;
        }
        return in;
    }

    /**
     * @return Returns the artifactId.
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * @param artifact The artifactId to set.
     */
    public void setArtifactId(String artifact) {
        this.artifactId = filterEmptyString(artifact);
    }

    /**
     * @return Returns the groupId.
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * @param groupId The groupId to set.
     */
    public void setGroupId(String groupId) {
        this.groupId = filterEmptyString(groupId);
    }

    /**
     * @return Returns the type.
     */
    public String getType() {
        return type;
    }

    /**
     * @param type The type to set.
     */
    public void setType(String type) {
        this.type = filterEmptyString(type);
    }

    /**
     * @return Returns the version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version The version to set.
     */
    public void setVersion(String version) {
        this.version = filterEmptyString(version);
    }

    /**
     * @return Returns the base version.
     */
    public String getBaseVersion() {
        return ArtifactUtils.toSnapshotVersion(version);
    }

    /**
     * @return Classifier.
     */
    @Override
    public String getClassifier() {
        return classifier;
    }

    /**
     * @param classifier Classifier.
     */
    public void setClassifier(String classifier) {
        this.classifier = filterEmptyString(classifier);
    }

    @Override
    public String toString() {
        if (this.classifier == null) {
            return groupId + ":" + artifactId + ":" + StringUtils.defaultString(version, "?") + ":" + type;
        } else {
            return groupId + ":" + artifactId + ":" + classifier + ":" + StringUtils.defaultString(version, "?") + ":"
                    + type;
        }
    }

    /**
     * @return Returns the artifact.
     */
    public Artifact getArtifact() {
        return this.artifact;
    }

    /**
     * @param artifact The artifact to set.
     */
    public void setArtifact(Artifact artifact) {
        this.artifact = artifact;
    }

    public void setExtension(String extension) {
        this.extension = filterEmptyString(extension);
    }

    @Override
    public String getExtension() {
        return extension != null ? extension : "jar";
    }

    public ArtifactCoords getArtifactCoords() {
        return new ArtifactCoords(groupId, artifactId, version, classifier, extension);
    }

}
