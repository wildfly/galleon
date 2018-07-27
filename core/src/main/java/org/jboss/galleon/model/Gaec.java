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
package org.jboss.galleon.model;

/**
 * A {@code groupId:artifactId:extension:classifier} quadruple.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class Gaec implements Comparable<Gaec> {

    public static class Builder {
        private String artifactId;
        private String classifier;
        private String extension = "jar";
        private String groupId;

        public Builder artifactId(String artifactId) {
            this.artifactId = artifactId;
            return this;
        }
        public Gaec build() {
            return new Gaec(groupId, artifactId, extension, classifier);
        }
        public Builder classifier(String classifier) {
            this.classifier = classifier;
            return this;
        }
        public Builder extension(String extension) {
            this.extension = extension;
            return this;
        }
        public Builder groupId(String groupId) {
            this.groupId = groupId;
            return this;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Gaec parse(String rawGaec) {
        final String[] segments = rawGaec.split(":");
        if (segments.length != 4) {
            throw new IllegalArgumentException("Could not parse "+ Gaec.class.getSimpleName() +" out of '"+ rawGaec +"'; expected four colon delimited segments");
        }
        return new Gaec(segments[0], segments[1], segments[2], segments[3]);
    }

    private final String artifactId;
    private final String classifier;
    private final String extension;
    private final String groupId;

    public Gaec(String groupId, String artifactId, String extension) {
        this(groupId, artifactId, extension, null);
    }

    public Gaec(String groupId, String artifactId, String extension, String classifier) {
        super();
        if (groupId == null || groupId.isEmpty()) {
            throw new IllegalArgumentException("null groupId");
        }
        this.groupId = groupId;
        if (artifactId == null || artifactId.isEmpty()) {
            throw new IllegalArgumentException("null artifactId");
        }
        this.artifactId = artifactId;
        if (extension == null || extension.isEmpty()) {
            throw new IllegalArgumentException("null extension");
        }
        this.extension = extension;
        if (classifier == null || classifier.isEmpty()) {
            this.classifier = null;
        } else {
            this.classifier = classifier;
        }
    }

    @Override
    public int compareTo(Gaec o) {
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
        Gaec other = (Gaec) obj;
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
        return true;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getClassifier() {
        return classifier;
    }

    public String getExtension() {
        return extension;
    }

    public String getGroupId() {
        return groupId;
    }

    public boolean hasClassifier() {
        return classifier != null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + groupId.hashCode();
        result = prime * result + artifactId.hashCode();
        result = prime * result + extension.hashCode();
        result = prime * result + ((classifier == null) ? 0 : classifier.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return toString(4);
    }

    public String toString(int segmentCount) {
        if (segmentCount < 1) {
            throw new IllegalArgumentException("segmentCount has to be >= 1; found "+ segmentCount);
        }
        if (segmentCount > 4) {
            throw new IllegalArgumentException("segmentCount has to be <= 4; found "+ segmentCount);
        }
        int len = groupId.length();
        if (segmentCount > 1) {
            len += artifactId.length() + 1;
            if (segmentCount > 2) {
                len += extension.length() + 1;
                if (segmentCount > 3) {
                    len += (classifier != null ? classifier.length() : 0) + 1;
                }
            }
        }

        final StringBuilder buf = new StringBuilder(len).append(groupId);
        if (segmentCount > 1) {
            buf.append(':').append(artifactId);
            if (segmentCount > 2) {
                buf.append(':').append(extension);
                if (segmentCount > 3) {
                    buf.append(':');
                    if (segmentCount > 3 && classifier != null) {
                        buf.append(classifier);
                    }
                }
            }
        }
        return buf.toString();
    }

}
