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
package org.jboss.galleon.api.test.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.jboss.galleon.util.IoUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class TestUtils {

    private static void mkdirs(final Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create dir " + dir);
        }
    }

    public static Path mkRandomTmpDir() {
        return IoUtils.createTmpDir(UUID.randomUUID().toString());
    }

    public static void rm(Path p) {
        IoUtils.recursiveDelete(p);
    }

    public static Path mkdirs(Path dir, String... name) {
        if(!Files.exists(dir)) {
            mkdirs(dir);
        }
        Path p = dir;
        for(String n : name) {
            p = p.resolve(n);
            try {
                Files.createDirectory(p);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to create dir " + dir);
            }
        }
        return p;
    }

    public static String read(Path p) {
        final StringWriter strWriter = new StringWriter();
        try(BufferedReader reader = Files.newBufferedReader(p);
                BufferedWriter writer = new BufferedWriter(strWriter)) {
            String line = reader.readLine();
            if (line != null) {
                writer.write(line);
                line = reader.readLine();
                while (line != null) {
                    writer.newLine();
                    writer.write(line);
                    line = reader.readLine();
                }
                writer.flush();
                return strWriter.getBuffer().toString();
            }
            return null;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read " + p, e);
        }
    }
}
