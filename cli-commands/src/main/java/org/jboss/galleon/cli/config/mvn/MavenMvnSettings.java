/*
 * Copyright 2016-2023 Red Hat, Inc. and/or its affiliates
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

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Repository;
import org.apache.maven.settings.RepositoryPolicy;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuilder;
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingResult;
import org.eclipse.aether.RepositoryListener;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.jboss.galleon.cli.CliLogging;
import static org.jboss.galleon.cli.CliMavenArtifactRepositoryManager.DEFAULT_REPOSITORY_TYPE;
import org.jboss.galleon.cli.Util;

/**
 *
 * @author jdenise@redhat.com
 */
class MavenMvnSettings implements MavenSettings {

    private static final String EXTERNAL = "external:";
    private static final String ALL = "*";
    private static final String NOT = "!";
    private final List<RemoteRepository> repositories;
    private final RepositorySystemSession session;
    private final MavenConfig config;
    private final org.eclipse.aether.repository.Proxy proxy;
    private final MavenProxySelector proxySelector;
    MavenMvnSettings(MavenConfig config, RepositorySystem repoSystem, RepositoryListener listener) throws ArtifactException {
        this.config = config;
        Settings settings = buildMavenSettings(config.getSettings());
        Proxy proxy = settings.getActiveProxy();
        if (proxy != null) {
            MavenProxySelector.Builder builder = new MavenProxySelector.Builder(proxy.getHost(), proxy.getPort(), proxy.getProtocol());
            builder.setPassword(proxy.getPassword());
            builder.setUserName(proxy.getUsername());
            if (proxy.getNonProxyHosts() != null) {
                String[] hosts = proxy.getNonProxyHosts().split("\\|");
                builder.addNonProxyHosts(Arrays.asList(hosts));
            }
            proxySelector = builder.build();
            Authentication auth = null;
            if (proxy.getPassword() != null && proxy.getUsername() != null) {
                auth = new AuthenticationBuilder().addUsername(proxy.getUsername()).addPassword(proxy.getPassword()).build();
            }
            this.proxy = new org.eclipse.aether.repository.Proxy(proxy.getProtocol(), proxy.getHost(), proxy.getPort(), auth);
        } else {
            this.proxy = null;
            proxySelector = null;
        }
        try {
            repositories = Collections.unmodifiableList(buildRemoteRepositories(settings));
        } catch (MalformedURLException ex) {
            throw new ArtifactException(ex.getMessage(), ex);
        }
        session = Util.newRepositorySession(repoSystem,
                settings.getLocalRepository() == null ? config.getLocalRepository() : Paths.get(settings.getLocalRepository()),
                listener, proxySelector, settings.isOffline());
    }

    @Override
    public List<RemoteRepository> getRepositories() {
        return repositories;
    }

    @Override
    public RepositorySystemSession getSession() {
        return session;
    }

    private static Settings buildMavenSettings(Path settingsPath) throws ArtifactException {
        SettingsBuildingRequest settingsBuildingRequest = new DefaultSettingsBuildingRequest();
        settingsBuildingRequest.setSystemProperties(System.getProperties());
        settingsBuildingRequest.setUserSettingsFile(settingsPath.toFile());
        SettingsBuildingResult settingsBuildingResult;
        DefaultSettingsBuilderFactory mvnSettingBuilderFactory = new DefaultSettingsBuilderFactory();
        DefaultSettingsBuilder settingsBuilder = mvnSettingBuilderFactory.newInstance();
        try {
            settingsBuildingResult = settingsBuilder.build(settingsBuildingRequest);
        } catch (SettingsBuildingException ex) {
            throw new ArtifactException(ex.getLocalizedMessage());
        }

        return settingsBuildingResult.getEffectiveSettings();
    }

