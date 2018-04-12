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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.ArtifactException;
import org.jboss.galleon.ArtifactRepositoryManager;

/**
 *
 * @author jdenise@redhat.com
 */
public class Universes {

    private final List<Universe> universes = new ArrayList<>();

    private ArtifactRepositoryManager manager;

    private Universes(ArtifactRepositoryManager manager) {
        this.manager = manager;
    }

    public List<Universe> getUniverses() {
        return Collections.unmodifiableList(universes);
    }

    public ArtifactCoords resolveStream(String name) throws ArtifactException {
        for (Universe universe : universes) {
            ArtifactCoords coords = universe.resolveStream(name);
            if (coords != null) {
                return coords;
            }
        }
        return null;
    }

    private void addUniverse(Universe universe) {
        universes.add(universe);
    }

    static Universes buildUniverses(ArtifactRepositoryManager manager,
            List<UniverseLocation> locations) throws Exception {
        Universes universes = new Universes(manager);
        try {
            for (UniverseLocation loc : locations) {
                universes.addUniverse(Universe.buildUniverse(manager, loc));
            }
        } catch (Exception ex) {
            // TO REMOVE, universe is a prototype not found in all contexts.
        }
        return universes;
    }
}
