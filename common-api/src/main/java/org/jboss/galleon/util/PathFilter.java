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
package org.jboss.galleon.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * A filter for paths
 * @author Emmanuel Hugonnet (c) 2017 Red Hat, inc.
 */
@FunctionalInterface
public interface PathFilter {

    /**
     * Tests whether or not the specified path should be
     * included in a path list.
     *
     * @param  path  The path to be tested
     * @return  <code>true</code> if and only if <code>pathn</code>
     *          should be included
     */
    boolean accept(Path path);

    PathFilter DEFAULT = new PathFilter() {
        @Override
        public boolean accept(Path path) {
            return true;
        }};

    class PathFilterImpl implements PathFilter {

        private final List<Pattern> directories;
        private final List<Pattern> files;

        private PathFilterImpl(List<Pattern> directories, List<Pattern> files) {
            this.directories = directories;
            this.files = files;
        }

        @Override
        public boolean accept(Path path) {
            final String current = PathsUtils.toForwardSlashSeparator(path.toString());
            boolean acceptDirectory = !this.directories.stream().anyMatch(new Predicate<Pattern>() {
                @Override
                public boolean test(Pattern pattern) {
                    return pattern.matcher(current).matches();
                }
            });
            if(Files.isDirectory(path)) {
                return acceptDirectory;
            }
            final String fileName = path.getFileName().toString();
            return acceptDirectory && !this.files.stream().anyMatch(new Predicate<Pattern>() {
                @Override
                public boolean test(Pattern pattern) {
                    return pattern.matcher(fileName).matches();
                }
            });
        }
    }

   class Builder {

        private final List<Pattern> directories = new ArrayList<>();
        private final List<Pattern> files = new ArrayList<>();

        private Builder() {
        }

        ;
        public static Builder instance() {
            return new Builder();
        }

        public Builder addDirectories(String... directoryNames) {
            for (String directory : directoryNames) {
                this.directories.add(wildcardToJavaRegexp(directory));
            }
            return this;
        }

        public Builder addFiles(String... fileNames) {
            for (String file : fileNames) {
                this.files.add(wildcardToJavaRegexp(file));
            }
            return this;
        }

        public static Pattern wildcardToJavaRegexp(String expr) {
            if (expr == null) {
                throw new IllegalArgumentException("expr is null");
            }
            String regex = PathsUtils.toForwardSlashSeparator(expr).replaceAll("([(){}\\[\\].+^$])", "\\\\$1"); // escape regex characters
            regex = regex.replaceAll("\\*", ".*"); // replace * with .*
            regex = regex.replaceAll("\\?", "."); // replace ? with .
            return Pattern.compile(regex);
        }

        public PathFilter build() {
            return new PathFilterImpl(directories, files);
        }
    }
}
