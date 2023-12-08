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
package org.jboss.galleon.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import org.jboss.galleon.Constants;
import org.jboss.galleon.BaseErrors;
import org.jboss.galleon.ProvisioningException;

/**
 * Utility class to resolve directories and files that represent
 * the provisioned state of the installation.
 *
 * @author Alexey Loubyansky
 */
public class PathsUtils {

    public static boolean isNewHome(Path homeDir) throws ProvisioningException {
        if (!Files.exists(homeDir)) {
            return true;
        }
        if (!Files.isDirectory(homeDir)) {
            throw new ProvisioningException(BaseErrors.notADir(homeDir));
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(homeDir)) {
            final Iterator<Path> i = stream.iterator();
            return !i.hasNext();
        } catch (IOException e) {
            throw new ProvisioningException(BaseErrors.readDirectory(homeDir), e);
        }
    }

    public static void assertInstallationDir(Path path) throws ProvisioningException {
        if (!Files.exists(path)) {
            return;
        }
        if (!Files.isDirectory(path)) {
            throw new ProvisioningException(BaseErrors.notADir(path));
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
            throw new ProvisioningException(BaseErrors.homeDirNotUsable(path));
        } catch (IOException e) {
            throw new ProvisioningException(BaseErrors.readDirectory(path), e);
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
}
