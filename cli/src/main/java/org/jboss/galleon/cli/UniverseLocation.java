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
package org.jboss.galleon.cli;

import org.jboss.galleon.ArtifactCoords;

/**
 *
 * @author jdenise@redhat.com
 */
public class UniverseLocation {
    private static final String UNIVERSE_VERSION_RANGE = "[0,)";
    public static UniverseLocation DEFAULT = new UniverseLocation("default",
            ArtifactCoords.newInstance("org.jboss.universe", "universe", null, "jar"));
    private final String name;
    private ArtifactCoords coords;

    public UniverseLocation(String name, ArtifactCoords coords) {
        this.name = name;
        this.coords = coords;
    }

    public String getName() {
        return name;
    }

    public ArtifactCoords getCoordinates() {
        return coords;
    }

    public String getVersionRange() {
        return UNIVERSE_VERSION_RANGE;
    }

    /**
     * Version has been retrieved.
       *
     * @param version The latest version of the universe location.
     */
    public void updateLatestVersion(String version) {
        this.coords = new ArtifactCoords(coords.getGroupId(), coords.getArtifactId(),
                version, coords.getClassifier(), coords.getExtension());
    }
}
