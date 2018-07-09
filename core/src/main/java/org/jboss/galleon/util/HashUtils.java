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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Alexey Loubyansky
 */
public class HashUtils {

    private static final char[] TABLE = "0123456789abcdef".toCharArray();

    private static final MessageDigest DIGEST;
    static {
        try {
            DIGEST = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] hashPath(Path path) throws IOException {
        synchronized (DIGEST) {
            DIGEST.reset();
            updateDigest(DIGEST, path);
            return DIGEST.digest();
        }
    }

    public static String hashFile(Path path) throws IOException {
        synchronized (DIGEST) {
            DIGEST.reset();
            updateDigest(DIGEST, path);
            return bytesToHexString(DIGEST.digest());
        }
    }

    public static String hash(String content) throws IOException {
        synchronized (DIGEST) {
            DIGEST.reset();
            DIGEST.update(content.getBytes(StandardCharsets.UTF_8));
            return bytesToHexString(DIGEST.digest());
        }
    }

    private static void updateDigest(MessageDigest digest, Path path) throws IOException {
        if(Files.isDirectory(path)) {
            try(DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                final Map<String, Path> sortedChildren = new TreeMap<String, Path>();
                for(Path p : stream) {
                    sortedChildren.put(p.getFileName().toString(), p);
                }
                for (Path child : sortedChildren.values()) {
                    updateDigest(digest, child);
                }
            }
        } else {
            try (BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(path))){
                byte[] bytes = new byte[8192];
                int read;
                while ((read = bis.read(bytes)) > -1) {
                    digest.update(bytes, 0, read);
                }
            }
        }
    }

    public static byte[] hashJar(Path jarFile, boolean ignoreManifest) throws IOException {
        synchronized (DIGEST) {
            DIGEST.reset();
            try (FileSystem zipfs = ZipUtils.newFileSystem(jarFile)) {
                for (Path zipRoot : zipfs.getRootDirectories()) {
                    final Map<String, Path> sortedChildren = new TreeMap<String, Path>();
                    try(DirectoryStream<Path> stream = Files.newDirectoryStream(zipRoot)) {
                        for(Path p : stream) {
                            final String fileName = p.getFileName().toString();
                            if(ignoreManifest && fileName.equals("META-INF/")) {
                                continue;
                            }
                            sortedChildren.put(fileName, p);
                        }
                    }
                    for (Path child : sortedChildren.values()) {
                        updateDigest(DIGEST, child);
                    }
                }
            }
            return DIGEST.digest();
        }
    }

    /**
     * Convert a byte array into a hex string.
     *
     * @param bytes the bytes
     * @return the string
     */
    public static String bytesToHexString(final byte[] bytes) {
        final StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            builder.append(TABLE[b >> 4 & 0x0f]).append(TABLE[b & 0x0f]);
        }
        return builder.toString();
    }

    /**
     * Convert a hex string into a byte[].
     *
     * @param s the string
     * @return the bytes
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len >> 1];
        for (int i = 0, j = 0; j < len; i++) {
            int x = Character.digit(s.charAt(j), 16) << 4;
            j++;
            x = x | Character.digit(s.charAt(j), 16);
            j++;
            data[i] = (byte) (x & 0xFF);
        }
        return data;
    }
}
