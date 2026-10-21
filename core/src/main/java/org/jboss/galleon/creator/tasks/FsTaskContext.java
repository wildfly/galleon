/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.creator.tasks;

import java.nio.file.Path;

import org.jboss.galleon.Constants;

/**
 *
 * @author Alexey Loubyansky
 */
public class FsTaskContext {

    public static class Builder {
        private Path targetRoot;

        private Builder() {
        }

        public Builder setTargetRoot(Path targetRoot) {
            this.targetRoot = targetRoot;
            return this;
        }

        public FsTaskContext build() {
            return new FsTaskContext(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final Path targetRoot;

    private FsTaskContext(Builder builder) {
        this.targetRoot = builder.targetRoot;
    }

    public Path getTargetRoot(boolean content) {
        if(content) {
            return targetRoot.resolve(Constants.CONTENT);
        }
        return targetRoot;
    }
}
