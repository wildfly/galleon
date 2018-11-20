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
