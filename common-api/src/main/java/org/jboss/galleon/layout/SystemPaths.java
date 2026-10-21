/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.layout;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jboss.galleon.util.PathsUtils;

public class SystemPaths {
    public static final String SYSTEM_PATHS_FILE = "systempaths.txt";

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

    public static SystemPaths load(Path installationDir) throws IOException {
        Path p = PathsUtils.getProvisionedStateDir(installationDir).resolve(SYSTEM_PATHS_FILE);
        Set<Path> paths = new HashSet<>();
        if (Files.exists(p)) {
            List<String> lst = Files.readAllLines(p);
            for(String path : lst) {
                paths.add(Paths.get(path));
            }
        }
        return new SystemPaths(paths);
    }

    public void store(Path installDir) throws IOException {
        Path pathsFile = PathsUtils.getProvisionedStateDir(installDir).resolve(SYSTEM_PATHS_FILE);
        StringBuilder builder = new StringBuilder();
        for (Path p : getPaths()) {
            builder.append(p.toString()).append(System.lineSeparator());
        }
        Files.write(pathsFile, builder.toString().getBytes());
    }

    public Set<Path> getPaths() {
        return Collections.unmodifiableSet(paths);
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
