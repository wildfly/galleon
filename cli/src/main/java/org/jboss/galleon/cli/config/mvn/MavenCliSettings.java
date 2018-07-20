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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.aether.RepositoryListener;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import static org.eclipse.aether.repository.RepositoryPolicy.CHECKSUM_POLICY_WARN;
import static org.eclipse.aether.repository.RepositoryPolicy.UPDATE_POLICY_NEVER;
import org.jboss.galleon.ArtifactException;
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
                listener, proxySelector, false);
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
            // The default policy for both snapshots and releases is to update daily.
            // This shows very bad performance. Artifact are downloaded in an unpredictable way.
            // We are never updating by default.
            // XXX jfdenise, could be made configurable.
            RemoteRepository.Builder builder = new RemoteRepository.Builder(repo.getName(),
                    repo.getType(), repo.getUrl());
            builder.setSnapshotPolicy(new RepositoryPolicy(true, UPDATE_POLICY_NEVER, CHECKSUM_POLICY_WARN));
            builder.setReleasePolicy(new RepositoryPolicy(true, UPDATE_POLICY_NEVER, CHECKSUM_POLICY_WARN));
            repos.add(builder.build());
        }
        return repos;
    }

}
