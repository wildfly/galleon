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
package org.jboss.galleon;

/**
 * A GroupId/ArtifactId/Version tripple. {@link #version} can be {@code null} while {@link #groupId} and
 * {@link #artifactId} cannot be {@code null}.
 *
 * @author Alexey Loubyansky
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class Gav implements Comparable<Gav> {

    public static Gav parse(String str) {

        int i = str.indexOf(':');
        if(i <= 0) {
            throw new IllegalArgumentException("groupId is missing in '" + str + "'");
        }
        final String groupId = str.substring(0, i);
        final String artifactId;
        final String version;
        i = str.indexOf(':', i + 1);
        if(i < 0) {
            artifactId = str.substring(groupId.length() + 1);
            version = null;
        } else {
            artifactId = str.substring(groupId.length() + 1, i);
            version = str.substring(i + 1);
        }
        return new Gav(groupId, artifactId, version);
    }

    private final String groupId;
    private final String artifactId;
    private final String version;

    public Gav(String groupId, String artifactId, String version) {
        super();
        if (groupId == null || groupId.isEmpty()) {
            throw new IllegalArgumentException("null groupId");
        }
        this.groupId = groupId;
        if (artifactId == null || artifactId.isEmpty()) {
            throw new IllegalArgumentException("null artifactId");
        }
        this.artifactId = artifactId;
        if (version == null || version.isEmpty()) {
            this.version = null;
        } else {
            this.version = version;
        }
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public ArtifactCoords toArtifactCoords(String extension) {
        return new ArtifactCoords(groupId, artifactId, version, null, extension);
    }

    public ArtifactCoords toArtifactCoords(String classifier, String extension) {
        return new ArtifactCoords(groupId, artifactId, version, classifier, extension);
    }

    public Ga toGa() {
        return new Ga(groupId, artifactId);
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        if (groupId != null) {
            buf.append(groupId);
        }
        buf.append(':');
        if (artifactId != null) {
            buf.append(artifactId);
        }
        if (version != null) {
            buf.append(':').append(version);
        }
        return buf.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((artifactId == null) ? 0 : artifactId.hashCode());
        result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
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
        Gav other = (Gav) obj;
        if (artifactId == null) {
            if (other.getArtifactId() != null)
                return false;
        } else if (!artifactId.equals(other.getArtifactId()))
            return false;
        if (groupId == null) {
            if (other.getGroupId() != null)
                return false;
        } else if (!groupId.equals(other.getGroupId()))
            return false;
        if (version == null) {
            if (other.getVersion() != null)
                return false;
        } else if (!version.equals(other.getVersion()))
            return false;
        return true;
    }

    @Override
    public int compareTo(Gav o) {
        if (o == null) {
            return 1;
        }
        int i = groupId.compareTo(o.getGroupId());
        if (i != 0) {
            return i;
        }
        i = artifactId.compareTo(o.getArtifactId());
        if (i != 0) {
            return i;
        }
        if (version == null) {
            return o.getVersion() == null ? 0 : -1;
        }
        if (o.getVersion() == null) {
            return 1;
        }
        return version.compareTo(o.getVersion());
    }
}