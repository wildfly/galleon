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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.ArtifactException;
import org.jboss.galleon.ArtifactRepositoryManager;
import org.jboss.galleon.cli.config.Configuration;
import org.jboss.galleon.cli.config.mvn.MavenConfig;
import org.jboss.galleon.cli.config.mvn.MavenConfig.MavenChangeListener;

/**
 *
 * @author jdenise@redhat.com
 */
public class Universes implements MavenChangeListener {

    private final List<Universe> universes = new ArrayList<>();
    private final List<UniverseLocation> locations;
    private final ArtifactRepositoryManager manager;

    private Universes(List<UniverseLocation> locations, ArtifactRepositoryManager manager) {
        this.locations = locations;
        this.manager = manager;
        addUniverses();
    }

    private void addUniverses() {
        try {
            for (UniverseLocation loc : locations) {
                addUniverse(Universe.buildUniverse(manager, loc));
            }
        } catch (Exception ex) {
            // TO REMOVE, universe is a prototype not found in all contexts.
        }
    }

    @Override
    public void configurationChanged(MavenConfig config) throws XMLStreamException, IOException {
        universes.clear();
        addUniverses();
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
        throw new ArtifactException("Can't resolve stream " + name);
    }

    private void addUniverse(Universe universe) {
        universes.add(universe);
    }

    static Universes buildUniverses(Configuration config, ArtifactRepositoryManager manager) throws Exception {
        Universes universes = new Universes(config.getUniversesLocations(), manager);
        config.getMavenConfig().addListener(universes);
        return universes;
    }
}
