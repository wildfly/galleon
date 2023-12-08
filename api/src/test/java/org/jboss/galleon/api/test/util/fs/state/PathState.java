/*
 * Copyright 2016-2023 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.api.test.util.fs.state;

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
