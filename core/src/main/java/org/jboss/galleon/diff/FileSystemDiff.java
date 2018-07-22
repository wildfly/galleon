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
package org.jboss.galleon.diff;

import static org.jboss.galleon.util.IoUtils.listContents;

import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.ProviderException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.galleon.MessageWriter;
import org.jboss.galleon.util.PathFilter;

import java.util.Set;

import difflib.DiffUtils;
import difflib.Patch;

/**
 *
 * @author Emmanuel Hugonnet (c) 2017 Red Hat, inc.
 */
public class FileSystemDiff {
    private final Path stagedInstallation;
    private final Path customizedInstallation;
    protected final MessageWriter messageWriter;

    public FileSystemDiff(MessageWriter messageWriter, Path stagedInstallation, Path customizedInstallation) {
        this.stagedInstallation = stagedInstallation;
        this.customizedInstallation = customizedInstallation;
        this.messageWriter = messageWriter;
    }

    public ProvisioningDiffResult diff() throws ProviderException {
        return diff(PathFilter.DEFAULT);
    }

    public ProvisioningDiffResult diff(final PathFilter filter) throws ProviderException {
        try {
            final Map<Path, String> stagged = listContents(stagedInstallation, filter);
            final Map<Path, String> custom = listContents(customizedInstallation, filter);
            Changes changes = listChanges(stagged, custom);
            return new ProvisioningDiffResult(
                    listDeletedFiles(stagged, custom),
                    listAddedFiles(stagged, custom),
                    changes.modifiedBinaryFiles,
                    changes.unifiedDiff);
        } catch (IOException ioex) {
            throw new ProviderException(ioex);
        }
    }

    private Set<Path> listDeletedFiles(final Map<Path, String> stagged, final Map<Path, String> custom) {
        Set<Path> deletedFiles = new HashSet<>();
        for (Path path : stagged.keySet()) {
            if (!custom.containsKey(path)) {
                Path staggedPath = resolveStagePath(path);
                if (Files.exists(staggedPath)) {
                    if (!Files.isDirectory(staggedPath) || !Files.exists(resolveOriginPath(path))) {
                        deletedFiles.add(path);
                    }
                }
            }
        }
        return deletedFiles;
    }

    private Set<Path> listAddedFiles(final Map<Path, String> stagged, final Map<Path, String> custom) {
        Set<Path> addedFiles = new HashSet<>();
        for (Path path : custom.keySet()) {
            if (!stagged.containsKey(path)) {
                addedFiles.add(path);
            }
        }
        return addedFiles;
    }

    private Path resolveStagePath(Path path) {
        return stagedInstallation.resolve(path);
    }

    private Path resolveOriginPath(Path path) {
        return customizedInstallation.resolve(path);
    }

        private Changes listChanges(final Map<Path, String> stagged, final Map<Path, String> custom) throws IOException {
            Changes changes = new Changes();
            for (Entry<Path, String> entry : stagged.entrySet()) {
                Path path = entry.getKey();
                if (custom.containsKey(path) && !custom.get(path).equals(entry.getValue())) {
                    try {
                        changes.unifiedDiff.put(path, extractUnifiedDiff(resolveStagePath(path), resolveOriginPath(path)));
                    } catch(MalformedInputException ex) {
                        changes.modifiedBinaryFiles.add(path);
                    }
                }
            }
            return changes;
        }

        private List<String> extractUnifiedDiff(Path revised, Path original) throws IOException {
            final List<String> revisedLines = Files.readAllLines(revised, StandardCharsets.UTF_8);
            final List<String> originalLines = Files.readAllLines(original, StandardCharsets.UTF_8);
            Patch<String> patch = DiffUtils.diff(revisedLines, originalLines);
            return DiffUtils.generateUnifiedDiff(revised.toString(), original.toString(), revisedLines, patch, 0);
        }

    private static class Changes {

        private final Map<Path, List<String>> unifiedDiff;
        private final Set<Path> modifiedBinaryFiles;

        private Changes() {
            this.unifiedDiff = new HashMap<>();
            this.modifiedBinaryFiles = new HashSet<>();
        }
    }
}
