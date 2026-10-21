/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
