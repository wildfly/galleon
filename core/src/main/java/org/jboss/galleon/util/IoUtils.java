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
package org.jboss.galleon.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 *
 * @author Alexey Loubyansky
 */
public class IoUtils {

    private static final Path TMP_DIR = Paths.get(PropertyUtils.getSystemProperty("java.io.tmpdir"));

    public static Path createTmpDir(String name) {
        final Path dir = TMP_DIR.resolve(name);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create " + dir.toAbsolutePath());
        }
        return dir;
    }

    public static Path createRandomTmpDir() {
        return createTmpDir(UUID.randomUUID().toString());
    }

    public static void recursiveDelete(Path root) {
        if (root == null || !Files.exists(root)) {
            return;
        }
        try {
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException e)
                    throws IOException {
                    if (e == null) {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    } else {
                        // directory iteration failed
                        throw e;
                    }
                }
            });
        } catch (IOException e) {
        }
    }

    public static void copy(Path source, Path target) throws IOException {
        if(Files.isDirectory(source)) {
            Files.createDirectories(target);
        } else {
            Files.createDirectories(target.getParent());
        }
        Files.walkFileTree(source, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                        throws IOException {
                        final Path targetDir = target.resolve(source.relativize(dir));
                        try {
                            Files.copy(dir, targetDir);
                        } catch (FileAlreadyExistsException e) {
                             if (!Files.isDirectory(targetDir)) {
                                 throw e;
                             }
                        }
                        return FileVisitResult.CONTINUE;
                    }
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                        Files.copy(file, target.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                        return FileVisitResult.CONTINUE;
                    }
                });
    }

    public static String readFile(Path file) throws IOException {
        final StringWriter buf = new StringWriter();
        try (BufferedWriter bw = new BufferedWriter(buf)) {
            Files.readAllLines(file).forEach(new Consumer<String>() {
                @Override
                public void accept(String line) {
                    try {
                        bw.newLine();
                        bw.append(line);
                    } catch (IOException ioex) {
                        throw new RuntimeException(ioex);
                    }
                }
            });
        }
        return buf.toString();
    }

    public static void writeFile(Path file, String content) throws IOException {
        Files.write(file, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
    }

     public static Map<Path, String> listContents(Path root, PathFilter filter) throws IOException {
        if (root == null || !Files.exists(root)) {
            return Collections.emptyMap();
        }
        if(Files.isRegularFile(root)) {
            return Collections.singletonMap(root.relativize(root), HashUtils.hashFile(root));
        }
        Map<Path, String> contents = new HashMap<>();
        Files.walkFileTree(root, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (filter.accept(root.relativize(dir))) {
                    String[] files = dir.toFile().list();
                    if (files == null || files.length == 0) {
                        contents.put(root.relativize(dir), HashUtils.hash(root.relativize(dir).toString()));
                    }
                    return FileVisitResult.CONTINUE;
                }
                return FileVisitResult.SKIP_SUBTREE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if(filter.accept(root.relativize(file))) {
                    contents.put(root.relativize(file), HashUtils.hashFile(file));
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
        return contents;
    }
}
