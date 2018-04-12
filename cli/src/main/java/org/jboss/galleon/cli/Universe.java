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

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.ArtifactException;
import org.jboss.galleon.ArtifactRepositoryManager;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLMapper;

/**
 *
 * @author jdenise@redhat.com
 */
public class Universe {

    public static final String NS = "urn:jboss:universe:1.0";
    private static final String UNIVERSE = "universe";
    private static final String UNIVERSE_FILE = "universe.xml";

    private static final String STREAM = "stream";
    private static final String GROUP_ID = "group-id";
    private static final String ARTIFACT_ID = "artifact-id";
    private static final String VERSION_RANGE = "version-range";
    private static final String NAME = "name";

    public static class StreamLocation {

        private final String name;
        private ArtifactCoords coordinates;
        private final String versionRange;
        private boolean resolved;

        private StreamLocation(String name, ArtifactCoords coordinates, String versionRange) {
            this.name = name;
            this.coordinates = coordinates;
            this.versionRange = versionRange;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @return the coordinates
         */
        public ArtifactCoords getCoordinates() {
            return coordinates;
        }

        /**
         * @return the versionRange
         */
        public String getVersionRange() {
            return versionRange;
        }

        private void resolve(ArtifactRepositoryManager manager) throws ArtifactException {
            if (!resolved) {
                String latestVersion = manager.getHighestVersion(coordinates, versionRange);
                if (latestVersion != null) {
                    coordinates = new ArtifactCoords(coordinates.getGroupId(), coordinates.getArtifactId(),
                            latestVersion, coordinates.getClassifier(), coordinates.getExtension());
                }
                resolved = true;
            }
        }

        private boolean resolved() {
            return resolved;
        }
    }

    static class UniverseReader implements XMLElementReader<Universe> {

        @Override
        public void readElement(XMLExtendedStreamReader reader, Universe universe) throws XMLStreamException {
            String localName = reader.getLocalName();
            if (!UNIVERSE.equals(localName)) {
                throw new XMLStreamException("Unexpected element: " + localName);
            }
            readUniverseElement_1_0(reader, universe);
        }

        public void readUniverseElement_1_0(XMLExtendedStreamReader reader, Universe universe) throws XMLStreamException {
            boolean universeEnded = false;
            while (reader.hasNext() && universeEnded == false) {
                int tag = reader.nextTag();
                if (tag == XMLStreamConstants.START_ELEMENT) {
                    final String localName = reader.getLocalName();
                    if (localName.equals(STREAM)) {
                        // For now, stream reference the feature pack directly.
                        String groupId = reader.getAttributeValue(null, GROUP_ID);
                        String artifactId = reader.getAttributeValue(null, ARTIFACT_ID);
                        // TODO, NO NEED FOR VERSION RANGE MUST REMOVE AT SOME POINT
                        String versionRange = reader.getAttributeValue(null, VERSION_RANGE);
                        String name = reader.getAttributeValue(null, NAME);
                        universe.addStreamLocation(new StreamLocation(name,
                                ArtifactCoords.newInstance(groupId, artifactId, null, null), versionRange));
                    } else {
                        throw new XMLStreamException("Unexpected element: " + localName);
                    }
                } else if (tag == XMLStreamConstants.END_ELEMENT) {
                    final String localName = reader.getLocalName();
                    if (localName.equals(UNIVERSE)) {
                        universeEnded = true;
                    }
                }
            }
        }
    }

    private final Map<String, StreamLocation> streamLocations = new HashMap<>();
    private final UniverseLocation location;
    private final ArtifactRepositoryManager manager;

    private Universe(UniverseLocation location, ArtifactRepositoryManager manager) {
        this.location = location;
        this.manager = manager;
    }

    private void addStreamLocation(StreamLocation location) {
        streamLocations.put(location.getName(), location);
    }

    public UniverseLocation getLocation() {
        return location;
    }

    public Collection<StreamLocation> getStreamLocations() {
        return Collections.unmodifiableCollection(streamLocations.values());
    }

    public ArtifactCoords resolveStream(String name) throws ArtifactException {
        StreamLocation loc = streamLocations.get(name);
        if (loc == null) {
            throw new ArtifactException("Unknown stream " + name);
        }
        if (!loc.resolved()) {
            loc.resolve(manager);
        }
        return loc.getCoordinates();
    }

    static Universe buildUniverse(ArtifactRepositoryManager manager,
            UniverseLocation location) throws Exception {
        String version = location.getCoordinates().getVersion();
        if (version == null || version.isEmpty()) {
            String latestVersion
                    = manager.getHighestVersion(location.getCoordinates(),
                            location.getVersionRange());
            if (latestVersion != null) {
                location.updateLatestVersion(latestVersion);
            }
        }
        Universe universe = new Universe(location, manager);
        Path p = manager.resolve(location.getCoordinates());
        try (JarFile jarFile = new JarFile(p.toFile())) {
            final Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                if (entry.getName().equals(UNIVERSE_FILE)) {
                    InputStream input = jarFile.getInputStream(entry);
                    return parse(input, universe);
                }
            }
        }
        throw new Exception("Universe content not found");
    }

    private static Universe parse(InputStream input, Universe universe) throws Exception {
        final XMLMapper mapper = XMLMapper.Factory.create();

        final XMLElementReader<Universe> reader = new UniverseReader();
        mapper.registerRootElement(new QName(NS, UNIVERSE), reader);
        XMLStreamReader universeReader = XMLInputFactory.newInstance().createXMLStreamReader(input);
        mapper.parseDocument(universe, universeReader);
        universeReader.close();
        return universe;
    }
}
