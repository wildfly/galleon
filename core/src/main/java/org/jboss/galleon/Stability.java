/*
 * Copyright 2016-2024 Red Hat, Inc. and/or its affiliates
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

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * Enumeration of stability levels.
 * Copied from wildfly-core.
 * @author Paul Ferraro
 */
public enum Stability {

    DEFAULT("default"),
    COMMUNITY("community"),
    PREVIEW("preview"),
    EXPERIMENTAL("experimental"),
    ;
    private final String value;

    public static Stability fromString(String value) {
        return Enum.valueOf(Stability.class, value.toUpperCase(Locale.ENGLISH));
    }

    Stability(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    /**
     * Indicates whether this stability enables the specified stability level.
     * @param stability a stability level
     * @return true, if this stability level enables the specified stability level, false otherwise.
     */
    public boolean enables(Stability stability) {
        // Currently assumes ascending nested sets
        return stability.ordinal() <= this.ordinal();
    }

    /**
     * Returns a complete map of a feature per stability level.
     * @param <F> the feature type
     * @param features a function returning the feature of a given stability level
     * @return a full mapping of feature per stability level
     */
    public static <F> Map<Stability, F> map(Function<Stability, F> features) {
        Map<Stability, F> map = new EnumMap<>(Stability.class);
        F lastStability = null;
        // Currently assumes ascending nested sets
        for (Stability stability : EnumSet.allOf(Stability.class)) {
            F feature = features.apply(stability);
            if (feature != null) {
                lastStability = feature;
            }
            if (lastStability != null) {
                map.put(stability, lastStability);
            }
        }
        return Collections.unmodifiableMap(map);
    }
}
