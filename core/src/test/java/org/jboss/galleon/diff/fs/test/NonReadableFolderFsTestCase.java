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
package org.jboss.galleon.diff.fs.test;

import org.assertj.core.api.Assertions;
import org.jboss.galleon.BaseErrors;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.diff.FsDiff;
import org.jboss.galleon.diff.FsEntryFactory;
import org.jboss.galleon.util.IoUtils;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class NonReadableFolderFsTestCase {

    private Path testFile;
    private Path root;
    private Path original;
    private Path other;

    @Before
    public void init() throws Exception {
        root = IoUtils.createRandomTmpDir();
        Files.createDirectories(original = root.resolve("original"));
        Files.createDirectories(other = root.resolve("other"));
    }

    @After
    public void tearDown() {
        testFile.toFile().setReadable(true);
        IoUtils.recursiveDelete(root);
    }

    @Test
    public void testNewUnreadableFolderIsIgnored() throws Exception {
        testFile = createFile(other, "a/d.txt", "d");
        Assume.assumeTrue("Skipping test as the filesystem doesn't allow to set non-readble folders",
                testFile.toFile().setReadable(false));

        final FsEntryFactory factory = FsEntryFactory.getInstance();
        final FsDiff diff = FsDiff.diff(factory.forPath(original), factory.forPath(other));
        Assertions.assertThat(diff.getAddedPaths())
                .containsOnly("a/");
    }

    @Test
    public void existingUnreadableFolderIsIgnored() throws Exception {
        createFile(original, "a/d.txt", "d");

        testFile = createFile(other, "a/d.txt", "d");
        Assume.assumeTrue("Skipping test as the filesystem doesn't allow to set non-readble folders",
                testFile.toFile().setReadable(false));

        final FsEntryFactory factory = FsEntryFactory.getInstance();
        Assertions.assertThatThrownBy(()->FsDiff.diff(factory.forPath(original), factory.forPath(other)))
                .isInstanceOf(ProvisioningException.class)
                .hasMessageContaining(BaseErrors.readDirectory(testFile));
    }

    protected Path createFile(Path context, String relativePath, String content) throws IOException {
        if(relativePath.charAt(0) == '/') {
            relativePath = relativePath.substring(1);
        }
        final Path target = context.resolve(relativePath);
        Files.createDirectories(target.getParent());
        try(BufferedWriter writer = Files.newBufferedWriter(target)) {
            writer.write(content);
        }
        return target;
    }
}
