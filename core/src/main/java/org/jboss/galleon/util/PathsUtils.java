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

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import org.jboss.galleon.Constants;
import org.jboss.galleon.Errors;
import org.jboss.galleon.MessageWriter;
import org.jboss.galleon.ProvisioningException;

/**
 * Utility class to resolve directories and files that represent
 * the provisioned state of the installation.
 *
 * @author Alexey Loubyansky
 */
public class PathsUtils {

    public static void assertInstallationDir(Path path) throws ProvisioningException {
        if (!Files.exists(path)) {
            return;
        }
        if (!Files.isDirectory(path)) {
            throw new ProvisioningException(Errors.notADir(path));
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            final Iterator<Path> i = stream.iterator();
            if(!i.hasNext()) {
                return;
            }
            while (i.hasNext()) {
                if (i.next().getFileName().toString().equals(Constants.PROVISIONED_STATE_DIR)) {
                    return;
                }
            }
            throw new ProvisioningException(Errors.homeDirNotUsable(path));
        } catch (IOException e) {
            throw new ProvisioningException(Errors.readDirectory(path));
        }
    }

    public static Path getProvisionedStateDir(Path home) {
        return home.resolve(Constants.PROVISIONED_STATE_DIR);
    }

    public static Path getProvisioningXml(Path home) {
        return getProvisionedStateDir(home).resolve(Constants.PROVISIONING_XML);
    }

    public static Path getProvisionedStateXml(Path home) {
        return getProvisionedStateDir(home).resolve(Constants.PROVISIONED_STATE_XML);
    }

    public static Path getStateHistoryDir(Path home) {
        return getProvisionedStateDir(home).resolve(Constants.HISTORY);
    }

    public static Path getStateHistoryFile(Path home) {
        return getStateHistoryDir(home).resolve(Constants.HISTORY_LIST);
    }

    public static String toForwardSlashSeparator(String path) {
        if(File.separatorChar == '/') {
            return path;
        }
        return path.replace(File.separatorChar, '/');
    }

    public static void replaceDist(Path stagedDir, Path home, boolean asUndo, MessageWriter log) throws ProvisioningException {
        log.verbose("Moving the provisioned installation from the staged directory to %s", home);
        // copy from the staged to the target installation directory
        if (Files.exists(home)) {
            if(asUndo) {
                StateHistoryUtils.removeLastUndoConfig(home, stagedDir, log);
            } else {
                StateHistoryUtils.addNewUndoConfig(home, stagedDir, log);
            }
            IoUtils.recursiveDelete(home);
        }
        try {
            IoUtils.copy(stagedDir, home);
        } catch (IOException e) {
            throw new ProvisioningException(Errors.copyFile(stagedDir, home));
        }
    }
}
