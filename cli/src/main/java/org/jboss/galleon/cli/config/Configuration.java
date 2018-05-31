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
package org.jboss.galleon.cli.config;

import org.jboss.galleon.cli.config.mvn.MavenConfig;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.UniverseLocation;
import org.jboss.galleon.cli.config.mvn.MavenConfig.MavenChangeListener;
import org.jboss.galleon.xml.XmlParsers;
import org.jboss.galleon.xml.util.FormattingXmlStreamWriter;

/**
 *
 * @author jdenise@redhat.com
 */
public class Configuration implements MavenChangeListener {

    static {
        new ConfigXmlParser10().plugin(XmlParsers.getInstance());
    }

    private static final String CONFIG_FILE_NAME = ".galleon-cli";

    private static final File DEFAULT_HISTORY_FILE = new File(System.getProperty("user.home"), ".galleon-history");
    private final List<UniverseLocation> universes = new ArrayList<>();
    private File historyFile = DEFAULT_HISTORY_FILE;
    private final MavenConfig maven;

    private Configuration() {
        maven = new MavenConfig();
        maven.addListener(this);
    }

    public MavenConfig getMavenConfig() {
        return maven;
    }

    @Override
    public void configurationChanged(MavenConfig config) throws XMLStreamException, IOException {
        needRewrite();
    }

    private void needRewrite() throws XMLStreamException, IOException {
        Path path = getStoragePath();
        Files.deleteIfExists(path);
        try (BufferedWriter bw = Files.newBufferedWriter(path,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            try (FormattingXmlStreamWriter writer = new FormattingXmlStreamWriter(XMLOutputFactory.newInstance()
                    .createXMLStreamWriter(bw))) {
                writer.writeStartDocument();
                writer.writeStartElement(ConfigXmlParser10.ROOT_1_0.getLocalPart());
                writer.writeDefaultNamespace(ConfigXmlParser10.NAMESPACE_1_0);
                maven.write(writer);
                writer.writeEndElement();
                writer.writeEndDocument();
            }
        }
    }

    public File getHistoryFile() {
        return historyFile;
    }

    public List<UniverseLocation> getUniversesLocations() {
        return Collections.unmodifiableList(universes);
    }

    public static Configuration parse() throws ProvisioningException {
        // For now, no XML config for universe.
        // TODO
        Configuration config = new Configuration();
        config.universes.add(UniverseLocation.DEFAULT);
        Path configFile = getStoragePath();
        if (Files.exists(configFile)) {
            try (BufferedReader reader = Files.newBufferedReader(configFile)) {
                XmlParsers.parse(reader, config);
            } catch (IOException | XMLStreamException e) {
                throw new ProvisioningException(Errors.parseXml(configFile), e);
            }
        }
        return config;
    }

    private static Path getStoragePath() {
        return new File(System.getProperty("user.home") + File.separator + CONFIG_FILE_NAME).toPath();
    }
}
