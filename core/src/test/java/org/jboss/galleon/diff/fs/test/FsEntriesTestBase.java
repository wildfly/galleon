/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.diff.fs.test;

import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.diff.FsDiff;
import org.jboss.galleon.diff.FsEntry;
import org.jboss.galleon.diff.FsEntryFactory;
import org.jboss.galleon.util.IoUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class FsEntriesTestBase {

    protected Path root;

    @Before
    public void init() throws Exception {
        root = IoUtils.createRandomTmpDir();
    }

    @After
    public void cleanup() throws Exception {
        IoUtils.recursiveDelete(root);
    }

    protected void createFile(String relativePath, String content) throws IOException {
        if(relativePath.charAt(0) == '/') {
            relativePath = relativePath.substring(1);
        }
        final Path target = root.resolve(relativePath);
        Files.createDirectories(target.getParent());
        try(BufferedWriter writer = Files.newBufferedWriter(target)) {
            writer.write(content);
        }
    }

    protected void mkdir(String relativePath) throws IOException {
        if(relativePath.charAt(0) == '/') {
            relativePath = relativePath.substring(1);
        }
        final Path target = root.resolve(relativePath);
        Files.createDirectories(target);
    }

    protected void initFs() throws IOException {
    }

    protected void initFactory(FsEntryFactory factory) {
    }

    protected abstract void assertRootEntry(FsEntry rootEntry) throws Exception;

    @Test
    public void test() throws Exception {
        initFs();
        final FsEntryFactory factory = FsEntryFactory.getInstance();
        initFactory(factory);
        assertRootEntry(factory.forPath(root));
    }

    protected void assertIdentical(FsEntry expected, FsEntry actual) throws ProvisioningException {
        final FsDiff diff = FsDiff.diff(expected, actual);
        if(diff.isEmpty()) {
            return;
        }
        fail(diff.toString());
    }
}