    private List<RemoteRepository> buildRemoteRepositories(Settings settings) throws ArtifactException, MalformedURLException {
        Map<String, Profile> profiles = settings.getProfilesAsMap();
        Map<String, RemoteRepository> repos = new LinkedHashMap<>();
        List<RemoteRepository> repositories = new ArrayList<>();
        Set<String> urls = new HashSet<>();
        for (Profile profile : settings.getProfiles()) {
            if ((profile.getActivation() != null && profile.getActivation().isActiveByDefault()) || settings.getActiveProfiles().contains(profile.getId())) {
                List<Repository> mavenRepositories = profile.getRepositories();
                for (Repository repo : mavenRepositories) {
                    repos.put(repo.getId(), buildRepository(repo.getId(), repo.getLayout(),
                            repo.getUrl(), settings, repo.getReleases(), repo.getSnapshots(), null));
                    urls.add(repo.getUrl());
                }
            }
        }
        List<RemoteRepository> defaultRepositories = config.getMissingDefaultRepositories(urls, proxySelector, proxy);
        for (RemoteRepository r : defaultRepositories) {
            repos.put(r.getId(), r);
        }
        repositories.addAll(handleMirroring(settings, repos));
        // Then the remaining repositories
        for (Entry<String, RemoteRepository> entry : repos.entrySet()) {
            repositories.add(entry.getValue());
        }
        return repositories;
    }

    private List<RemoteRepository> handleMirroring(Settings settings, Map<String, RemoteRepository> repos) throws MalformedURLException {
        List<RemoteRepository> repositories = new ArrayList<>();
        // Mirrors are hidding actual repo.
        for (Mirror mirror : settings.getMirrors()) {
            String[] patterns = mirror.getMirrorOf().split(",");
            List<RemoteRepository> mirrored = new ArrayList<>();
            boolean all = false;
            List<String> excluded = new ArrayList<>();
            for (String p : patterns) {
                p = p.trim();
                if (ALL.equals(p)) {
                    all = true;
                } else if (p.startsWith(NOT)) {
                    excluded.add(p.substring(NOT.length()));
                }
            }
            if (all) {
                // Add all except the excluded ones.
                List<String> safeKeys = new ArrayList<>(repos.keySet());
                for (String k : safeKeys) {
                    if (!excluded.contains(k)) {
                        mirrored.add(repos.remove(k));
                    }
                }
            } else {
                for (String p : patterns) {
                    p = p.trim();
                    if (p.startsWith(EXTERNAL)) {
                        CliLogging.log.warn("external:* mirroring is not supported, "
                                + "skipping configuration item");
                        continue;
                    }
                    RemoteRepository m = repos.get(p);
                    if (m != null) {
                        // Remove from the initial map, it is hidden by mirror
                        mirrored.add(repos.remove(p));
                    }
                }
            }
            if (!mirrored.isEmpty()) { // We have an active mirror
                repositories.add(buildRepository(mirror.getId(),
                        mirror.getLayout(), mirror.getUrl(), settings, null, null, mirrored));
            }
        }
        return repositories;
    }

    private RemoteRepository buildRepository(String id, String type, String url,
            Settings settings, RepositoryPolicy rp, RepositoryPolicy sp, List<RemoteRepository> mirrored) throws MalformedURLException {
        RemoteRepository.Builder builder = new RemoteRepository.Builder(id,
                type == null ? DEFAULT_REPOSITORY_TYPE : type,
                url);
        if (rp != null) {
            org.eclipse.aether.repository.RepositoryPolicy releases
                    = new org.eclipse.aether.repository.RepositoryPolicy(rp.isEnabled(),
                            rp.getUpdatePolicy(), rp.getChecksumPolicy());
            builder.setReleasePolicy(releases);
        }
        if (sp != null) {
            org.eclipse.aether.repository.RepositoryPolicy snapshots
                    = new org.eclipse.aether.repository.RepositoryPolicy(sp.isEnabled(),
                            sp.getUpdatePolicy(), sp.getChecksumPolicy());
            builder.setReleasePolicy(snapshots);
        }
        for (Server server : settings.getServers()) {
            if (server.getId().equals(id)) {
                if (server.getUsername() != null) {
                    AuthenticationBuilder authBuilder = new AuthenticationBuilder();
                    authBuilder.addPassword(server.getPassword());
                    authBuilder.addUsername(server.getUsername());
                    builder.setAuthentication(authBuilder.build());
                } else if (server.getPrivateKey() != null) {
                    AuthenticationBuilder authBuilder = new AuthenticationBuilder();
                    authBuilder.addPrivateKey(server.getPrivateKey(), server.getPassphrase());
                    builder.setAuthentication(authBuilder.build());
                }
            }
        }
        if (mirrored != null) {
            builder.setMirroredRepositories(mirrored);
        }
        if (proxySelector != null && proxySelector.proxyFor(new URL(url).getHost())) {
            builder.setProxy(proxy);
        }
        return builder.build();
    }
}
