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



import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.jboss.galleon.MessageWriter;

import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;

/**
 *
 * @author Emmanuel Hugonnet (c) 2017 Red Hat, inc.
 */
public abstract class FileSystemMerge {
    private final Path stagedInstallation;
    private final Path customizedInstallation;
    protected final MessageWriter messageWriter;

    protected FileSystemMerge(MessageWriter messageWriter, Path stagedInstallation, Path customizedInstallation) {
        this.stagedInstallation = stagedInstallation;
        this.customizedInstallation = customizedInstallation;
        this.messageWriter = messageWriter;
    }

    protected Path resolveStagePath(Path path) {
        return stagedInstallation.resolve(path);
    }

    protected Path resolveOriginPath(Path path) {
        return customizedInstallation.resolve(path);
    }

    public abstract void executeUpdate(ProvisioningDiffResult result) throws IOException;

    public abstract void patchFailure(Path path);

    protected void patchFiles(Map<Path, List<String>> changes) {
        for(Entry<Path, List<String>> change : changes.entrySet()) {
            patchFile(change.getKey(), change.getValue());
        }
    }

    protected void patchFile(Path path, List<String> diff) {
        Path file = resolveStagePath(path);
        String unifiedDiff = "";
        if(messageWriter.isVerboseEnabled()) {
            unifiedDiff = diff.stream().collect(Collectors.joining(System.lineSeparator()));
        }
        try {
            Patch<String> patch = DiffUtils.parseUnifiedDiff(diff);
            List<String> updatedLines = DiffUtils.patch(Files.readAllLines(file, StandardCharsets.UTF_8), patch);
            Files.write(file, updatedLines);
        } catch (PatchFailedException | IOException ex) {
            messageWriter.verbose(ex, "Couldn't patch file %s with %s because %s", file, unifiedDiff, ex.getMessage());
            patchFailure(path);
        }
    }

    public static class Factory {

        public static FileSystemMerge getInstance(Strategy strategy, MessageWriter messageWriter, Path stagedInstallation, Path customizedInstallation) {
            switch (strategy) {
                case THEIRS:
                    return new TheirsStrategy(messageWriter, stagedInstallation, customizedInstallation);
                case INTERACTIVE:
                case OURS:
                default:
                    return new OursStrategy(messageWriter, stagedInstallation, customizedInstallation);
            }
        }
    }
}
