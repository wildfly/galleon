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
package org.jboss.galleon.maven.plugin.util;

import java.util.List;

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

    @Override
    protected RepositorySystemSession getSession() throws MavenUniverseException {
        return session;
    }

    @Override
    protected List<RemoteRepository> getRepositories() throws MavenUniverseException {
        return repositories;
    }
}
