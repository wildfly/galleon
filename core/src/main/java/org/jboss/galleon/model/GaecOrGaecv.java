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
 * Contains either a {@link Gaec} or a {@link Gaecv}. Suitable for situations, where the version is optional.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class GaecOrGaecv {

    public static class Builder {
        private Gaec.Builder gaecBuilder = Gaec.builder();
        private String version;

        public Builder artifactId(String artifactId) {
            gaecBuilder.artifactId(artifactId);
            return this;
        }
        public GaecOrGaecv build() {
            if (version == null) {
                return unresolved(gaecBuilder.build());
            } else {
                return resolved(new Gaecv(gaecBuilder.build(), version));
            }
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

    public static GaecOrGaecv parse(String rawGaecv) {
        final String[] segments = rawGaecv.split(":");
        switch (segments.length) {
        case 4:
            return unresolved(new Gaec(segments[0], segments[1], segments[2], segments[3]));
        case 5:
            return resolved(new Gaecv(new Gaec(segments[0], segments[1], segments[2], segments[3]), segments[4]));
        default:
            throw new IllegalArgumentException("Could not parse "+ GaecOrGaecv.class.getSimpleName() +" out of '"+ rawGaecv +"'; expected five or four colon delimited segments");
        }
    }

    public static GaecOrGaecv resolved(Gaecv gaecv) {
        return new GaecOrGaecv(null, gaecv);
    }

    public static GaecOrGaecv unresolved(Gaec gaec) {
        return new GaecOrGaecv(gaec, null);
    }

    private final Gaec gaec;
    private final Gaecv gaecv;

    GaecOrGaecv(Gaec gaec, Gaecv gaecv) {
        super();
        if ((gaec == null) == (gaecv == null)) {
            throw new IllegalArgumentException("Exactly one of gaec and gaecv has to be non null");
        }
        this.gaec = gaec;
        this.gaecv = gaecv;
    }

    public Gaec getGaec() {
        return gaecv != null ? gaecv.getGaec() : gaec;
    }

    public Gaecv getGaecv() {
        return gaecv;
    }

    public boolean isVersionResolved() {
        return gaecv != null;
    }

    public static Builder builder() {
        return new Builder();
    }
}
