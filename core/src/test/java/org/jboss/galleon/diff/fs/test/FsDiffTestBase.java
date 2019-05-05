/*
 * Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
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
public abstract class FsDiffTestBase {

    protected Path root;
    protected Path original;
    protected Path other;
    protected Path ctxPath;

    @Before
    public void init() throws Exception {
        root = IoUtils.createRandomTmpDir();
        Files.createDirectories(original = root.resolve("original"));
        Files.createDirectories(other = root.resolve("other"));
    }

    @After
    public void cleanup() throws Exception {
        IoUtils.recursiveDelete(root);
    }

    protected void createFile(String relativePath, String content) throws IOException {
        if(relativePath.charAt(0) == '/') {
            relativePath = relativePath.substring(1);
        }
        final Path target = ctxPath.resolve(relativePath);
        Files.createDirectories(target.getParent());
        try(BufferedWriter writer = Files.newBufferedWriter(target)) {
            writer.write(content);
        }
    }

    protected void mkdir(String relativePath) throws IOException {
        if(relativePath.charAt(0) == '/') {
            relativePath = relativePath.substring(1);
        }
        final Path target = ctxPath.resolve(relativePath);
        Files.createDirectories(target);
    }

    protected final void initOriginal() throws IOException {
        ctxPath = original;
        initOriginalDir();
    }

    protected abstract void initOriginalDir() throws IOException;

    protected final void initOther() throws IOException {
        ctxPath = other;
        initOtherDir();
    }

    protected abstract void initOtherDir() throws IOException;

    protected void initFactory(FsEntryFactory factory) {
    }

    protected abstract void assertDiff(FsDiff diff) throws Exception;

    @Test
    public void test() throws Exception {
        initOriginal();
        initOther();
        final FsEntryFactory factory = FsEntryFactory.getInstance();
        initFactory(factory);
        assertDiff(FsDiff.diff(factory.forPath(original), factory.forPath(other)));

    }

    protected void assertIdentical(FsEntry expected, FsEntry actual) throws ProvisioningException {
        final FsDiff diff = FsDiff.diff(expected, actual);
        if(diff.isEmpty()) {
            return;
        }
        fail(diff.toString());
    }
}
