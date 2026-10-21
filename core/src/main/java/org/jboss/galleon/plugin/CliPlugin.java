/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
