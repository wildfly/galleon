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
 * A {@link #resolved} object of type {@code T} (typically a {@link Gaecv} or a {@link Gaecvp}) combined with a
 * {@link GaecRange} out of which it was resolved.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class ResolvedGaecRange<T> {

    /**
     * If the given {@link GaecRange} is a single version range (i.e. it starts with neither {@code '('} nor
     * {@code '['}) returns a new {@link ResolvedGaecRange}; otherwise throws a {@link IllegalArgumentException}.
     *
     * @param gaecRange to use as {@link #gaecRange} and to infer {@link #resolved} from.
     * @return a new {@link ResolvedGaecRange}
     */
    public static ResolvedGaecRange<Gaecv> ofSingle(GaecRange gaecRange) {
        String versionRange = gaecRange.getVersionRange();
        switch (versionRange.charAt(0)) {
        case '(':
        case '[':
            throw new IllegalArgumentException("null gaecv");
        default:
            return new ResolvedGaecRange<Gaecv>(gaecRange, new Gaecv(gaecRange.getGaec(), versionRange));
        }
    }

    /**
     * Each plain version string is trivially a version range. Therefore the {@link GaecRange} can be inferred from a
     * {@link Gaecv}.
     *
     * @param gaecv to use as {@link #resolved} and to infer {@link #gaecRange} from.
     * @return a new {@link ResolvedGaecRange}
     */
    public static ResolvedGaecRange<Gaecv> ofSingleGaecv(Gaecv gaecv) {
        return new ResolvedGaecRange<Gaecv>(gaecv.toGaecRange(), gaecv);
    }

    /**
     * Each plain version string is trivially a version range. Therefore the {@link GaecRange} can be inferred from a
     * {@link Gaecvp}.
     *
     * @param gaecvp to use as {@link #resolved} and to infer {@link #gaecRange} from.
     * @return a new {@link ResolvedGaecRange}
     */
    public static ResolvedGaecRange<Gaecvp> ofSingleGaecvp(Gaecvp gaecvp) {
        return new ResolvedGaecRange<Gaecvp>(gaecvp.getGaecv().toGaecRange(), gaecvp);
    }

    private final GaecRange gaecRange;
    private final T resolved;

    public ResolvedGaecRange(GaecRange gaecRange, T resolved) {
        super();
        if (resolved == null) {
            throw new IllegalArgumentException("null resolved");
        }
        this.resolved = resolved;
        if (gaecRange == null) {
            throw new IllegalArgumentException("null gaecRange");
        }
        this.gaecRange = gaecRange;
    }

    /**
     * @return a {@link GaecRange} that resolves to the {@link Gaecv} returned by {@link #getResolved()}
     */
    public GaecRange getGaecRange() {
        return gaecRange;
    }

    /**
     * @return a {@link Gaecv} or {@link Gaecvp} resolved from the {@link GaecRange} returned by {@link #getGaecRange()}
     */
    public T getResolved() {
        return resolved;
    }
}
