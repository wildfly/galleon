/*
 * Copyright 2016-2022 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.layout;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SystemPathsTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void testSinglePath() throws Exception {
        final SystemPaths systemPaths = new SystemPaths(of(Path.of("protected", "test1.txt")));

        assertTrue(systemPaths.isSystemPath(Path.of("protected", "test1.txt")));
    }

    @Test
    public void testPathInProtectedFolder() throws Exception {
        final SystemPaths systemPaths = new SystemPaths(of(Path.of("protected")));

        assertTrue(systemPaths.isSystemPath(Path.of("protected", "test1.txt")));
    }

    @Test
    public void testSubFolderInProtectedFolder() throws Exception {
        final SystemPaths systemPaths = new SystemPaths(of(Path.of("protected")));

        assertTrue(systemPaths.isSystemPath(Path.of("protected", "sub", "test1.txt")));
    }

    @Test
    public void testPathOutsideProtectedFolder() throws Exception {
        final SystemPaths systemPaths = new SystemPaths(of(Path.of("protected")));

        assertFalse(systemPaths.isSystemPath(Path.of("test1.txt")));
    }

    @Test
    public void testMultipleFiles() throws Exception {
        final SystemPaths systemPaths = new SystemPaths(of(Path.of("protected", "test1.txt"), Path.of("protected", "test2.txt")));

        assertTrue(systemPaths.isSystemPath(Path.of("protected", "test1.txt")));
    }

    @Test
    public void testMultipleOverlappingFiles() throws Exception {
        final SystemPaths systemPaths = new SystemPaths(of(Path.of("protected", "test1.txt"), Path.of("protected")));

        assertTrue(systemPaths.isSystemPath(Path.of("protected", "test1.txt")));
        assertTrue(systemPaths.isSystemPath(Path.of("protected", "test2.txt")));
    }

    private Set<Path> of(Path... paths) {
        return new HashSet<>(Arrays.asList(paths));
    }
}