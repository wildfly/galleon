/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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