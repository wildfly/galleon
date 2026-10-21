/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.plugin;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.runtime.ProvisioningRuntime;

/**
 * Provisioning plug-in can be referenced from a feature-pack configuration.
 *
 * An implementation of this interface can execute certain product-specific
 * tasks to complete the installation provisioned by the core mechanism.
 *
 * Examples of such tasks could be:
 * - generate product configuration files;
 * - set file permissions;
 * - create/remove directory structures;
 * - etc.
 *
 * @author Alexey Loubyansky
 */
public interface InstallPlugin extends ProvisioningPlugin {

    /**
     * Called after the installation dependencies and configurations have been
     * successfully resolved but before any package was installed.
     *
     * @param runtime  provisioning runtime
     * @throws ProvisioningException  in case the plugin failed  to process the callback
     */
    default void preInstall(ProvisioningRuntime runtime) throws ProvisioningException {}

    /**
     * Called after all the packages have been installed.
     *
     * @param runtime  provisioning runtime
     * @throws ProvisioningException  in case the plugin failed  to process the callback
     */
    default void postInstall(ProvisioningRuntime runtime) throws ProvisioningException {}
}
