/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.test.util.fs.state;

import java.nio.file.Files;
import java.nio.file.Path;

import org.jboss.galleon.test.util.TestUtils;
import org.junit.Assert;

/**
 *
 * @author Alexey Loubyansky
 */
public class FileContentState extends PathState {

    public static class FileContentBuilder extends PathState.Builder {

        private final String content;

        private FileContentBuilder(String name, String content) {
            super(name);
            this.content = content;
        }

        public FileContentState build() {
            return new FileContentState(name, content);
        }
    }

    public static FileContentBuilder builder(String name, String content) {
        return new FileContentBuilder(name, content);
    }

    private final String content;

    protected FileContentState(String name, String content) {
        super(name);
        this.content = content;
    }

    /* (non-Javadoc)
     * @see org.jboss.galleon.test.util.pathstate.PathState#doAssertState(java.nio.file.Path)
     */
    @Override
    protected void doAssertState(Path path) {
        if(Files.isDirectory(path)) {
            Assert.fail("Path is a not directory: " + path);
        }
        if(!content.equals(TestUtils.read(path))) {
            Assert.fail(path + " expected to contain " + content + " instead of " + TestUtils.read(path));
        }
    }
}
