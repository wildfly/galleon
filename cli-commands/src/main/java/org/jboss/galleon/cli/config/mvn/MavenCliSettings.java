/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.config.mvn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.aether.RepositoryListener;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.jboss.galleon.cli.Util;

/**
 *
 * @author jdenise@redhat.com
 */
class MavenCliSettings implements MavenSettings {

    private final List<RemoteRepository> repositories;
    private final RepositorySystemSession session;

    MavenCliSettings(MavenConfig config, RepositorySystem repoSystem, RepositoryListener listener) throws ArtifactException {
        repositories = Collections.unmodifiableList(buildRepositories(config));
        MavenProxySelector proxySelector = null;
        session = Util.newRepositorySession(repoSystem, config.getLocalRepository(),
                listener, proxySelector, config.isOffline());
    }

    @Override
    public List<RemoteRepository> getRepositories() {
        return repositories;
    }

    @Override
    public RepositorySystemSession getSession() {
        return session;
    }

    private List<RemoteRepository> buildRepositories(MavenConfig config) throws ArtifactException {
        List<RemoteRepository> repos = new ArrayList<>();
        for (MavenRemoteRepository repo : config.getRemoteRepositories()) {
            repos.add(config.buildRemoteRepository(repo, null, null));
        }
        return repos;
    }

}
