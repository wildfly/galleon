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

package org.jboss.galleon.diff.fs.test;

import java.io.IOException;
import org.jboss.galleon.diff.FsEntry;
import org.jboss.galleon.diff.FsEntryFactory;

/**
 *
 * @author Alexey Loubyansky
 */
public class BasicWildcardFilteringTestCase extends FsEntriesTestBase {

    @Override
    protected void initFs() throws IOException {
        createFile("a/b/file1.glnew", "file1");
        createFile("a/b/file2.txt", "file1");
        mkdir("a/b/f");
        createFile("a/b/c/file1.txt", "file1");
        createFile("a/d/file1.glnew", "file1");
        createFile("e/file1.txt", "file1");
        createFile("file1.glnew", "file1");
        mkdir("g");
    }

    @Override
    protected void initFactory(FsEntryFactory factory) {
        factory.filter("*.glnew");
        factory.filter("*e");
    }

    @Override
    protected void assertRootEntry(FsEntry rootEntry) throws Exception {
        final FsEntry expectedRoot = new FsEntry(null, root);
        final FsEntry a = new FsEntry(expectedRoot, root.resolve("a"));
        final FsEntry b = new FsEntry(a, root.resolve("a/b"));
        new FsEntry(b, root.resolve("a/b/file2.txt"));
        new FsEntry(b, root.resolve("a/b/c"));
        new FsEntry(b, root.resolve("a/b/f"));
        new FsEntry(a, root.resolve("a/d"));
        new FsEntry(expectedRoot, root.resolve("g"));

        assertIdentical(expectedRoot, rootEntry);
    }
}
