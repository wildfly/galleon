/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.maven.plugin.util;

import java.util.List;

import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.jboss.galleon.universe.maven.MavenUniverseException;

/**
 *
 * @author Emmanuel Hugonnet (c) 2017 Red Hat, inc.
 * @author Alexey Loubyansky
 */
public class MavenArtifactRepositoryManager extends AbstractMavenArtifactRepositoryManager {

    private final RepositorySystemSession session;
    private final List<RemoteRepository> repositories;

    /**
     * Creates an instance that only will resolve artifacts using the Maven local repository.
     *
     * @param repoSystem The repository system instance, must not be {@code null}.
     * @param repoSession The repository session, must not be {@code null}.
     */
    public MavenArtifactRepositoryManager(final RepositorySystem repoSystem, final RepositorySystemSession repoSession){
        super(repoSystem);
        this.session = repoSession;
        this.repositories = null;
    }

    /**
     * Creates an instance that will use a list of remote repositories where to find an artifact if the artifact is not in
     * the local Maven repository.
     *
     * @param repoSystem The repository system instance, must not be {@code null}.
     * @param repoSession The repository session, must not be {@code null}.
     * @param repositories The list of remote repositories where to find the artifact if it is not in the local Maven repository.
     */
    public MavenArtifactRepositoryManager(final RepositorySystem repoSystem, final RepositorySystemSession repoSession, final List<RemoteRepository> repositories){
        super(repoSystem);
        this.session = repoSession;
        this.repositories = repositories;
    }

    /**
     * Creates an instance that will only resolve artifacts from the Maven local repository, while still providing
     * the remote repositories to the resolver. Artifacts in the local repository are tracked with the id of
     * the remote repository they were downloaded from and are only considered available if a repository with
     * the same id is part of the request. Passing the repositories with an offline session keeps such
     * artifacts available without accessing the network.
     *
     * @param repoSystem The repository system instance, must not be {@code null}.
     * @param repoSession The repository session, must not be {@code null}.
     * @param repositories The list of remote repositories the artifacts in the local Maven repository may originate from.
     * @return an offline repository manager
     */
    public static MavenArtifactRepositoryManager offline(final RepositorySystem repoSystem, final RepositorySystemSession repoSession, final List<RemoteRepository> repositories) {
        final DefaultRepositorySystemSession offlineSession = new DefaultRepositorySystemSession(repoSession);
        offlineSession.setOffline(true);
        return new MavenArtifactRepositoryManager(repoSystem, offlineSession, repositories);
    }

    @Override
    protected RepositorySystemSession getSession() throws MavenUniverseException {
        return session;
    }

    @Override
    protected List<RemoteRepository> getRepositories() throws MavenUniverseException {
        return repositories;
    }
}
