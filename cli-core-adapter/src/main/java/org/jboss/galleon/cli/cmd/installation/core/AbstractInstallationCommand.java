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
package org.jboss.galleon.cli.cmd.installation.core;

import java.io.IOException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.core.GalleonCoreExecution;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.cli.model.FeatureContainers;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.layout.FeaturePackLayout;
import org.jboss.galleon.layout.ProvisioningLayout;
import org.jboss.galleon.runtime.ProvisioningRuntime;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractInstallationCommand<T extends org.jboss.galleon.cli.cmd.installation.AbstractInstallationCommand> implements GalleonCoreExecution<ProvisioningSession, T> {

    protected ProvisioningManager getManager(ProvisioningSession session, T command) throws ProvisioningException {
        return session.newProvisioningManager(command.getInstallationDirectory(session.getPmSession().getAeshContext()), false);
    }

    public FeatureContainer getFeatureContainer(ProvisioningSession session, ProvisioningLayout<FeaturePackLayout> layout, T command) throws ProvisioningException,
            CommandExecutionException, IOException {
        FeatureContainer container;
        ProvisioningManager manager = getManager(session, command);

        if (manager.getProvisionedState() == null) {
            throw new CommandExecutionException("Specified directory doesn't contain an installation");
        }
        if (layout == null) {
            ProvisioningConfig config = manager.getProvisioningConfig();
            try (ProvisioningRuntime runtime = manager.getRuntime(config)) {
                container = FeatureContainers.fromProvisioningRuntime(session, runtime);
            }
        } else {
            try (ProvisioningRuntime runtime = manager.getRuntime(layout)) {
                container = FeatureContainers.fromProvisioningRuntime(session, runtime);
            }
        }
        return container;
    }
}
