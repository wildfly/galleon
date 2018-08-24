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
package org.jboss.galleon.cli.config.mvn;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.stream.XMLStreamException;
import org.eclipse.aether.RepositoryListener;
import org.eclipse.aether.RepositorySystem;
import org.jboss.galleon.ProvisioningException;
import static org.jboss.galleon.cli.CliMavenArtifactRepositoryManager.DEFAULT_REPOSITORY_TYPE;
import org.jboss.galleon.util.PropertyUtils;
import org.jboss.galleon.xml.util.FormattingXmlStreamWriter;

/**
 *
 * @author jdenise@redhat.com
 */
public class MavenConfig {

    private static final List<MavenRemoteRepository> DEFAULT_REPOSITORIES = new ArrayList<>();
    static {
        DEFAULT_REPOSITORIES.add(new MavenRemoteRepository("jboss-public-repository-group",
                DEFAULT_REPOSITORY_TYPE, "http://repository.jboss.org/nexus/content/groups/public/"));
        DEFAULT_REPOSITORIES.add(new MavenRemoteRepository("maven-central", DEFAULT_REPOSITORY_TYPE,
                "http://repo1.maven.org/maven2/"));
    }
    public interface MavenChangeListener {
        void configurationChanged(MavenConfig config) throws XMLStreamException, IOException;
    }

    private final Map<String, MavenRemoteRepository> repositories = new HashMap<>();
    private Path localRepository;
    private final List<MavenChangeListener> listeners = new ArrayList<>();
    private Path settings;

    public Path getSettings() {
        return settings;
    }

    public void addListener(MavenChangeListener listener) {
        listeners.add(listener);
    }

    static Path getDefaultMavenRepositoryPath() {
        String repoPath = PropertyUtils.getSystemProperty("maven.repo.path");
        if (repoPath == null) {
            repoPath = new StringBuilder(PropertyUtils.getSystemProperty("user.home")).append(File.separatorChar)
                    .append(".m2").append(File.separatorChar)
                    .append("repository")
                    .toString();
        }
        return Paths.get(repoPath);
    }

    public void addRemoteRepository(MavenRemoteRepository repo) throws XMLStreamException, IOException, ProvisioningException {
        repositories.put(repo.getName(), repo);
        advertise();
    }

    private void advertise() throws XMLStreamException, IOException {
        for (MavenChangeListener listener : listeners) {
            listener.configurationChanged(this);
        }
    }

    public Collection<MavenRemoteRepository> getRemoteRepositories() {
        return repositories.isEmpty() ? DEFAULT_REPOSITORIES : Collections.unmodifiableCollection(repositories.values());
    }

    public Set<String> getRemoteRepositoryNames() {
        return Collections.unmodifiableSet(repositories.keySet());
    }

    public void removeRemoteRepository(String name) throws XMLStreamException, IOException, ProvisioningException {
        MavenRemoteRepository rep = repositories.remove(name);
        if (rep == null) {
            throw new ProvisioningException("Repository " + name + " doesn't exist");
        }
        advertise();
    }

    public void write(FormattingXmlStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(MavenConfigXml.MAVEN);
        if (localRepository != null) {
            writer.writeStartElement(MavenConfigXml.LOCAL_REPOSITORY);
            writer.writeCharacters(localRepository.toAbsolutePath().toString());
            writer.writeEndElement();
        }
        if (settings != null) {
            writer.writeStartElement(MavenConfigXml.SETTINGS);
            writer.writeCharacters(settings.toAbsolutePath().toString());
            writer.writeEndElement();
        }
        if (repositories.isEmpty()) {
            writer.writeEmptyElement(MavenConfigXml.REPOSITORIES);
        } else {
            writer.writeStartElement(MavenConfigXml.REPOSITORIES);

            for (MavenRemoteRepository repo : repositories.values()) {
                writer.writeStartElement(MavenConfigXml.REPOSITORY);
                writer.writeAttribute(MavenConfigXml.NAME, repo.getName());
                writer.writeAttribute(MavenConfigXml.TYPE, repo.getType());
                writer.writeCharacters(repo.getUrl());
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    public Path getLocalRepository() {
        return localRepository == null ? getDefaultMavenRepositoryPath() : localRepository;
    }

    public void setLocalRepository(Path path) throws XMLStreamException, IOException {
        localRepository = path == null ? null : path.normalize();
        advertise();
    }

    public void setSettings(Path path) throws XMLStreamException, IOException {
        settings = path == null ? null : path.normalize();
        advertise();
    }

    private boolean reuseMavenSettings() {
        return getSettings() != null;
    }

    public MavenSettings buildSettings(RepositorySystem repoSystem,
            RepositoryListener listener) throws ArtifactException {
        if (reuseMavenSettings()) {
            return new MavenMvnSettings(this, repoSystem, listener);
        } else {
            return new MavenCliSettings(this, repoSystem, listener);
        }
    }
}
