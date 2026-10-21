/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.state;


import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.api.config.GalleonProvisionedConfig;
import org.jboss.galleon.plugin.ProvisionedConfigHandler;

/**
 *
 * @author Alexey Loubyansky
 */
public interface ProvisionedConfig extends GalleonProvisionedConfig {

    void handle(ProvisionedConfigHandler handler) throws ProvisioningException;
}
