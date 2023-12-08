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
