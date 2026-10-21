/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
