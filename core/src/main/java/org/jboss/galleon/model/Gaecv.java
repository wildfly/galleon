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
 * A {@link Gaec} with a {@link #version}.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class Gaecv {

    public static class Builder {
        private Gaec.Builder gaecBuilder = Gaec.builder();
        private String version;

        public Builder artifactId(String artifactId) {
            gaecBuilder.artifactId(artifactId);
            return this;
        }
        public Gaecv build() {
            return new Gaecv(gaecBuilder.build(), version);
        }
        public Builder classifier(String classifier) {
            gaecBuilder.classifier(classifier);
            return this;
        }
        public Builder extension(String extension) {
            gaecBuilder.extension(extension);
            return this;
        }
        public Builder groupId(String groupId) {
            gaecBuilder.groupId(groupId);
            return this;
        }
        public Builder version(String version) {
            this.version = version;
            return this;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Gaecv parse(String rawGaecv) {
        final String[] segments = rawGaecv.split(":");
        if (segments.length != 5) {
            throw new IllegalArgumentException("Could not parse "+ Gaecv.class.getSimpleName() +" out of '"+ rawGaecv +"'; expected five colon delimited segments");
        }
        return new Gaecv(new Gaec(segments[0], segments[1], segments[2], segments[3]), segments[4]);
    }

    private final Gaec gaec;
    private final String version;

    public Gaecv(Gaec gaec, String version) {
        super();
        if (gaec == null) {
            throw new IllegalArgumentException("gaec cannot be null");
        }
        this.gaec = gaec;
        if (version == null) {
            throw new IllegalArgumentException("version cannot be null");
        }
        this.version = version;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Gaecv other = (Gaecv) obj;
        return version.equals(other.version) && gaec.equals(other.gaec);
    }

    public String getArtifactFileName() {
        final StringBuilder fileName = new StringBuilder();
        fileName.append(gaec.getArtifactId()).append('-').append(version);
        if(gaec.hasClassifier()) {
            fileName.append('-').append(gaec.getClassifier());
        }
        fileName.append('.').append(gaec.getExtension());
        return fileName.toString();
    }

    public Gaec getGaec() {
        return gaec;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + gaec.hashCode();
        result = prime * result + version.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return gaec.toString() + ":"+ version;
    }

    /**
     * This transformation is legal, because each version string is a valid range. See <a href=
     * "https://maven.apache.org/ref/3.5.0/maven-artifact/apidocs/org/apache/maven/artifact/versioning/VersionRange.html#createFromVersionSpec(java.lang.String)">org.apache.maven.artifact.versioning.VersionRange</a>
     *
     * @return a new {@link GaecRange}
     */
    public GaecRange toGaecRange() {
        return new GaecRange(gaec, version);
    }
}
