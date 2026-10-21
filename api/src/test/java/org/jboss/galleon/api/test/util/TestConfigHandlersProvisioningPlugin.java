/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.api.test.util;

import java.util.ServiceLoader;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.plugin.InstallPlugin;
import org.jboss.galleon.plugin.ProvisionedConfigHandler;
import org.jboss.galleon.runtime.ProvisioningRuntime;
import org.jboss.galleon.state.ProvisionedConfig;

/**
 * @author Alexey Loubyansky
 *
 */
public class TestConfigHandlersProvisioningPlugin implements InstallPlugin {

    @Override
    public void postInstall(ProvisioningRuntime ctx) throws ProvisioningException {
        if (ctx.hasConfigs()) {
            final ServiceLoader<ProvisionedConfigHandler> handlers = ServiceLoader.load(ProvisionedConfigHandler.class);
            for (ProvisionedConfigHandler handler : handlers) {
                for (ProvisionedConfig config : ctx.getConfigs()) {
                    config.handle(handler);
                }
            }
        }
    }
}
