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

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Complete Maven artifact coordinates.
 *
 * @author Alexey Loubyansky
 */
public class ArtifactCoords implements Comparable<ArtifactCoords> {

    private static final Pattern COORDS_PATTERN = Pattern.compile("([^: ]+):([^: ]+)(:([^: ]*)(:([^: ]+))?)?:([^: ]+)");

    public static ArtifactCoords newInstance(String groupId, String artifactId, String version, String extension) {
        return new ArtifactCoords(groupId, artifactId, version, null, extension);
    }

    public static ArtifactCoords fromGav(Gav gav, String extension) {
        return new ArtifactCoords(gav.getGroupId(), gav.getArtifactId(), gav.getVersion(), null, extension);
    }

    public static ArtifactCoords fromString(String str) {
        return new ArtifactCoords(str);
    }

    public static Gav newGav(String groupId, String artifactId, String version) {
        return new ArtifactCoords(groupId, artifactId, version, "", "zip").toGav();
    }

    public static Gav newGav(String str) {

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
        return newGav(groupId, artifactId, version);
    }

    public static Ga newGa(String groupId, String artifactId) {
        return new ArtifactCoords(groupId, artifactId, null, "", "zip").toGa();
    }

    private static String get(String value, String defaultValue) {
        return (value == null || value.length() <= 0) ? defaultValue : value;
    }

    /**
     * GroupId/ArtifactId/Version view of ArtifactCoords
     *
     * @author Alexey Loubyansky
     */
    public class Gav implements Comparable<Gav> {

        public String getGroupId() {
            return groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public String getVersion() {
            return version;
        }

        public ArtifactCoords toArtifactCoords() {
            return ArtifactCoords.this;
        }

        public Ga toGa() {
            return ArtifactCoords.this.toGa();
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
            if(o == null) {
                return 1;
            }
            int i = groupId.compareTo(o.getGroupId());
            if(i != 0) {
                return i;
            }
            i = artifactId.compareTo(o.getArtifactId());
            if(i != 0) {
                return i;
            }
            if(version == null) {
                return o.getVersion() == null ? 0 : -1;
            }
            if(o.getVersion() == null) {
                return 1;
            }
            return version.compareTo(o.getVersion());
        }
    }

    /**
     * GroupId/ArtifactId view of ArtifactCoords
     *
     * @author Alexey Loubyansky
     */
    public class Ga implements Comparable<Ga> {

        public String getGroupId() {
            return groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public Gav toGav() {
            return ArtifactCoords.this.toGav();
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

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String classifier;
    private final String extension;

    private final Gav gavPart;
    private final Ga gaPart;

    private ArtifactCoords(String str) {
        final Matcher m = COORDS_PATTERN.matcher(str);
        if (!m.matches()) {
            throw new IllegalArgumentException("Bad artifact coordinates " + str
                    + ", expected format is <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>");
        }
        groupId = m.group(1);
        artifactId = m.group(2);
        extension = get(m.group(4), "jar");
        classifier = get(m.group(6), "");
        version = m.group(7);

        gavPart = new Gav();
        gaPart = new Ga();
    }

    public ArtifactCoords(String groupId, String artifactId, String version, String classifier, String extension) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.classifier = get(classifier, "");
        this.extension = get(extension, "jar");

        gavPart = new Gav();
        gaPart = new Ga();
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

    public String getClassifier() {
        return classifier;
    }

    public String getExtension() {
        return extension;
    }

    public Gav toGav() {
        return gavPart;
    }

    public Ga toGa() {
        return gaPart;
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
        ArtifactCoords other = (ArtifactCoords) obj;
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
        return true;
    }

    @Override
    public int compareTo(ArtifactCoords o) {
        // compare groupIds
        int result = groupId.compareTo(o.groupId);
        if (result != 0) {
            return result;
        }

        // groupIds are the same, compare artifactIds
        result = artifactId.compareTo(o.artifactId);
        if (result != 0) {
            return result;
        }

        // artifactIds are the same, compare classifiers
        if (classifier != null) {
            if (o.classifier == null) {
                result = 1;
            } else {
                result = classifier.compareTo(o.classifier);
            }
        } else {
            if (o.classifier != null) {
                result = -1;
            }
        }
        if (result != 0) {
            return result;
        }

        // classifiers are the same, compare extensions
        if (extension != null) {
            if (o.extension == null) {
                result = 1;
            } else {
                result = extension.compareTo(o.extension);
            }
        } else {
            if (o.extension != null) {
                result = -1;
            }
        }
        if (result != 0) {
            return result;
        }

        if (version != null) {
            if (o.version == null) {
                result = 1;
            } else {
                result = version.compareTo(o.version);
            }
        } else {
            if (o.version != null) {
                result = -1;
            }
        }

        return result;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append(groupId).append(':').append(artifactId);
        if(!classifier.isEmpty()) {
            buf.append(':');
            if(extension != null) {
                buf.append(extension);
            }
            buf.append(':').append(classifier);
        }
        if(version != null) {
            buf.append(':').append(version);
        }
        return buf.toString();
    }
}
