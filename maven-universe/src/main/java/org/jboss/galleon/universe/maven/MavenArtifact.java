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

package org.jboss.galleon.universe.maven;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Alexey Loubyansky
 */
public class MavenArtifact {

    private static Pattern COORDS_PATTERN;

    public static final String EXT_JAR = "jar";
    public static final String EXT_ZIP = "zip";

    public static MavenArtifact fromString(String str) throws MavenUniverseException {
        if(COORDS_PATTERN == null) {
            COORDS_PATTERN = Pattern.compile("([^: ]+):([^: ]+)(:([^: ]*)(:([^: ]+))?)?:([^: ]+)");
        }
        final Matcher m = COORDS_PATTERN.matcher(str);
        if (!m.matches()) {
            throw new MavenUniverseException("Bad artifact coordinates " + str
                    + ", expected format is <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>");
        }
        final MavenArtifact artifact = new MavenArtifact();
        artifact.setGroupId(m.group(1));
        artifact.setArtifactId(m.group(2));
        String v = m.group(4);
        if(v != null && v.length() > 0) {
            artifact.setExtension(v);
        }
        v = m.group(6);
        if(v != null && v.length() > 0) {
            artifact.setClassifier(v);
        }
        artifact.setVersion(m.group(7));
        return artifact;
    }

    private String groupId;
    private String artifactId;
    private String version;
    private String classifier = "";
    private String extension = EXT_JAR;
    private String versionRange;
    private Path path;

    public MavenArtifact() {
    }

    public String getGroupId() {
        return groupId;
    }

    public MavenArtifact setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public MavenArtifact setArtifactId(String artifactId) {
        this.artifactId = artifactId;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public MavenArtifact setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getClassifier() {
        return classifier;
    }

    public MavenArtifact setClassifier(String classifier) {
        this.classifier = classifier;
        return this;
    }

    public String getExtension() {
        return extension;
    }

    public MavenArtifact setExtension(String extension) {
        this.extension = extension;
        return this;
    }

    public String getVersionRange() {
        return versionRange;
    }

    public MavenArtifact setVersionRange(String versionRange) {
        this.versionRange = versionRange;
        return this;
    }

    public Path getPath() {
        return path;
    }

    public MavenArtifact setPath(Path localArtifact) {
        this.path = localArtifact;
        return this;
    }

    public boolean isResolved() {
        return path != null;
    }

    public String getArtifactFileName() throws MavenUniverseException {
        if(artifactId == null) {
            MavenErrors.missingArtifactId();
        }
        if(version == null) {
            MavenErrors.missingVersion(this);
        }
        if(extension == null) {
            MavenErrors.missingExtension(this);
        }
        final StringBuilder fileName = new StringBuilder();
        fileName.append(artifactId).append('-').append(version);
        if(classifier != null && !classifier.isEmpty()) {
            fileName.append('-').append(classifier);
        }
        fileName.append('.').append(extension);
        return fileName.toString();
    }

    public String getCoordsAsString() {
        final StringBuilder buf = new StringBuilder();
        buf.append(groupId).append(':').append(artifactId);
        if(extension != null) {
            buf.append(':').append(extension);
        }
        if(!classifier.isEmpty()) {
            buf.append(':').append(classifier);
        }
        if(version != null) {
            buf.append(':').append(version);
        }
        return buf.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((artifactId == null) ? 0 : artifactId.hashCode());
        result = prime * result + ((classifier == null) ? 0 : classifier.hashCode());
        result = prime * result + ((extension == null) ? 0 : extension.hashCode());
        result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        result = prime * result + ((versionRange == null) ? 0 : versionRange.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MavenArtifact other = (MavenArtifact) obj;
        if (artifactId == null) {
            if (other.artifactId != null)
                return false;
        } else if (!artifactId.equals(other.artifactId))
            return false;
        if (classifier == null) {
            if (other.classifier != null)
                return false;
        } else if (!classifier.equals(other.classifier))
            return false;
        if (extension == null) {
            if (other.extension != null)
                return false;
        } else if (!extension.equals(other.extension))
            return false;
        if (groupId == null) {
            if (other.groupId != null)
                return false;
        } else if (!groupId.equals(other.groupId))
            return false;
        if (version == null) {
            if (other.version != null)
                return false;
        } else if (!version.equals(other.version))
            return false;
        if (versionRange == null) {
            if (other.versionRange != null)
                return false;
        } else if (!versionRange.equals(other.versionRange))
            return false;
        return true;
    }
}
