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
 * A GroupId/ArtifactId pair. Both elements cannot be {@code null}.
 *
 * @author Alexey Loubyansky
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 *
 */
public class Ga implements Comparable<Ga> {

    public Ga(String groupId, String artifactId) {
        super();
        if (groupId == null || groupId.isEmpty()) {
            throw new IllegalArgumentException("null groupId");
        }
        this.groupId = groupId;
        if (artifactId == null || artifactId.isEmpty()) {
            throw new IllegalArgumentException("null artifactId");
        }
        this.artifactId = artifactId;
    }

    private final String groupId;
    private final String artifactId;

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public Gav toGav(String version) {
        return new Gav(groupId, artifactId, version);
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        if(groupId != null) {
            buf.append(groupId);
        }
        buf.append(':');
        if(artifactId != null) {
            buf.append(artifactId);
        }
        return buf.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((artifactId == null) ? 0 : artifactId.hashCode());
        result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
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
        Ga other = (Ga) obj;
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
        return true;
    }

    @Override
    public int compareTo(Ga o) {
        if(o == null) {
            return 1;
        }
        int i = groupId.compareTo(o.getGroupId());
        if(i != 0) {
            return i;
        }
        return artifactId.compareTo(o.getArtifactId());
    }
}