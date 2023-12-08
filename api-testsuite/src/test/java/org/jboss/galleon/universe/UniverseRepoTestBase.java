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
package org.jboss.galleon.universe;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jboss.galleon.universe.maven.repo.SimplisticMavenRepoManager;
import org.jboss.galleon.util.IoUtils;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Alexey Loubyansky
 */
public class UniverseRepoTestBase {

    private Path workDir;
    protected Path repoHome;
    protected SimplisticMavenRepoManager repo;

    @Before
    public void init() throws Exception {
        workDir = Files.createTempDirectory("galleon-test");
        repoHome = mkdirs("repo");
        repo = initResolver(repoHome);
        doInit();
    }

    protected Path mkdirs(String... el) {
        Path p = workDir;
        for(String e : el) {
            p = p.resolve(e);
        }
        try {
            Files.createDirectories(p);
        } catch (IOException e1) {
            throw new IllegalStateException("Failed to create dirs " + p);
        }
        return p;
    }

    protected SimplisticMavenRepoManager initResolver(Path repoHome) {
        return SimplisticMavenRepoManager.getInstance(repoHome);
    }

    protected void doInit() throws Exception {
    }

    @After
    public void cleanup() throws Exception {
        try {
            doCleanUp();
        } finally {
            repo = null;
            IoUtils.recursiveDelete(workDir);
        }
    }

    protected void doCleanUp() {
    }
}
