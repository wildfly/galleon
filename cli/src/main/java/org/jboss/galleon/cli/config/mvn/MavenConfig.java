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
import static org.eclipse.aether.repository.RepositoryPolicy.UPDATE_POLICY_ALWAYS;
import static org.eclipse.aether.repository.RepositoryPolicy.UPDATE_POLICY_DAILY;
import static org.eclipse.aether.repository.RepositoryPolicy.UPDATE_POLICY_INTERVAL;
import static org.eclipse.aether.repository.RepositoryPolicy.UPDATE_POLICY_NEVER;
import org.jboss.galleon.ProvisioningException;
import static org.jboss.galleon.cli.CliMavenArtifactRepositoryManager.DEFAULT_REPOSITORY_TYPE;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.util.PropertyUtils;
import org.jboss.galleon.xml.util.FormattingXmlStreamWriter;

/**
 *
 * @author jdenise@redhat.com
 */
public class MavenConfig {

    static final String DEFAULT_SNAPSHOT_UPDATE_POLICY = UPDATE_POLICY_NEVER;
    static final String DEFAULT_RELEASE_UPDATE_POLICY = UPDATE_POLICY_DAILY;

    static final boolean DEFAULT_ENABLE_SNAPSHOT = false;
    static final boolean DEFAULT_ENABLE_RELEASE = true;

    private static final List<String> VALID_UPDATE_POLICIES;

    static {
        List<String> policies = new ArrayList<>();
        policies.add(UPDATE_POLICY_NEVER);
        policies.add(UPDATE_POLICY_ALWAYS);
        policies.add(UPDATE_POLICY_DAILY);
        policies.add(UPDATE_POLICY_INTERVAL + ":");
        VALID_UPDATE_POLICIES = Collections.unmodifiableList(policies);
    }

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
    private String defaultSnapshotPolicy = DEFAULT_SNAPSHOT_UPDATE_POLICY;
    private String defaultReleasePolicy = DEFAULT_RELEASE_UPDATE_POLICY;

    private boolean defaultEnableSnapshot = DEFAULT_ENABLE_SNAPSHOT;
    private boolean defaultEnableRelease = DEFAULT_ENABLE_RELEASE;

    private boolean disableAdvertise;
    private boolean offline;

    void disableAdvertise() {
        disableAdvertise = true;
    }

    void enableAdvertise() {
        disableAdvertise = false;
    }

    public boolean isSnapshotEnabled() {
        return defaultEnableSnapshot;
    }

    public boolean isReleaseEnabled() {
        return defaultEnableRelease;
    }

    public void enableSnapshot(Boolean enable) throws XMLStreamException, IOException {
        defaultEnableSnapshot = enable;
        advertise();
    }

    public void resetSnapshot() throws XMLStreamException, IOException {
        defaultEnableSnapshot = DEFAULT_ENABLE_SNAPSHOT;
        advertise();
    }

    public void enableRelease(Boolean enable) throws XMLStreamException, IOException {
        defaultEnableRelease = enable;
        advertise();
    }

    public void resetRelease() throws XMLStreamException, IOException {
        defaultEnableRelease = DEFAULT_ENABLE_RELEASE;
        advertise();
    }

    public String getDefaultSnapshotPolicy() {
        return defaultSnapshotPolicy;
    }

    public String getDefaultReleasePolicy() {
        return defaultReleasePolicy;
    }

    public void setDefaultSnapshotPolicy(String policy) throws ProvisioningException,
            XMLStreamException, IOException {
        validatePolicy(policy);
        defaultSnapshotPolicy = policy;
        advertise();
    }

    public void resetDefaultSnapshotPolicy() throws ProvisioningException,
            XMLStreamException, IOException {
        defaultSnapshotPolicy = DEFAULT_SNAPSHOT_UPDATE_POLICY;
        advertise();
    }

    public void setDefaultReleasePolicy(String policy) throws ProvisioningException,
            XMLStreamException, IOException {
        validatePolicy(policy);
        defaultReleasePolicy = policy;
        advertise();
    }

