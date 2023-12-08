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
package org.jboss.galleon.cli;

import org.jboss.galleon.cli.config.mvn.MavenSettings;
import java.util.List;
import org.eclipse.aether.RepositoryListener;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.jboss.galleon.cli.config.mvn.ArtifactException;
import org.jboss.galleon.cli.config.mvn.MavenConfig;
import org.jboss.galleon.maven.plugin.util.AbstractMavenArtifactRepositoryManager;
import org.jboss.galleon.universe.maven.MavenUniverseException;

/**
 *
 * @author Alexey Loubyansky
 */
public class CliMavenArtifactRepositoryManager extends AbstractMavenArtifactRepositoryManager {

    public static final String DEFAULT_REPOSITORY_TYPE = "default";
    private final MavenConfig config;
    private final RepositoryListener listener;

    private MavenSettings mavenSettings;
    private boolean commandStarted;

    CliMavenArtifactRepositoryManager(MavenConfig config, RepositoryListener listener) {
        super(Util.newRepositorySystem());
        this.config = config;
        this.listener = listener;
    }

    @Override
    public RepositorySystemSession getSession() throws MavenUniverseException {
        try {
            return getSettings().getSession();
        } catch (ArtifactException ex) {
            throw new MavenUniverseException(ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    public List<RemoteRepository> getRepositories() throws MavenUniverseException {
        try {
            return getSettings().getRepositories();
        } catch (ArtifactException ex) {
            throw new MavenUniverseException(ex.getLocalizedMessage(), ex);
        }
    }

    void commandStart() {
        commandStarted = true;
        mavenSettings = null;
    }

    void commandEnd() {
        commandStarted = false;
    }

    private MavenSettings getSettings() throws ArtifactException {
        if (commandStarted) { // reuse settings.
            if (mavenSettings == null) {
                mavenSettings = config.buildSettings(getRepositorySystem(), listener);
            }
        } else {
            mavenSettings = config.buildSettings(getRepositorySystem(), listener);
        }
        return mavenSettings;
    }
}
