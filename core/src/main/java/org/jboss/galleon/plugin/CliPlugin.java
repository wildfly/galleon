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
package org.jboss.galleon.plugin;

import java.io.IOException;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.runtime.PackageRuntime;

/**
 * Plugin for CLI
 *
 * @author jdenise@redhat.com
 */
public interface CliPlugin extends ProvisioningPlugin {

    interface CustomPackageContent {
        String getInfo();
    }

    /**
     * Retrieve package custom content.
     *
     * @param pkg The package.
     * @return null if no custom content.
     * @throws ProvisioningDescriptionException  in case handling failed
     * @throws ProvisioningException  in case handling failed
     * @throws IOException  in case handling failed
     */
    CustomPackageContent handlePackageContent(PackageRuntime pkg)
            throws ProvisioningException, ProvisioningDescriptionException, IOException;
}
