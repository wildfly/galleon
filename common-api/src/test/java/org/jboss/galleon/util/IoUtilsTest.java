/*
 * Copyright 2016-2024 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.util;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class IoUtilsTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    @Test
    public void copySymlinkDirectory() throws Exception {
        final Path source = temporaryFolder.newFolder("source").toPath();
        final Path targetParent = temporaryFolder.newFolder("target-parent").toPath();

        final Path sourceFile = source.resolve("test.txt");
        final Path target = targetParent.resolve("target");
        Files.createDirectory(target);
        final Path link = Files.createSymbolicLink(targetParent.resolve("link"), target.toAbsolutePath());

        Files.writeString(sourceFile, "test text");

        IoUtils.copy(source, link);
    }

    @Test
    public void copyFileIntoSymlinkDirectory() throws Exception {
        final Path source = temporaryFolder.newFolder("source").toPath();
        final Path targetParent = temporaryFolder.newFolder("target-parent").toPath();

        final Path sourceFile = source.resolve("test.txt");
        final Path target = targetParent.resolve("target");
        Files.createDirectory(target);
        final Path link = Files.createSymbolicLink(targetParent.resolve("link"), target.toAbsolutePath());

        Files.writeString(sourceFile, "test text");

        IoUtils.copy(sourceFile, link.resolve("test.txt"));
    }

    @Test
    public void copySubFolderIntoSymlinkDirectory() throws Exception {
        final Path source = temporaryFolder.newFolder("source").toPath();
        final Path targetParent = temporaryFolder.newFolder("target-parent").toPath();

        final Path sourceFile = source.resolve("sub").resolve("test.txt");
        final Path target = targetParent.resolve("target");
        Files.createDirectory(target);
        final Path link = Files.createSymbolicLink(targetParent.resolve("link"), target.toAbsolutePath());

        Files.createDirectory(source.resolve("sub"));
        Files.writeString(sourceFile, "test text");

        IoUtils.copy(sourceFile, link.resolve("sub").resolve("test.txt"));
    }

}