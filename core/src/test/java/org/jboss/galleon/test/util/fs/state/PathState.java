/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.test.util.fs.state;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Assert;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class PathState {

    public abstract static class Builder {

        protected final String name;

        protected Builder(String name) {
            this.name = name;
        }

        public abstract PathState build();
    }

    protected final String name;

    protected PathState(String name) {
        this.name = name;
    }

    protected void assertState(Path parent) {
        if (name != null) {
            final Path path = parent.resolve(name);
            if (!Files.exists(path)) {
                Assert.fail("Path doesn't exist: " + path);
            }
            doAssertState(path);
        } else {
            doAssertState(parent);
        }
    }

    protected abstract void doAssertState(Path path);
}
