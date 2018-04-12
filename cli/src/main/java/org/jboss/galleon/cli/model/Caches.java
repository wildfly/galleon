/*
 * Copyright ${license.git.copyrightYears} Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.cli.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.galleon.ArtifactCoords;

/**
 *
 * @author jdenise@redhat.com
 */
public class Caches {

    private static final Map<ArtifactCoords.Gav, FeatureContainer> FP_CACHE = new HashMap<>();
    private static final Map<ArtifactCoords.Gav, Set<FeatureSpecInfo>> SPEC_CACHE = new HashMap<>();

    public static FeatureContainer getFeaturePackInfo(ArtifactCoords.Gav gav) {
        return FP_CACHE.get(gav);
    }

    public static Map<ArtifactCoords.Gav, Set<FeatureSpecInfo>> getSpecs() throws IOException {
        return SPEC_CACHE;
    }

    public static void addFeaturePackInfo(ArtifactCoords.Gav gav, FeatureContainer info) {
        FP_CACHE.put(gav, info);
    }

    public static void addSpecs(Map<ArtifactCoords.Gav, Set<FeatureSpecInfo>> specs) throws IOException {
        SPEC_CACHE.putAll(specs);
    }

}
