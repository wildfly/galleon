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
 * A {@link Gaec} with a {@link #versionRange}.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class GaecRange {
    public static String ALL_VERSIONS_RANGE = "[0.0,)";
    public static class Builder {
        private Gaec.Builder gaecBuilder = Gaec.builder();
        private String versionRange = ALL_VERSIONS_RANGE;

        public Builder artifactId(String artifactId) {
            gaecBuilder.artifactId(artifactId);
            return this;
        }
        public GaecRange build() {
            return new GaecRange(gaecBuilder.build(), versionRange);
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
        public Builder versionRange(String versionRange) {
            this.versionRange = versionRange;
            return this;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
    public static GaecRange parse(String rawGaecRange) {
        final String[] segments = rawGaecRange.split(":");
        if (segments.length != 5) {
            throw new IllegalArgumentException("Could not parse "+ GaecRange.class.getSimpleName() +" out of '"+ rawGaecRange +"'; expected five colon delimited segments");
        }
        return new GaecRange(new Gaec(segments[0], segments[1], segments[2], segments[3]), segments[4]);
    }

    private final Gaec gaec;

    private final String versionRange;

    public GaecRange(Gaec gaec, String versionRange) {
        super();
        if (gaec == null) {
            throw new IllegalArgumentException("gaec cannot be null");
        }
        this.gaec = gaec;
        if (versionRange == null) {
            throw new IllegalArgumentException("versionRange cannot be null");
        }
        this.versionRange = versionRange;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GaecRange other = (GaecRange) obj;
        return versionRange.equals(other.versionRange) && gaec.equals(other.gaec);
    }

    public Gaec getGaec() {
        return gaec;
    }
    public String getVersionRange() {
        return versionRange;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + gaec.hashCode();
        result = prime * result + versionRange.hashCode();
        return result;
    }
    @Override
    public String toString() {
        return gaec.toString() + ":"+ versionRange;
    }

}
