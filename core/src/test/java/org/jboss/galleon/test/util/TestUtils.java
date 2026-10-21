/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.test.util;

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
