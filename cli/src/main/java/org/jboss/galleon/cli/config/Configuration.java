/*
 * Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.config.mvn.MavenConfig.MavenChangeListener;
import org.jboss.galleon.util.IoUtils;
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

    // Monthly cleanup, could be made configurable.
    public static final long CACHE_PERIOD = 1000 * 60 * 43200L;

    private static final String GALLON_DIR_NAME = ".galleon-cli";
    private static final String CONFIG_FILE_NAME = "cli-config";
    private static final String LAYOUT_DIR_NAME = "layout";
    private static final String LAYOUT_CONTENT_FILE_NAME = LAYOUT_DIR_NAME + ".properties";
    private static final String CACHE_DIR_NAME = "cache";

    private static final String HISTORY_FILE_NAME = "cli-history";

    private final Path cacheDir;
    private final Path layoutCacheDir;
    private final Path layoutContentFile;
    private final File historyFile;
    private final MavenConfig maven;

    private Configuration() throws ProvisioningException {
        Path galleonDir = getConfigDirectory();
        if (Files.exists(galleonDir) && !Files.isDirectory(galleonDir)) {
            throw new ProvisioningException(CliErrors.invalidConfigDirectory(galleonDir));
        }
        historyFile = galleonDir.resolve(HISTORY_FILE_NAME).toFile();
        cacheDir = galleonDir.resolve(CACHE_DIR_NAME);
        layoutCacheDir = cacheDir.resolve(LAYOUT_DIR_NAME);
        layoutContentFile = cacheDir.resolve(LAYOUT_CONTENT_FILE_NAME);
        maven = new MavenConfig();
        maven.addListener(this);
    }

    public MavenConfig getMavenConfig() {
        return maven;
    }

    public Path getLayoutCache() {
        return layoutCacheDir;
    }

    public Properties getLayoutCacheContent() throws IOException {
        Properties props = new Properties();
        if (Files.exists(layoutContentFile)) {
            try (FileInputStream stream = new FileInputStream(layoutContentFile.toFile())) {
                props.load(stream);
            }
        }
        return props;
    }

    public void storeLayoutCacheContent(Properties props) throws IOException {
        if (props.isEmpty()) {
            Files.deleteIfExists(layoutContentFile);
            return;
        }
        if (!Files.exists(layoutContentFile)) {
            Files.createDirectories(layoutContentFile.getParent());
            Files.createFile(layoutContentFile);
        }
        try (FileOutputStream stream = new FileOutputStream(layoutContentFile.toFile())) {
            props.store(stream, null);
        }
    }

    @Override
    public void configurationChanged(MavenConfig config) throws XMLStreamException, IOException {
        needRewrite();
    }

    public void needRewrite() throws XMLStreamException, IOException {
        Path configFile = getConfigFile();
        Files.deleteIfExists(configFile);
        try (BufferedWriter bw = Files.newBufferedWriter(configFile,
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

    public static Configuration parse() throws ProvisioningException {
        return parse(Collections.emptyMap());
    }

    public static Configuration parse(Map<String, String> options) throws ProvisioningException {
        Configuration config = new Configuration();
        Path configFile = getConfigFile();
        if (Files.exists(configFile)) {
            try (BufferedReader reader = Files.newBufferedReader(configFile)) {
                XmlParsers.parse(reader, config);
            } catch (IOException | XMLStreamException e) {
                throw new ProvisioningException(Errors.parseXml(configFile), e);
            }
        }
        return config;
    }

    private static Path getConfigDirectory() {
        Path galleonDir = new File(System.getProperty("user.home")
                + File.separator + GALLON_DIR_NAME).toPath();
        if (!Files.exists(galleonDir)) {
            galleonDir.toFile().mkdir();
        }
        return galleonDir;
    }

    private static Path getConfigFile() {
        return getConfigDirectory().resolve(CONFIG_FILE_NAME);
    }

    public void clearLayoutCache() throws IOException {
        try {
            IoUtils.recursiveDelete(getLayoutCache());
        } finally {
            Files.deleteIfExists(layoutContentFile);
        }
    }
}
