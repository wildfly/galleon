/*
 * Copyright 2016-2022 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.layout;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SystemPaths {

    private final Set<Path> paths;

    public SystemPaths(Set<Path> paths) {
        Set<Path> tmp = new HashSet<>();
        for (Path path : paths) {
            if (path.isAbsolute()) {
                throw new IllegalArgumentException("Provided system-path must be relative to the installation. " + path);
            }
            if (tmp.isEmpty()) {
                tmp.add(path.normalize());
                continue;
            }
            // check if the new path is already covered by higher level path
            if (tmp.stream().noneMatch(p->path.normalize().startsWith(p))) {
                // check if the new path covers any already added paths
                tmp.removeAll(tmp.stream().filter(p->p.startsWith(path.normalize())).collect(Collectors.toSet()));
                tmp.add(path.normalize());
            }
        }
        this.paths = tmp;
    }

    public boolean isSystemPath(Path path) {
        if (path.isAbsolute()) {
            throw new IllegalArgumentException("Provided system-path must be relative to the installation." + path);
        }

        for (Path p : paths) {
            if (path.normalize().startsWith(p)) {
                return true;
            }
        }
        return false;
    }
}
