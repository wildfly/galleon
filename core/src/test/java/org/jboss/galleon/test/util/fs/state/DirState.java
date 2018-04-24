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
package org.jboss.galleon.test.util.fs.state;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.galleon.test.util.TestUtils;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.PathsUtils;
import org.junit.Assert;

/**
 *
 * @author Alexey Loubyansky
 */
public class DirState extends PathState {

    public static final String SEPARATOR = "/";

    public static class DirBuilder extends PathState.Builder {

        private Map<String, PathState.Builder> childStates = Collections.emptyMap();

        private DirBuilder(String name) {
            super(name);
        }

        private DirBuilder addState(String name, PathState.Builder state) {
            childStates = CollectionUtils.put(childStates, name, state);
            return this;
        }

        public DirBuilder addDir(String relativePath) {
            DirState.DirBuilder dirBuilder = this;
            final String[] parts = relativePath.split(SEPARATOR);
            int i = 0;
            while (i < parts.length) {
                dirBuilder = dirBuilder.dirBuilder(parts[i++]);
            }
            return this;
        }

        public DirBuilder addFile(String relativePath, String content) {
            DirState.DirBuilder dirBuilder = this;
            final String[] parts = relativePath.split(SEPARATOR);
            int i = 0;
            if(parts.length > 1) {
                while(i < parts.length - 1) {
                    dirBuilder = dirBuilder.dirBuilder(parts[i++]);
                }
            }
            dirBuilder.addState(parts[i], FileContentState.builder(parts[i], content));
            return this;
        }

        private DirBuilder dirBuilder(String name) {
            final PathState.Builder builder = childStates.get(name);
            if(builder != null) {
                return (DirBuilder)builder;
            }
            final DirBuilder dirBuilder = DirState.builder(name);
            addState(name, dirBuilder);
            return dirBuilder;
        }

        public DirBuilder skip(String relativePath) {
            DirState.DirBuilder dirBuilder = this;
            final String[] parts = relativePath.split(SEPARATOR);
            int i = 0;
            if(parts.length > 1) {
                while(i < parts.length - 1) {
                    dirBuilder = dirBuilder.dirBuilder(parts[i++]);
                }
            }
            dirBuilder.addState(parts[i], SkipPathState.builder(parts[i]));
            return this;
        }

        public DirBuilder clear() {
            childStates = Collections.emptyMap();
            return this;
        }

        public DirBuilder init(Path path) throws Exception {
            Files.walkFileTree(path, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
                    new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                            if(path != dir) {
                                addDir(PathsUtils.toForwardSlashSeparator(path.relativize(dir).toString()));
                            }
                            return FileVisitResult.CONTINUE;
                        }
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                            throws IOException {
                            addFile(PathsUtils.toForwardSlashSeparator(path.relativize(file).toString()), TestUtils.read(file));
                            return FileVisitResult.CONTINUE;
                        }
                    });
            return this;
        }

        @Override
        public DirState build() {
            final Map<String, PathState> states = new HashMap<>(childStates.size());
            for(Map.Entry<String, PathState.Builder> entry : childStates.entrySet()) {
                states.put(entry.getKey(), entry.getValue().build());
            }
            return new DirState(name, Collections.unmodifiableMap(states));
        }
    }

    public static DirBuilder rootBuilder() {
        return builder(null);
    }

    public static DirBuilder builder(String name) {
        return new DirBuilder(name);
    }

    private final Map<String, PathState> childStates;

    private DirState(String name, Map<String, PathState> states) {
        super(name);
        this.childStates = states;
    }

    @Override
    public void assertState(Path root) {
        if(name == null) {
            doAssertState(root);
        } else {
            super.assertState(root);
        }
    }

    @Override
    protected void doAssertState(Path path) {
        if(!Files.isDirectory(path)) {
            Assert.fail("Path is a directory: " + path);
        }
        Set<String> actualPaths = new HashSet<>();
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for(Path child : stream) {
                actualPaths.add(child.getFileName().toString());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read directory " + path, e);
        }
        for (Map.Entry<String, PathState> entry : childStates.entrySet()) {
            entry.getValue().assertState(path);
            actualPaths.remove(entry.getKey());
        }
        if (!actualPaths.isEmpty()) {
            Assert.fail("Dir " + path + " does not contain " + actualPaths);
        }
    }
}
