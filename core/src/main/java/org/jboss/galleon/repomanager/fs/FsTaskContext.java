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
package org.jboss.galleon.repomanager.fs;

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