    public void resetDefaultReleasePolicy() throws ProvisioningException,
            XMLStreamException, IOException {
        defaultReleasePolicy = DEFAULT_RELEASE_UPDATE_POLICY;
        advertise();
    }

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
        if (!disableAdvertise) {
            for (MavenChangeListener listener : listeners) {
                listener.configurationChanged(this);
            }
        }
    }

    public Collection<MavenRemoteRepository> getRemoteRepositories() {
        if (repositories.isEmpty()) {
            return DEFAULT_REPOSITORIES;
        } else {
            List<MavenRemoteRepository> repos = new ArrayList<>();
            repos.addAll(DEFAULT_REPOSITORIES);
            repos.addAll(repositories.values());
            return Collections.unmodifiableCollection(repos);
        }
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
        if (!defaultReleasePolicy.equals(DEFAULT_RELEASE_UPDATE_POLICY)) {
            writer.writeStartElement(MavenConfigXml.RELEASE_UPDATE_POLICY);
            writer.writeCharacters(defaultReleasePolicy);
            writer.writeEndElement();
        }
        if (!defaultSnapshotPolicy.equals(DEFAULT_SNAPSHOT_UPDATE_POLICY)) {
            writer.writeStartElement(MavenConfigXml.SNAPSHOT_UPDATE_POLICY);
            writer.writeCharacters(defaultSnapshotPolicy);
            writer.writeEndElement();
        }
        if (defaultEnableSnapshot != DEFAULT_ENABLE_SNAPSHOT) {
            writer.writeStartElement(MavenConfigXml.ENABLE_SNAPSHOT);
            writer.writeCharacters(Boolean.toString(defaultEnableSnapshot));
            writer.writeEndElement();
        }
        if (defaultEnableRelease != DEFAULT_ENABLE_RELEASE) {
            writer.writeStartElement(MavenConfigXml.ENABLE_RELEASE);
            writer.writeCharacters(Boolean.toString(defaultEnableRelease));
            writer.writeEndElement();
        }
        if (settings != null) {
            writer.writeStartElement(MavenConfigXml.SETTINGS);
            writer.writeCharacters(settings.toAbsolutePath().toString());
            writer.writeEndElement();
        }
        if (offline) {
            writer.writeStartElement(MavenConfigXml.OFFLINE);
            writer.writeCharacters(Boolean.toString(offline));
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
                if (repo.getReleaseUpdatePolicy() != null) {
                    writer.writeAttribute(MavenConfigXml.RELEASE_UPDATE_POLICY, repo.getReleaseUpdatePolicy());
                }
                if (repo.getSnapshotUpdatePolicy() != null) {
                    writer.writeAttribute(MavenConfigXml.SNAPSHOT_UPDATE_POLICY, repo.getSnapshotUpdatePolicy());
                }
                if (repo.getEnableRelease() != null) {
                    writer.writeAttribute(MavenConfigXml.ENABLE_RELEASE, Boolean.toString(repo.getEnableRelease()));
                }
                if (repo.getEnableSnapshot() != null) {
                    writer.writeAttribute(MavenConfigXml.ENABLE_SNAPSHOT, Boolean.toString(repo.getEnableSnapshot()));
                }
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
        localRepository = path.normalize();
        advertise();
    }

    public void resetLocalRepository() throws XMLStreamException, IOException {
        localRepository = null;
        advertise();
    }

    public void setSettings(Path path) throws XMLStreamException, IOException {
        settings = path.normalize();
        advertise();
    }

    public void resetSettings() throws XMLStreamException, IOException {
        settings = null;
        advertise();
    }

    public void enableOffline(Boolean offline) throws XMLStreamException, IOException {
        this.offline = offline;
        advertise();
    }

    public void resetOffline() throws XMLStreamException, IOException {
        this.offline = false;
        advertise();
    }

    public boolean isOffline() {
        return offline;
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

    static void validatePolicy(String policy) throws ProvisioningException {
        // A null policy means that it will get replaced by default policy.
        if (policy == null) {
            return;
        }
        String radical = UPDATE_POLICY_INTERVAL + ":";
        if (policy.startsWith(radical)) {
            String minutes = policy.substring(radical.length());
            try {
                Integer.parseInt(minutes);
            } catch (NumberFormatException ex) {
                throw new ProvisioningException(CliErrors.invalidMavenUpdatePolicy(policy));
            }
            return;
        }

        if (!VALID_UPDATE_POLICIES.contains(policy)) {
                throw new ProvisioningException(CliErrors.invalidMavenUpdatePolicy(policy));
        }
    }

    public static List<String> getUpdatePolicies() {
        return VALID_UPDATE_POLICIES;
    }
}
