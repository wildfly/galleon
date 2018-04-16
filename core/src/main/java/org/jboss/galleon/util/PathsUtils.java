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

import java.nio.file.Path;

import org.jboss.galleon.Constants;

/**
 * Utility class to resolve directories and files that represent
 * the provisioned state of the installation.
 *
 * @author Alexey Loubyansky
 */
public class PathsUtils {

    public static Path getProvisionedStateDir(Path installationDir) {
        return installationDir.resolve(Constants.PROVISIONED_STATE_DIR);
    }

    public static Path getProvisioningXml(Path installationDir) {
        return getProvisionedStateDir(installationDir).resolve(Constants.PROVISIONING_XML);
    }

    public static Path getProvisionedStateXml(Path installationDir) {
        return getProvisionedStateDir(installationDir).resolve(Constants.PROVISIONED_STATE_XML);
    }
}
