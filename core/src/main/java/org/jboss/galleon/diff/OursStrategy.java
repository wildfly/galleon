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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.jboss.galleon.MessageWriter;
import org.jboss.galleon.util.IoUtils;

/**
 *
 * @author Emmanuel Hugonnet (c) 2017 Red Hat, inc.
 */
public class OursStrategy extends FileSystemMerge {

    public OursStrategy(MessageWriter messageWriter, Path stagedInstallation, Path customizedInstallation) {
        super(messageWriter, stagedInstallation, customizedInstallation);
    }

    @Override
    public void executeUpdate(FileSystemDiffResult result) throws IOException {
        for (Path deletedPath : result.getDeletedFiles()) {
            messageWriter.verbose("File %s has been removed", resolveStagePath(deletedPath));
            IoUtils.recursiveDelete(resolveStagePath(deletedPath));
        }
        for (Path addedPath : result.getAddedFiles()) {
            Path target = resolveStagePath(addedPath);
            Path src = resolveOriginPath(addedPath);
            try {
                if (Files.isDirectory(src)) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    Files.copy(src, target);
                }
                messageWriter.verbose("File %s has been copied to %s", src, target);
            } catch (IOException ioex) {
                throw new RuntimeException("Couldn't copy file " + src, ioex);
            }
        }
        patchFiles(result.getUnifiedDiffs());
    }

    @Override
    public void patchFailure(Path path) {
        Path target = resolveStagePath(path);
        Path src = resolveOriginPath(path);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(src, target, StandardCopyOption.REPLACE_EXISTING);
            messageWriter.verbose("Because of patch failure: file %s has been copied to %s", src, target);
        } catch (IOException ioex) {
            throw new RuntimeException("Couldn't copy file " + src, ioex);
        }
    }
}
